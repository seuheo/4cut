package com.example.a4cut.ui.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import com.example.a4cut.ui.viewmodel.PhotoState

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
     * 인생네컷 프레임 전용 합성 함수
     * @param frameBitmap 인생네컷 프레임 Bitmap
     * @param photos 합성할 Bitmap 사진 목록 (4개)
     * @return 합성된 최종 Bitmap
     */
    suspend fun composeLife4CutFrame(
        frameBitmap: Bitmap,
        photos: List<Bitmap?>
    ): Bitmap = withContext(Dispatchers.Default) {
        println("=== composeLife4CutFrame 시작 ===")
        println("프레임 크기: ${frameBitmap.width}x${frameBitmap.height}")
        println("전달받은 사진 개수: ${photos.size}")
        println("사진 null 체크: ${photos.map { it != null }}")
        
        // 프레임 크기에 맞춰 결과 Bitmap 생성
        val resultBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // 🚨 인생네컷 프레임의 각 사진 칸의 정확한 픽셀 좌표
        // Rect(left, top, right, bottom) - 세로로 4개 배치
        val photoRects = listOf(
            // 첫 번째 칸 (가장 위)
            RectF(85f, 125f, 490f, 475f),
            // 두 번째 칸
            RectF(85f, 495f, 490f, 845f),
            // 세 번째 칸
            RectF(85f, 865f, 490f, 1215f),
            // 네 번째 칸 (가장 아래)
            RectF(85f, 1235f, 490f, 1585f)
        )
        
        println("인생네컷 프레임 사진 영역 좌표:")
        photoRects.forEachIndexed { index, rect ->
            println("사진[$index]: (${rect.left.toInt()}, ${rect.top.toInt()}) - (${rect.right.toInt()}, ${rect.bottom.toInt()})")
            println("  크기: ${rect.width().toInt()}x${rect.height().toInt()}")
        }
        
        // 4장의 사진을 각 영역에 배치
        photos.take(4).forEachIndexed { index, photo ->
            photo?.let { bitmap ->
                val rect = photoRects[index]
                println("사진[$index] 처리 중: 원본 크기 ${bitmap.width}x${bitmap.height}")
                
                // 사진을 영역 크기에 맞춰 스케일링
                val scaledPhoto = Bitmap.createScaledBitmap(
                    bitmap,
                    rect.width().toInt(),
                    rect.height().toInt(),
                    true
                )
                
                // 캔버스에 사진 그리기
                canvas.drawBitmap(
                    scaledPhoto,
                    rect.left,
                    rect.top,
                    paint
                )
                println("사진[$index] 배치 완료: 위치 (${rect.left.toInt()}, ${rect.top.toInt()})")
            } ?: run {
                println("사진[$index]가 null이므로 건너뜀")
            }
        }
        
        println("=== composeLife4CutFrame 완료 ===")
        resultBitmap
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
     * Phase 3: PhotoState를 사용하여 편집된 사진과 프레임을 합성
     * @param photoStates 편집 상태가 포함된 사진 목록 (4개)
     * @param frameBitmap 적용할 프레임 Bitmap
     * @return 합성된 최종 Bitmap
     */
    suspend fun composeImageWithPhotoStates(
        photoStates: List<PhotoState>,
        frameBitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.Default) {
        // 최종 결과물이 될 Bitmap 생성
        val resultBitmap = Bitmap.createBitmap(OUTPUT_WIDTH, OUTPUT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. 프레임을 배경으로 그리기 (전체 화면에 맞춤)
        val frameRect = RectF(0f, 0f, OUTPUT_WIDTH.toFloat(), OUTPUT_HEIGHT.toFloat())
        canvas.drawBitmap(frameBitmap, null, frameRect, paint)

        // 2. 4컷 사진을 프레임 안의 지정된 위치에 그리기 (편집 상태 반영)
        val photoPositions = calculatePhotoPositions()
        
        photoStates.forEachIndexed { index, photoState ->
            if (photoState.bitmap != null && index < photoPositions.size) {
                val basePosition = photoPositions[index]
                
                // Matrix를 사용하여 사용자 편집 상태 적용
                val matrix = Matrix()
                
                // 기본 위치로 이동
                val photoRect = RectF(0f, 0f, photoState.bitmap.width.toFloat(), photoState.bitmap.height.toFloat())
                matrix.setRectToRect(photoRect, basePosition, Matrix.ScaleToFit.CENTER)
                
                // 사용자가 편집한 scale 적용 (중심점 기준)
                matrix.postScale(
                    photoState.scale, 
                    photoState.scale, 
                    basePosition.centerX(), 
                    basePosition.centerY()
                )
                
                // 사용자가 편집한 offset 적용
                matrix.postTranslate(photoState.offsetX, photoState.offsetY)
                
                // Canvas의 특정 영역에만 그려지도록 클리핑
                canvas.save()
                canvas.clipRect(basePosition)
                canvas.drawBitmap(photoState.bitmap, matrix, paint)
                canvas.restore()
            }
        }

        resultBitmap
    }

    /**
     * 단일 사진에 프레임을 적용하여 새로운 이미지를 생성
     * @param photoBitmap 적용할 사진 Bitmap
     * @param frameBitmap 적용할 프레임 Bitmap
     * @param isPreview 미리보기용 저해상도 여부 (true: 미리보기, false: 저장용)
     * @return 프레임이 적용된 Bitmap
     */
    suspend fun applyFrameToPhoto(
        photoBitmap: Bitmap,
        frameBitmap: Bitmap,
        isPreview: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        // 해상도 결정 (미리보기용은 저해상도, 저장용은 고해상도)
        val outputWidth = if (isPreview) OUTPUT_WIDTH / 2 else OUTPUT_WIDTH
        val outputHeight = if (isPreview) OUTPUT_HEIGHT / 2 else OUTPUT_HEIGHT
        
        // 최종 결과물이 될 Bitmap 생성
        val resultBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. 프레임을 배경으로 그리기 (전체 화면에 맞춤)
        val frameRect = RectF(0f, 0f, outputWidth.toFloat(), outputHeight.toFloat())
        canvas.drawBitmap(frameBitmap, null, frameRect, paint)

        // 2. 사진을 프레임 안의 중앙에 그리기
        val photoRect = calculateSinglePhotoPosition(outputWidth, outputHeight)
        canvas.drawBitmap(photoBitmap, null, photoRect, paint)

        resultBitmap
    }

    /**
     * 단일 사진의 배치 위치를 계산 (프레임 중앙에 배치)
     * @param outputWidth 출력 이미지 너비
     * @param outputHeight 출력 이미지 높이
     * @return 사진의 RectF 좌표
     */
    private fun calculateSinglePhotoPosition(outputWidth: Int, outputHeight: Int): RectF {
        // 여백 계산 (프레임 테두리 공간 확보)
        val marginRatio = 0.1f // 10% 여백
        val marginX = outputWidth * marginRatio
        val marginY = outputHeight * marginRatio
        
        // 사진 영역 크기 계산
        val photoWidth = outputWidth - (marginX * 2)
        val photoHeight = outputHeight - (marginY * 2)
        
        return RectF(
            marginX,
            marginY,
            marginX + photoWidth,
            marginY + photoHeight
        )
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
                return@withContext null
            }
        } ?: return@withContext null
    }

    /**
     * Drawable 리소스를 고품질 Bitmap으로 변환 (Vector Drawable + PNG 모두 지원)
     * @param context Context
     * @param drawableId Drawable 리소스 ID
     * @param width 원하는 너비
     * @param height 원하는 높이
     * @return 고품질 Bitmap
     */
    fun loadDrawableAsBitmap(
        context: Context,
        drawableId: Int,
        width: Int,
        height: Int
    ): Bitmap {
        println("ImageComposer: Drawable 로딩 시작 - ID: $drawableId, 크기: ${width}x${height}")
        val drawable = context.getDrawable(drawableId)
        if (drawable == null) {
            println("ImageComposer: Drawable이 null입니다 - ID: $drawableId")
        } else {
            println("ImageComposer: Drawable 로딩 성공 - ${drawable.javaClass.simpleName}")
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)
        
        println("ImageComposer: Bitmap 생성 완료 - ${bitmap.width}x${bitmap.height}")
        return bitmap
    }
    
    /**
     * Vector Drawable을 고품질 Bitmap으로 변환 (하위 호환성)
     * @param context Context
     * @param drawableId Vector Drawable 리소스 ID
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
        return loadDrawableAsBitmap(context, drawableId, width, height)
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
