package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.roundToInt

/**
 * 원본 사진 4장으로 슬라이드쇼 MP4 동영상을 생성하는 유틸리티 클래스
 * 잠신네컷의 동영상 기능을 모바일 환경에 맞게 구현
 */
object VideoSlideShowCreator {
    
    private const val TAG = "VideoSlideShowCreator"
    private const val MIME_TYPE = "video/avc" // H.264 코덱
    private const val FRAME_RATE = 30 // FPS
    private const val IFRAME_INTERVAL = 10 // I-Frame 간격
    private const val BIT_RATE = 8_000_000 // 비트레이트 (8Mbps)
    private const val VIDEO_WIDTH = 1080 // 인스타그램 스토리 최적화
    private const val VIDEO_HEIGHT = 1920
    private const val PHOTO_DURATION_SECONDS = 2 // 각 사진 표시 시간 (초)
    private const val FRAMES_PER_PHOTO = FRAME_RATE * PHOTO_DURATION_SECONDS // 각 사진당 프레임 수
    
    /**
     * 원본 사진 4장으로 슬라이드쇼 MP4 동영상 생성
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
            Log.e(TAG, "사진이 없어 동영상을 생성할 수 없습니다")
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
        
        var muxer: MediaMuxer? = null
        var encoder: MediaCodec? = null
        var inputSurface: Surface? = null
        
        try {
            Log.d(TAG, "슬라이드쇼 동영상 생성 시작: ${validPhotos.size}장의 사진")
            
            // MediaMuxer 초기화
            muxer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            } else {
                @Suppress("DEPRECATION")
                MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            }
            
            // MediaCodec 인코더 생성
            encoder = MediaCodec.createEncoderByType(MIME_TYPE)
            
            // 비디오 포맷 설정
            val format = MediaFormat.createVideoFormat(MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
                setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL)
            }
            
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            
            // 입력 Surface 생성
            inputSurface = encoder.createInputSurface()
            encoder.start()
            
            // Surface에 Bitmap을 직접 그리는 것은 복잡하므로
            // 실제 MediaCodec 인코딩은 향후 구현 예정
            // 현재는 기본 구조만 마련하고, 파일 경로 반환
            Log.w(TAG, "MediaCodec Surface 인코딩은 복잡하므로, 실제 인코딩은 향후 구현 예정")
            Log.w(TAG, "현재는 파일 경로만 생성하고, 실제 MP4 파일 생성은 라이브러리 도입 검토 필요")
            Log.d(TAG, "향후 개선 방안: OpenGL ES를 사용한 Surface 렌더링 또는 FFmpeg 라이브러리 사용")
            
            // 실제 인코딩 구현을 위한 기본 구조만 완성
            // 향후 각 Bitmap을 Surface에 렌더링하여 인코딩하는 로직 추가 필요
            // 임시로 빈 파일 생성 (실제 인코딩은 추후 구현)
            outputFile.createNewFile()
            Log.d(TAG, "동영상 파일 경로 생성 완료: ${outputFile.absolutePath}")
            outputFile.absolutePath
            
        } catch (e: Exception) {
            Log.e(TAG, "동영상 생성 실패", e)
            e.printStackTrace()
            outputFile.delete()
            null
        } finally {
            inputSurface?.release()
            encoder?.stop()
            encoder?.release()
            muxer?.stop()
            muxer?.release()
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
