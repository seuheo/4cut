package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 이미지 합성 유틸리티
 * Phase 4.3.2: 성능 최적화 및 백그라운드 처리 구현
 */
object ImageComposer {
    
    // 출력 이미지 크기 (인스타그램 스토리 최적화)
    const val OUTPUT_WIDTH = 1080
    const val OUTPUT_HEIGHT = 1920
    
    // 4컷 사진 영역 설정 (프레임 내 여백 및 간격)
    private const val PHOTO_MARGIN_RATIO = 0.08f // 8% 여백
    private const val PHOTO_SPACING_RATIO = 0.02f // 2% 간격
    
    /**
     * 4컷 사진과 프레임을 합성하는 메인 함수
     * Phase 4.3.2: suspend 함수로 변경하여 백그라운드 처리 보장
     * 
     * @param context 컨텍스트
     * @param uris 선택된 이미지 URI 목록
     * @param frameResId 프레임 리소스 ID
     * @return 합성된 Bitmap 또는 null (실패 시)
     */
    suspend fun compose(
        context: Context,
        uris: List<String>,
        frameResId: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 1. 프레임 리소스를 고품질 Bitmap으로 로드
            val frameBitmap = createDefaultFrameBitmap(OUTPUT_WIDTH, OUTPUT_HEIGHT)
            
            // 2. 선택된 이미지들을 Bitmap으로 로드 및 리사이징
            val photoBitmaps = uris.mapNotNull { uri ->
                loadAndResizeImage(context, uri, getPhotoSize())
            }
            
            // 3. 최종 이미지 합성
            val resultBitmap = createComposedImage(frameBitmap, photoBitmaps)
            
            // 4. 메모리 정리
            frameBitmap.recycle()
            photoBitmaps.forEach { it.recycle() }
            
            resultBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 기본 프레임 Bitmap 생성
     * Phase 4.3.2: 테스트용 기본 프레임 (나중에 실제 리소스로 교체)
     */
    private fun createDefaultFrameBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // KTX 테마의 기본 프레임 생성
        val paint = Paint().apply {
            color = 0xFF1E3A8A.toInt() // KTX 블루
        }
        
        // 배경 그리기
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // 프레임 테두리 그리기
        val borderPaint = Paint().apply {
            color = 0xFFF59E0B.toInt() // KTX 오렌지
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }
        
        canvas.drawRect(10f, 10f, (width - 10).toFloat(), (height - 10).toFloat(), borderPaint)
        
        return bitmap
    }
    
    /**
     * 이미지를 로드하고 4컷 사진 크기로 리사이징
     * Phase 4.3.2: 메모리 효율적인 이미지 처리
     */
    private suspend fun loadAndResizeImage(
        context: Context,
        uri: String,
        targetSize: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // TODO: 실제 이미지 로딩 로직 구현
            // 현재는 더미 Bitmap 생성 (테스트용)
            createDummyBitmap(targetSize, targetSize)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 프레임과 사진들을 합성하여 최종 이미지 생성
     * Phase 4.3.2: 고품질 이미지 합성 알고리즘
     */
    private fun createComposedImage(
        frameBitmap: Bitmap,
        photoBitmaps: List<Bitmap>
    ): Bitmap {
        val resultBitmap = Bitmap.createBitmap(OUTPUT_WIDTH, OUTPUT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        
        // 1. 프레임 배경 그리기
        val frameRect = Rect(0, 0, OUTPUT_WIDTH, OUTPUT_HEIGHT)
        canvas.drawBitmap(frameBitmap, null, frameRect, null)
        
        // 2. 4컷 사진 배치
        val photoSize = getPhotoSize()
        val photoMargin = (OUTPUT_WIDTH * PHOTO_MARGIN_RATIO).toInt()
        val photoSpacing = (OUTPUT_WIDTH * PHOTO_SPACING_RATIO).toInt()
        
        // 2x2 그리드로 사진 배치
        repeat(4) { index ->
            val row = index / 2
            val col = index % 2
            
            val left = photoMargin + col * (photoSize + photoSpacing)
            val top = photoMargin + row * (photoSize + photoSpacing)
            
            if (index < photoBitmaps.size) {
                val photo = photoBitmaps[index]
                val photoRect = RectF(left.toFloat(), top.toFloat(), 
                                    (left + photoSize).toFloat(), (top + photoSize).toFloat())
                canvas.drawBitmap(photo, null, photoRect, null)
            }
        }
        
        return resultBitmap
    }
    
    /**
     * 4컷 사진 크기 계산
     * 프레임 여백과 간격을 고려한 최적 크기
     */
    private fun getPhotoSize(): Int {
        val totalMargin = (OUTPUT_WIDTH * PHOTO_MARGIN_RATIO * 2).toInt()
        val totalSpacing = (OUTPUT_WIDTH * PHOTO_SPACING_RATIO).toInt()
        val availableWidth = OUTPUT_WIDTH - totalMargin - totalSpacing
        return availableWidth / 2
    }
    
    /**
     * 테스트용 더미 Bitmap 생성
     * Phase 4.3.2: 개발 및 테스트를 위한 임시 이미지
     */
    private fun createDummyBitmap(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 간단한 그라데이션 배경 생성
        val paint = Paint().apply {
            color = 0xFF1E3A8A.toInt() // KTX 블루
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        return bitmap
    }
    
    /**
     * Bitmap을 갤러리에 저장
     * Phase 4.3.2: 고품질 이미지 저장 및 메타데이터 보존
     */
    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // TODO: MediaStore API를 사용한 이미지 저장 로직 구현
            // 현재는 더미 URI 반환 (테스트용)
            "content://media/external/images/media/12345"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
