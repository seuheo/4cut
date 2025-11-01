package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
     * 원본 사진 4장으로 슬라이드쇼 MP4 동영상 생성
     * @param photos 원본 사진 Bitmap 목록 (4장)
     * @param context Context
     * @return 생성된 MP4 파일 경로 (실패 시 null)
     * 
     * 주의: 현재는 라이브러리 의존성 문제로 실제 인코딩이 구현되지 않았습니다.
     * 시도한 라이브러리들:
     * 1. FFmpegKit (은퇴 2025-01-06) - Maven Central에서 제거됨
     * 2. WritingMinds FFmpeg-Android-Java - JitPack에서 찾을 수 없음
     * 3. bravoborja/tanersener 포크 - 401 Unauthorized
     * 4. JCodec - API 시그니처 문제
     * 
     * TODO: FFmpeg 바이너리 직접 포함 또는 MediaCodec 직접 구현 검토 필요
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
        
        try {
            Log.d(TAG, "슬라이드쇼 동영상 생성 시작: ${validPhotos.size}장의 사진")
            Log.w(TAG, "현재는 라이브러리 의존성 문제로 실제 인코딩 미구현")
            Log.w(TAG, "FFmpeg 바이너리 직접 포함 또는 MediaCodec 직접 구현 필요")
            
            // TODO: 실제 비디오 인코딩 구현 필요
            // 현재는 파일 경로만 생성하고 null 반환
            // 실제 라이브러리 확인 후 인코딩 로직 추가 예정
            
            null
            
        } catch (e: Exception) {
            Log.e(TAG, "동영상 생성 실패", e)
            e.printStackTrace()
            outputFile.delete()
            null
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
