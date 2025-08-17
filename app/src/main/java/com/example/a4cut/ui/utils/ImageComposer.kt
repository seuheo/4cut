package com.example.a4cut.ui.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * 이미지 합성 및 저장을 담당하는 유틸리티 클래스
 * Phase 3.2: 4컷 사진과 KTX 프레임을 합성하여 최종 이미지 생성
 */
class ImageComposer(private val context: Context) {

    companion object {
        // 최종 출력 이미지 해상도 (인스타그램 스토리 최적화)
        const val OUTPUT_WIDTH = 1080
        const val OUTPUT_HEIGHT = 1920
        
        // 프레임 내 사진 배치 비율 (전체 해상도 대비)
        private const val HORIZONTAL_MARGIN_RATIO = 0.08f  // 좌우 여백 8%
        private const val VERTICAL_MARGIN_RATIO = 0.15f     // 상하 여백 15%
        private const val PHOTO_SPACING_RATIO = 0.02f       // 사진 간 간격 2%
    }

    /**
     * 4컷 사진과 프레임을 합성하여 최종 이미지를 생성
     * @param photos 합성할 Bitmap 사진 목록 (4개)
     * @param frameBitmap 적용할 프레임 Bitmap
     * @return 합성된 최종 Bitmap
     */
    suspend fun composeImage(
        photos: List<Bitmap?>,
        frameBitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.Default) {
        // 최종 결과물이 될 Bitmap 생성
        val resultBitmap = Bitmap.createBitmap(OUTPUT_WIDTH, OUTPUT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. 프레임을 배경으로 그리기 (전체 화면에 맞춤)
        val frameRect = RectF(0f, 0f, OUTPUT_WIDTH.toFloat(), OUTPUT_HEIGHT.toFloat())
        canvas.drawBitmap(frameBitmap, null, frameRect, paint)

        // 2. 4컷 사진을 프레임 안의 지정된 위치에 그리기
        val photoPositions = calculatePhotoPositions()
        
        photos.forEachIndexed { index, bitmap ->
            if (bitmap != null && index < photoPositions.size) {
                val position = photoPositions[index]
                canvas.drawBitmap(bitmap, null, position, paint)
            }
        }

        resultBitmap
    }

    /**
     * 4컷 사진의 배치 위치를 비율 기반으로 계산
     * @return 각 사진의 RectF 좌표 목록
     */
    private fun calculatePhotoPositions(): List<RectF> {
        // 여백 계산
        val horizontalMargin = OUTPUT_WIDTH * HORIZONTAL_MARGIN_RATIO
        val verticalMargin = OUTPUT_HEIGHT * VERTICAL_MARGIN_RATIO
        val photoSpacing = OUTPUT_WIDTH * PHOTO_SPACING_RATIO
        
        // 사진 영역 크기 계산
        val totalPhotoAreaWidth = OUTPUT_WIDTH - (horizontalMargin * 2) - photoSpacing
        val totalPhotoAreaHeight = OUTPUT_HEIGHT - (verticalMargin * 2) - photoSpacing
        
        val photoWidth = totalPhotoAreaWidth / 2
        val photoHeight = totalPhotoAreaHeight / 2
        
        // 각 사진의 위치 계산 (2x2 그리드)
        return listOf(
            // 1번 사진 (좌상단)
            RectF(
                horizontalMargin,
                verticalMargin,
                horizontalMargin + photoWidth,
                verticalMargin + photoHeight
            ),
            // 2번 사진 (우상단)
            RectF(
                horizontalMargin + photoWidth + photoSpacing,
                verticalMargin,
                OUTPUT_WIDTH - horizontalMargin,
                verticalMargin + photoHeight
            ),
            // 3번 사진 (좌하단)
            RectF(
                horizontalMargin,
                verticalMargin + photoHeight + photoSpacing,
                horizontalMargin + photoWidth,
                OUTPUT_HEIGHT - verticalMargin
            ),
            // 4번 사진 (우하단)
            RectF(
                horizontalMargin + photoWidth + photoSpacing,
                verticalMargin + photoHeight + photoSpacing,
                OUTPUT_WIDTH - horizontalMargin,
                OUTPUT_HEIGHT - verticalMargin
            )
        )
    }

    /**
     * Bitmap 이미지를 갤러리에 JPEG 형식으로 저장
     * @param bitmap 저장할 Bitmap
     * @param displayName 파일명 (예: "KTX_4cut_20241219.jpg")
     * @return 저장 성공 시 URI, 실패 시 null
     */
    suspend fun saveBitmapToGallery(bitmap: Bitmap, displayName: String): Uri? = withContext(Dispatchers.IO) {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(imageCollection, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    // JPEG 품질 95로 압축하여 저장 (품질과 용량의 균형)
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw Exception("Bitmap 압축 실패")
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
                return@withContext it
            } catch (e: Exception) {
                // 저장 실패 시 생성된 URI 삭제
                resolver.delete(it, null, null)
                e.printStackTrace()
                return@withContext false
            }
        } ?: return@withContext false
    }

    /**
     * 벡터 드로어블을 고품질 Bitmap으로 변환
     * @param context Context
     * @param drawableId 벡터 드로어블 리소스 ID
     * @param width 원하는 너비
     * @param height 원하는 높이
     * @return 고품질 Bitmap
     */
    fun loadVectorDrawableAsBitmap(
        context: Context,
        drawableId: Int,
        width: Int,
        height: Int
    ): Bitmap {
        val drawable = context.getDrawable(drawableId)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)
        
        return bitmap
    }

    /**
     * Bitmap 메모리 해제 (메모리 누수 방지)
     * @param bitmap 해제할 Bitmap
     */
    fun recycleBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
    }

    /**
     * 여러 Bitmap 메모리 해제
     * @param bitmaps 해제할 Bitmap 목록
     */
    fun recycleBitmaps(bitmaps: List<Bitmap?>) {
        bitmaps.forEach { recycleBitmap(it) }
    }
}
