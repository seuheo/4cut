package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.scale.BitmapUtil
import java.io.File

/**
 * 원본 사진 4장으로 슬라이드쇼 MP4 동영상을 생성하는 유틸리티 클래스
 * 잠신네컷의 동영상 기능을 모바일 환경에 맞게 구현
 */
object VideoSlideShowCreator {
    
    private const val TAG = "VideoSlideShowCreator"
    private const val VIDEO_WIDTH = 1080 // 인스타그램 스토리 최적화
    private const val VIDEO_HEIGHT = 1920
    private const val PHOTO_DURATION_SECONDS = 2 // 각 사진 표시 시간 (초)
    private const val FRAME_RATE = 30 // FPS
    private const val FRAMES_PER_PHOTO = FRAME_RATE * PHOTO_DURATION_SECONDS // 각 사진당 프레임 수 (60프레임)
    
    /**
     * 원본 사진 4장으로 슬라이드쇼 MP4 동영상 생성 (JCodec AndroidSequenceEncoder 사용)
     * @param photos 원본 사진 Bitmap 목록 (4장)
     * @param context Context
     * @return 생성된 MP4 파일 경로 (실패 시 null)
     */
    suspend fun createSlideShowVideo(
        photos: List<Bitmap?>,
        context: Context
    ): String? = withContext(Dispatchers.IO) {
        val validPhotos = photos.filterNotNull()
        if (validPhotos.isEmpty()) {
            Log.e(TAG, "슬라이드쇼를 만들 유효한 사진이 없습니다")
            return@withContext null
        }
        
        val outputFile = File(
            context.getExternalFilesDir(null)?.let { 
                File(it, "videos")
            } ?: context.cacheDir,
            "slideshow_${System.currentTimeMillis()}.mp4"
        )
        
        // 비디오 디렉토리 생성
        outputFile.parentFile?.mkdirs()
        
        try {
            Log.d(TAG, "JCodec 동영상 생성 시작: ${outputFile.absolutePath} (${validPhotos.size}장의 사진)")
            
            // 1. AndroidSequenceEncoder 생성 (File과 프레임 속도 사용)
            // createSequenceEncoder는 File과 Int(프레임 속도)를 받음
            val encoder = AndroidSequenceEncoder.createSequenceEncoder(outputFile, FRAME_RATE)
            
            // 2. 각 Bitmap을 리사이즈하고 Picture로 변환하여 인코딩
            validPhotos.forEachIndexed { index, bitmap ->
                Log.d(TAG, "${index + 1}번째 사진 인코딩 중...")
                
                // Bitmap 리사이즈 (1080x1920에 맞춤)
                val resizedBitmap = resizeBitmap(bitmap, VIDEO_WIDTH, VIDEO_HEIGHT)
                
                try {
                    // Bitmap을 JCodec Picture 포맷으로 변환
                    val picture = BitmapUtil.fromBitmap(resizedBitmap)
                    
                    // 각 사진을 2초(60프레임) 동안 인코딩
                    for (i in 0 until FRAMES_PER_PHOTO) {
                        encoder.encodeNativeFrame(picture)
                    }
                    
                    Log.d(TAG, "${index + 1}번째 사진 인코딩 완료 (${FRAMES_PER_PHOTO}프레임)")
                    
                    // 리사이즈된 Bitmap 재활용
                    if (resizedBitmap != bitmap && !resizedBitmap.isRecycled) {
                        resizedBitmap.recycle()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Picture 변환 또는 인코딩 실패: 사진 ${index + 1}", e)
                    throw e
                }
            }
            
            // 3. 인코더 완료
            encoder.finish()
            Log.d(TAG, "JCodec 동영상 생성 완료: ${outputFile.absolutePath} (총 ${validPhotos.size * FRAMES_PER_PHOTO}프레임)")
            
            outputFile.absolutePath
            
        } catch (e: Exception) {
            Log.e(TAG, "JCodec 동영상 생성 실패", e)
            e.printStackTrace()
            outputFile.delete() // 실패 시 파일 삭제
            null
        } finally {
            // 4. 리소스 정리 완료
            // AndroidSequenceEncoder는 finish() 호출 시 자동으로 리소스를 정리함
            Log.d(TAG, "인코더 리소스 정리 완료")
        }
    }
    
    /**
     * Bitmap을 지정된 크기로 리사이즈 (비율 유지)
     */
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scale = minOf(
            targetWidth.toFloat() / width,
            targetHeight.toFloat() / height
        )
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
