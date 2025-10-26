package com.example.a4cut.ui.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
        
        // 롱 폼 프레임 실제 크기 (50x152mm)
        private const val LONG_FORM_WIDTH_MM = 50f
        private const val LONG_FORM_HEIGHT_MM = 152f
        private const val DPI = 300f // 인쇄 품질을 위한 DPI
        
        // 프레임 내 사진 배치 비율 (전체 해상도 대비)
        private const val HORIZONTAL_MARGIN_RATIO = 0.08f  // 좌우 여백 8%
        private const val VERTICAL_MARGIN_RATIO = 0.15f     // 상하 여백 15%
        private const val PHOTO_SPACING_RATIO = 0.02f       // 사진 간 간격 2%
        
        /**
         * mm를 픽셀로 변환 (DPI 기준)
         */
        private fun mmToPixels(mm: Float): Int {
            return (mm * DPI / 25.4f).toInt() // 1인치 = 25.4mm
        }
        
        /**
         * 롱 폼 프레임의 출력 크기 계산 (50x152mm)
         */
        fun getLongFormOutputSize(): Pair<Int, Int> {
            val width = mmToPixels(LONG_FORM_WIDTH_MM)
            val height = mmToPixels(LONG_FORM_HEIGHT_MM)
            return Pair(width, height)
        }
    }

    /**
     * 인생네컷 프레임 전용 합성 함수
     * @param frameBitmap 인생네컷 프레임 Bitmap
     * @param photos 합성할 Bitmap 사진 목록 (4개)
     * @param frameId 프레임 ID (선택사항, 프레임별 맞춤형 위치 계산용)
     * @return 합성된 최종 Bitmap
     */
    suspend fun composeLife4CutFrame(
        frameBitmap: Bitmap,
        photos: List<Bitmap?>,
        frameId: String? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        println("=== ImageComposer: composeLife4CutFrame 시작 ===")
        println("프레임 ID: $frameId")
        println("프레임 크기: ${frameBitmap.width}x${frameBitmap.height}")
        println("프레임 비율: ${frameBitmap.width.toFloat() / frameBitmap.height.toFloat()}")
        println("입력 사진 개수: ${photos.size}")
        println("사진 null 체크: ${photos.map { it != null }}")
        println("사진 크기들: ${photos.map { "${it?.width ?: 0}x${it?.height ?: 0}" }}")
        
        // 롱 폼 프레임인 경우 실제 크기(50x152mm)로 출력 크기 결정
        val (outputWidth, outputHeight) = if (frameId == "long_form_white" || frameId == "long_form_black") {
            val size = getLongFormOutputSize()
            println("롱 폼 프레임 감지! 실제 크기 사용: ${size.first}x${size.second} (50x152mm)")
            size
        } else {
            println("일반 프레임 사용: ${frameBitmap.width}x${frameBitmap.height}")
            Pair(frameBitmap.width, frameBitmap.height)
        }
        
        // 메모리 최적화된 Bitmap 생성
        val resultBitmap = createSafeBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
            ?: throw IllegalStateException("결과 Bitmap 생성 실패")
        
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. 프레임을 배경으로 그리기 (출력 크기에 맞춤)
        val frameRect = RectF(0f, 0f, outputWidth.toFloat(), outputHeight.toFloat())
        canvas.drawBitmap(frameBitmap, null, frameRect, paint)
        println("프레임 배경 그리기 완료: ${outputWidth}x${outputHeight}")

        // ✨ 중요: 프레임 위에 세로로 약간 더 긴 4개의 사각형 안에 사진 배치
        val photoRects = calculateLife4CutPhotoPositions(outputWidth, outputHeight, frameId)
        println("계산된 사진 위치들:")
        photoRects.forEachIndexed { index, rect ->
            println("  ${index + 1}번째: (${rect.left}, ${rect.top}, ${rect.right}, ${rect.bottom}) 크기: ${rect.width()}x${rect.height()}")
        }

        photos.take(4).forEachIndexed { index, photo ->
            photo?.let { bitmap ->
                val rect = photoRects[index]
                println("${index + 1}번째 사진 처리 시작:")
                println("  원본 크기: ${bitmap.width}x${bitmap.height}")
                println("  목표 크기: ${rect.width().toInt()}x${rect.height().toInt()}")
                
                // 각 칸의 크기에 맞게 사진을 프레임 사각형 모양에 정확히 맞게 크기 조절
                val scaledPhoto = scaleBitmapToFill(bitmap, rect.width().toInt(), rect.height().toInt())
                println("  스케일된 크기: ${scaledPhoto.width}x${scaledPhoto.height}")
                
                // 정확한 위치에 사진을 그립니다.
                canvas.drawBitmap(scaledPhoto, rect.left, rect.top, paint)
                println("  ${index + 1}번째 사진 그리기 완료")
                
                // 스케일된 비트맵이 원본과 다른 경우에만 메모리 해제
                if (scaledPhoto != bitmap && !scaledPhoto.isRecycled) {
                    // UI에서 사용 중인 Bitmap은 재활용하지 않음
                    // scaledPhoto.recycle() // 주석 처리
                }
            } ?: run {
                println("${index + 1}번째 사진이 null이므로 건너뜀")
            }
        }
        
        println("=== ImageComposer: composeLife4CutFrame 완료 ===")
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
        
        // 메모리 최적화된 Bitmap 생성
        val bitmap = createSafeBitmap(width, height, Bitmap.Config.ARGB_8888)
            ?: throw IllegalStateException("Bitmap 생성 실패")
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
     * 안전한 Bitmap 생성 (메모리 최적화)
     * @param width 너비
     * @param height 높이
     * @param config Bitmap 설정
     * @return 생성된 Bitmap 또는 null
     */
    fun createSafeBitmap(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.RGB_565): Bitmap? {
        return try {
            // 메모리 사용량 계산
            val memoryUsage = width * height * when (config) {
                Bitmap.Config.ALPHA_8 -> 1
                Bitmap.Config.RGB_565 -> 2
                Bitmap.Config.ARGB_4444 -> 2
                Bitmap.Config.ARGB_8888 -> 4
                else -> 4
            }
            
            // 메모리 사용량이 너무 크면 null 반환
            if (memoryUsage > 50 * 1024 * 1024) { // 50MB 제한
                println("ImageComposer: 메모리 사용량이 너무 큽니다: ${memoryUsage / 1024 / 1024}MB")
                return null
            }
            
            Bitmap.createBitmap(width, height, config)
        } catch (e: OutOfMemoryError) {
            println("ImageComposer: 메모리 부족으로 Bitmap 생성 실패: ${e.message}")
            null
        } catch (e: Exception) {
            println("ImageComposer: Bitmap 생성 중 오류: ${e.message}")
            null
        }
    }

    /**
     * 여러 Bitmap 메모리 해제
     * @param bitmaps 해제할 Bitmap 목록
     */
    fun recycleBitmaps(bitmaps: List<Bitmap?>) {
        bitmaps.forEach { recycleBitmap(it) }
    }

    /**
     * 프레임별 사진 위치 정의
     */
    data class FramePhotoLayout(
        val frameId: String,
        val photoPositions: List<RectF> // 정규화된 좌표 (0.0 ~ 1.0)
    )
    
    /**
     * 프레임별 사진 위치 데이터
     */
    private val frameLayouts = mapOf(
        "ktx_frame_signature" to FramePhotoLayout(
            frameId = "ktx_frame_signature",
            photoPositions = listOf(
                // 1번 사진 (좌상단) - 정규화된 좌표
                RectF(0.12f, 0.15f, 0.47f, 0.50f),
                // 2번 사진 (우상단)
                RectF(0.53f, 0.15f, 0.88f, 0.50f),
                // 3번 사진 (좌하단)
                RectF(0.12f, 0.55f, 0.47f, 0.85f),
                // 4번 사진 (우하단)
                RectF(0.53f, 0.55f, 0.88f, 0.85f)
            )
        ),
        "ktx_frame_busan" to FramePhotoLayout(
            frameId = "ktx_frame_busan",
            photoPositions = listOf(
                RectF(0.12f, 0.15f, 0.47f, 0.50f),
                RectF(0.53f, 0.15f, 0.88f, 0.50f),
                RectF(0.12f, 0.55f, 0.47f, 0.85f),
                RectF(0.53f, 0.55f, 0.88f, 0.85f)
            )
        ),
        "ktx_frame_jeonju" to FramePhotoLayout(
            frameId = "ktx_frame_jeonju",
            photoPositions = listOf(
                RectF(0.12f, 0.15f, 0.47f, 0.50f),
                RectF(0.53f, 0.15f, 0.88f, 0.50f),
                RectF(0.12f, 0.55f, 0.47f, 0.85f),
                RectF(0.53f, 0.55f, 0.88f, 0.85f)
            )
        ),
        "ktx_frame_seoul" to FramePhotoLayout(
            frameId = "ktx_frame_seoul",
            photoPositions = listOf(
                RectF(0.12f, 0.15f, 0.47f, 0.50f),
                RectF(0.53f, 0.15f, 0.88f, 0.50f),
                RectF(0.12f, 0.55f, 0.47f, 0.85f),
                RectF(0.53f, 0.55f, 0.88f, 0.85f)
            )
        ),
        "ktx_frame_gyeongju" to FramePhotoLayout(
            frameId = "ktx_frame_gyeongju",
            photoPositions = listOf(
                RectF(0.12f, 0.15f, 0.47f, 0.50f),
                RectF(0.53f, 0.15f, 0.88f, 0.50f),
                RectF(0.12f, 0.55f, 0.47f, 0.85f),
                RectF(0.53f, 0.55f, 0.88f, 0.85f)
            )
        ),
        "image_e15024" to FramePhotoLayout(
            frameId = "image_e15024",
            photoPositions = listOf(
                // 제공해주신 픽셀 좌표를 정규화된 좌표로 변환
                // 프레임 크기 가정: 1080x1920 (9:16 비율)
                // 첫 번째 칸 (맨 위): Rect(75, 140, 480, 520)
                RectF(0.069f, 0.073f, 0.444f, 0.271f),
                // 두 번째 칸: Rect(75, 545, 480, 925)
                RectF(0.069f, 0.284f, 0.444f, 0.482f),
                // 세 번째 칸: Rect(75, 950, 480, 1330)
                RectF(0.069f, 0.495f, 0.444f, 0.693f),
                // 네 번째 칸 (맨 아래): Rect(75, 1355, 480, 1735)
                RectF(0.069f, 0.706f, 0.444f, 0.904f)
            )
        ),
        "long_form_white" to FramePhotoLayout(
            frameId = "long_form_white",
            photoPositions = listOf(
                // Long Form White 프레임용 위치 (2:6 인치 스타일, 세로형 레이아웃)
                // 프레임 비율: 1:3 (가로:세로)
                // 각 칸은 프레임의 84% 너비, 19% 높이를 차지하며 첫 번째 사진 위치 유지, 사진 간격만 살짝 줄임 (간격 1.5%)
                RectF(0.08f, 0.03f, 0.92f, 0.22f),  // 첫 번째 칸 (Top: 3%, Bottom: 22%) Height: 19%
                RectF(0.08f, 0.235f, 0.92f, 0.425f),  // 두 번째 칸 (Top: 23.5%, Bottom: 42.5%) Height: 19% / Gap: 1.5%
                RectF(0.08f, 0.44f, 0.92f, 0.63f),  // 세 번째 칸 (Top: 44%, Bottom: 63%) Height: 19% / Gap: 1.5%
                RectF(0.08f, 0.645f, 0.92f, 0.835f)   // 네 번째 칸 (Top: 64.5%, Bottom: 83.5%) Height: 19% / Gap: 1.5%
            )
        ),
        "long_form_black" to FramePhotoLayout(
            frameId = "long_form_black",
            photoPositions = listOf(
                // Long Form Black 프레임용 위치 (2:6 인치 스타일, 세로형 레이아웃)
                // 프레임 비율: 1:3 (가로:세로)
                // 각 칸은 프레임의 84% 너비, 19% 높이를 차지하며 첫 번째 사진 위치 유지, 사진 간격만 살짝 줄임 (간격 1.5%)
                RectF(0.08f, 0.03f, 0.92f, 0.22f),  // 첫 번째 칸 (Top: 3%, Bottom: 22%) Height: 19%
                RectF(0.08f, 0.235f, 0.92f, 0.425f),  // 두 번째 칸 (Top: 23.5%, Bottom: 42.5%) Height: 19% / Gap: 1.5%
                RectF(0.08f, 0.44f, 0.92f, 0.63f),  // 세 번째 칸 (Top: 44%, Bottom: 63%) Height: 19% / Gap: 1.5%
                RectF(0.08f, 0.645f, 0.92f, 0.835f)   // 네 번째 칸 (Top: 64.5%, Bottom: 83.5%) Height: 19% / Gap: 1.5%
            )
        )
    )
    
    /**
     * 인생네컷 프레임용 4개 사진의 배치 위치를 계산 (프레임별 맞춤형)
     * @param frameWidth 프레임 너비
     * @param frameHeight 프레임 높이
     * @param frameId 프레임 ID (선택사항, 기본값은 기본 레이아웃 사용)
     * @return 각 사진의 RectF 좌표 목록
     */
    private fun calculateLife4CutPhotoPositions(
        frameWidth: Int, 
        frameHeight: Int, 
        frameId: String? = null
    ): List<RectF> {
        println("=== calculateLife4CutPhotoPositions 시작 ===")
        println("입력 프레임 크기: ${frameWidth}x${frameHeight}")
        println("프레임 ID: $frameId")
        
        // 프레임별 맞춤형 레이아웃 사용 또는 기본 레이아웃 사용
        val layout = frameId?.let { frameLayouts[it] } ?: frameLayouts["ktx_frame_signature"]
        
        if (layout != null) {
            println("프레임별 맞춤형 레이아웃 사용: ${layout.frameId}")
            
            // 정규화된 좌표를 실제 픽셀 좌표로 변환
            val positions = layout.photoPositions.map { normalizedRect ->
                RectF(
                    normalizedRect.left * frameWidth,
                    normalizedRect.top * frameHeight,
                    normalizedRect.right * frameWidth,
                    normalizedRect.bottom * frameHeight
                )
            }
            
            println("최종 계산된 위치들 (프레임별 맞춤형):")
            positions.forEachIndexed { index, rect ->
                println("  ${index + 1}번째: (${rect.left}, ${rect.top}, ${rect.right}, ${rect.bottom})")
                println("    크기: ${rect.width()}x${rect.height()}")
            }
            println("=== calculateLife4CutPhotoPositions 완료 ===")
            
            return positions
        } else {
            println("프레임별 레이아웃을 찾을 수 없어 기본 계산 방식 사용")
            
            // 기존 방식으로 폴백 (하드코딩된 비율 사용)
            val horizontalMargin = frameWidth * 0.12f
            val verticalMargin = frameHeight * 0.15f
            val photoSpacing = frameWidth * 0.03f
            
            val totalPhotoAreaWidth = frameWidth - (horizontalMargin * 2) - photoSpacing
            val totalPhotoAreaHeight = frameHeight - (verticalMargin * 2) - photoSpacing
            
            val photoWidth = totalPhotoAreaWidth / 2
            val photoHeight = totalPhotoAreaHeight / 2
            val adjustedPhotoHeight = photoHeight * 1.05f
            
            val positions = listOf(
                RectF(horizontalMargin, verticalMargin, horizontalMargin + photoWidth, verticalMargin + adjustedPhotoHeight),
                RectF(horizontalMargin + photoWidth + photoSpacing, verticalMargin, frameWidth - horizontalMargin, verticalMargin + adjustedPhotoHeight),
                RectF(horizontalMargin, verticalMargin + adjustedPhotoHeight + photoSpacing, horizontalMargin + photoWidth, frameHeight - verticalMargin),
                RectF(horizontalMargin + photoWidth + photoSpacing, verticalMargin + adjustedPhotoHeight + photoSpacing, frameWidth - horizontalMargin, frameHeight - verticalMargin)
            )
            
            println("최종 계산된 위치들 (기본 방식):")
            positions.forEachIndexed { index, rect ->
                println("  ${index + 1}번째: (${rect.left}, ${rect.top}, ${rect.right}, ${rect.bottom})")
                println("    크기: ${rect.width()}x${rect.height()}")
            }
            println("=== calculateLife4CutPhotoPositions 완료 ===")
            
            return positions
        }
    }

    /**
     * Bitmap을 지정된 사각형 크기에 맞게 조절 (비율 유지하면서 프레임을 완전히 채움)
     * @param bitmap 원본 Bitmap
     * @param targetWidth 목표 너비
     * @param targetHeight 목표 높이
     * @return 조절된 Bitmap
     */
    private fun scaleBitmapToFit(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        println("=== scaleBitmapToFit 시작 ===")
        println("원본 크기: ${originalWidth}x${originalHeight}")
        println("목표 크기: ${targetWidth}x${targetHeight}")
        
        // 원본 비율과 목표 비율 계산
        val originalRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val targetRatio = targetWidth.toFloat() / targetHeight.toFloat()
        
        println("비율 계산:")
        println("  원본 비율: $originalRatio")
        println("  목표 비율: $targetRatio")
        
        val (scaledWidth, scaledHeight) = if (originalRatio > targetRatio) {
            // 원본이 더 넓은 경우: 너비에 맞춰서 높이 조절 (프레임을 완전히 채움)
            val scaledHeight = (targetWidth / originalRatio).toInt()
            println("  원본이 더 넓음: 너비 기준으로 조절")
            Pair(targetWidth, scaledHeight)
        } else {
            // 원본이 더 높은 경우: 높이에 맞춰서 너비 조절 (프레임을 완전히 채움)
            val scaledWidth = (targetHeight * originalRatio).toInt()
            println("  원본이 더 높음: 높이 기준으로 조절")
            Pair(scaledWidth, targetHeight)
        }
        
        println("최종 스케일 크기: ${scaledWidth}x${scaledHeight}")
        println("=== scaleBitmapToFit 완료 ===")
        
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
    
    /**
     * Bitmap을 지정된 사각형 크기에 정확히 맞게 조절 (ContentScale.Crop 방식)
     * 비율을 유지하면서 목표 영역을 완전히 채우도록 크롭
     * @param bitmap 원본 Bitmap
     * @param targetWidth 목표 너비
     * @param targetHeight 목표 높이
     * @return 조절된 Bitmap
     */
    private fun scaleBitmapToFill(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        println("=== scaleBitmapToFill 시작 ===")
        println("원본 크기: ${originalWidth}x${originalHeight}")
        println("목표 크기: ${targetWidth}x${targetHeight}")
        
        // 원본 비트맵이 이미 목표 크기와 같으면 원본 반환 (재활용 방지)
        if (originalWidth == targetWidth && originalHeight == targetHeight) {
            println("크기가 동일하므로 원본 반환")
            return bitmap
        }
        
        // ContentScale.Crop 방식으로 비율을 유지하면서 크롭
        val scaledBitmap = scaleBitmapWithCrop(bitmap, targetWidth, targetHeight)
        
        println("최종 스케일 크기: ${scaledBitmap.width}x${scaledBitmap.height}")
        println("=== scaleBitmapToFill 완료 ===")
        
        return scaledBitmap
    }
    
    /**
     * 크롭 방향을 나타내는 열거형
     */
    enum class CropAlignment {
        CENTER,     // 중앙 크롭 (기본값)
        TOP,        // 상단 크롭
        BOTTOM,     // 하단 크롭
        LEFT,       // 좌측 크롭
        RIGHT       // 우측 크롭
    }
    
    /**
     * 비트맵을 비율을 유지하면서 목표 크기에 맞게 크롭
     * ContentScale.Crop과 동일한 동작
     * @param bitmap 원본 Bitmap
     * @param targetWidth 목표 너비
     * @param targetHeight 목표 높이
     * @param alignment 크롭 정렬 방식 (기본값: CENTER)
     * @return 크롭된 Bitmap
     */
    private fun scaleBitmapWithCrop(
        bitmap: Bitmap, 
        targetWidth: Int, 
        targetHeight: Int,
        alignment: CropAlignment = CropAlignment.CENTER
    ): Bitmap {
        val originalWidth = bitmap.width.toFloat()
        val originalHeight = bitmap.height.toFloat()
        val targetWidthFloat = targetWidth.toFloat()
        val targetHeightFloat = targetHeight.toFloat()
        
        // 원본과 목표 비율 계산
        val originalRatio = originalWidth / originalHeight
        val targetRatio = targetWidthFloat / targetHeightFloat
        
        val scale: Float
        val srcLeft: Float
        val srcTop: Float
        val srcRight: Float
        val srcBottom: Float
        
        if (originalRatio > targetRatio) {
            // 원본이 더 넓음 (세로를 기준으로 스케일, 가로 크롭)
            scale = targetHeightFloat / originalHeight
            val scaledWidth = originalWidth * scale
            val cropWidth = scaledWidth - targetWidthFloat
            val cropLeft = when (alignment) {
                CropAlignment.LEFT -> 0f
                CropAlignment.RIGHT -> cropWidth / scale
                else -> cropWidth / 2f / scale // CENTER, TOP, BOTTOM는 중앙 크롭
            }
            
            srcLeft = cropLeft
            srcTop = 0f
            srcRight = originalWidth - cropLeft
            srcBottom = originalHeight
        } else {
            // 원본이 더 높음 (가로를 기준으로 스케일, 세로 크롭)
            scale = targetWidthFloat / originalWidth
            val scaledHeight = originalHeight * scale
            val cropHeight = scaledHeight - targetHeightFloat
            val cropTop = when (alignment) {
                CropAlignment.TOP -> 0f
                CropAlignment.BOTTOM -> cropHeight / scale
                else -> cropHeight / 2f / scale // CENTER, LEFT, RIGHT는 중앙 크롭
            }
            
            srcLeft = 0f
            srcTop = cropTop
            srcRight = originalWidth
            srcBottom = originalHeight - cropTop
        }
        
        println("크롭 계산:")
        println("  스케일 비율: $scale")
        println("  소스 영역: ($srcLeft, $srcTop, $srcRight, $srcBottom)")
        
        // 크롭된 영역을 목표 크기로 스케일링
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            srcLeft.toInt(),
            srcTop.toInt(),
            (srcRight - srcLeft).toInt(),
            (srcBottom - srcTop).toInt()
        )
        
        val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true)
        
        // 중간 비트맵 메모리 해제
        if (croppedBitmap != bitmap && !croppedBitmap.isRecycled) {
            croppedBitmap.recycle()
        }
        
        return finalBitmap
    }
}
