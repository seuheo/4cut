package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 이미지 선택 및 처리를 위한 클래스
 * 갤러리에서 이미지를 선택하고 4컷 그리드에 맞게 최적화
 */
class ImagePicker(private val context: Context) {
    
    /**
     * 갤러리에서 이미지 선택을 위한 ActivityResultContracts 생성
     */
    fun createImagePickerContract(): ActivityResultContracts.GetMultipleContents {
        return ActivityResultContracts.GetMultipleContents()
    }
    
    /**
     * 선택된 이미지들을 4컷 그리드에 맞게 처리
     * @param uris 선택된 이미지 URI 목록
     * @param targetSize 각 셀의 목표 크기 (정사각형)
     * @return 처리된 Bitmap 목록
     */
    suspend fun processImagesForGrid(
        uris: List<Uri>,
        targetSize: Int = 512 // 기본 512x512px
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val processedImages = mutableListOf<Bitmap>()
        
        uris.take(4).forEach { uri ->
            try {
                val bitmap = loadAndResizeImage(uri, targetSize)
                processedImages.add(bitmap)
            } catch (e: Exception) {
                // 에러 발생 시 기본 이미지 생성
                val defaultBitmap = createDefaultImage(targetSize)
                processedImages.add(defaultBitmap)
            }
        }
        
        // 4개가 되지 않으면 기본 이미지로 채움
        while (processedImages.size < 4) {
            val defaultBitmap = createDefaultImage(targetSize)
            processedImages.add(defaultBitmap)
        }
        
        processedImages
    }
    
    /**
     * 이미지를 로드하고 목표 크기로 리사이징
     * 메모리 최적화를 위해 원본 이미지를 메모리에 그대로 올리지 않음
     * 안전한 이미지 디코딩을 위한 추가 옵션 적용
     */
    private fun loadAndResizeImage(uri: Uri, targetSize: Int): Bitmap {
        // 이미지 크기만 먼저 확인
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }
        
        // 적절한 샘플링 크기 계산 (더 공격적인 샘플링)
        val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, targetSize)
        
        // 실제 이미지 로드 (안전한 디코딩 옵션 적용)
        val loadOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888 // 풀 컬러 지원 (RGB_565는 색상 손실 가능)
            inDither = false // 디더링 비활성화로 성능 향상
            inTempStorage = ByteArray(16 * 1024) // 임시 스토리지 크기 제한
            inPurgeable = false // 퍼지 가능 비활성화 (안정성 향상)
            inInputShareable = false // 입력 스트림 공유 비활성화 (안정성 향상)
            inScaled = false // 자동 스케일링 비활성화
            inPremultiplied = false // 프리멀티플라이드 비활성화
        }
        
        val bitmap = try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, loadOptions)
            } ?: throw IllegalStateException("이미지를 로드할 수 없습니다")
        } catch (e: Exception) {
            // 디코딩 실패 시 더 안전한 옵션으로 재시도
            println("ImagePicker: 이미지 디코딩 실패, 안전한 옵션으로 재시도: ${e.message}")
            
            // 여러 단계의 안전한 옵션으로 재시도
            val safeOptionsList = listOf(
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize * 2
                    inPreferredConfig = Bitmap.Config.ARGB_8888 // 풀 컬러 지원
                    inScaled = false
                },
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize * 4
                    inPreferredConfig = Bitmap.Config.ARGB_8888 // 풀 컬러 지원
                    inScaled = false
                },
                BitmapFactory.Options().apply {
                    inSampleSize = sampleSize * 8
                    inPreferredConfig = Bitmap.Config.ARGB_8888 // 풀 컬러 지원
                    inScaled = false
                }
            )
            
            var decodedBitmap: Bitmap? = null
            for (safeOptions in safeOptionsList) {
                try {
                    decodedBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream, null, safeOptions)
                    }
                    if (decodedBitmap != null) {
                        println("ImagePicker: 안전한 옵션으로 이미지 디코딩 성공")
                        break
                    }
                } catch (e2: Exception) {
                    println("ImagePicker: 안전한 옵션도 실패: ${e2.message}")
                    continue
                }
            }
            
            decodedBitmap ?: throw IllegalStateException("모든 디코딩 옵션이 실패했습니다")
        }
        
        // Bitmap 유효성 검증
        println("ImagePicker: 디코딩된 Bitmap - 크기: ${bitmap.width}x${bitmap.height}, isRecycled: ${bitmap.isRecycled}, config: ${bitmap.config}")
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            println("ImagePicker: 경고! Bitmap 크기가 유효하지 않음")
            bitmap.recycle()
            throw IllegalStateException("유효하지 않은 Bitmap 크기: ${bitmap.width}x${bitmap.height}")
        }
        
        if (bitmap.isRecycled) {
            println("ImagePicker: 경고! Bitmap이 이미 재활용됨")
            throw IllegalStateException("Bitmap이 이미 재활용되었습니다")
        }
        
        // 정확한 크기로 리사이징 (Canvas를 사용한 고품질 리사이징)
        val finalBitmap = if (bitmap.width != targetSize || bitmap.height != targetSize) {
            println("ImagePicker: 리사이징 시작 - 원본: ${bitmap.width}x${bitmap.height} -> 목표: ${targetSize}x${targetSize}")
            
            // Canvas를 사용한 고품질 리사이징 (createScaledBitmap 대신)
            val resizedBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(resizedBitmap)
            
            // 고품질 리샘플링을 위한 Paint 설정
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            paint.isDither = true // 디더링 활성화로 색상 품질 향상
            
            // Matrix를 사용한 스케일링
            val matrix = Matrix()
            val scaleX = targetSize.toFloat() / bitmap.width
            val scaleY = targetSize.toFloat() / bitmap.height
            val scale = scaleX.coerceAtMost(scaleY) // 비율 유지 (Crop)
            
            // 중앙 정렬을 위한 이동
            val dx = (targetSize - bitmap.width * scale) / 2f
            val dy = (targetSize - bitmap.height * scale) / 2f
            
            matrix.postScale(scale, scale)
            matrix.postTranslate(dx, dy)
            
            canvas.drawBitmap(bitmap, matrix, paint)
            canvas.setBitmap(null) // Canvas와 Bitmap 연결 해제
            
            println("ImagePicker: Canvas 리사이징 완료 - 결과: ${resizedBitmap.width}x${resizedBitmap.height}, config: ${resizedBitmap.config}")
            
            // 리사이징된 Bitmap 유효성 검증
            if (resizedBitmap.width != targetSize || resizedBitmap.height != targetSize || resizedBitmap.isRecycled) {
                println("ImagePicker: 오류! 리사이징된 Bitmap이 유효하지 않음")
                resizedBitmap.recycle()
                bitmap.recycle()
                throw IllegalStateException("리사이징된 Bitmap이 유효하지 않음")
            }
            
            // 원본 비트맵 메모리 해제
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            
            resizedBitmap
        } else {
            println("ImagePicker: 리사이징 불필요 - 이미 목표 크기와 동일")
            bitmap
        }
        
        println("ImagePicker: 최종 Bitmap - 크기: ${finalBitmap.width}x${finalBitmap.height}, isRecycled: ${finalBitmap.isRecycled}, config: ${finalBitmap.config}")
        return finalBitmap
    }
    
    /**
     * 샘플링 크기 계산 (메모리 최적화)
     * 더 공격적인 샘플링으로 메모리 사용량 감소
     */
    private fun calculateSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        targetSize: Int
    ): Int {
        var sampleSize = 1
        // 더 공격적인 샘플링으로 메모리 사용량 감소
        while (originalWidth / sampleSize > targetSize * 1.5 || originalHeight / sampleSize > targetSize * 1.5) {
            sampleSize *= 2
        }
        return sampleSize
    }
    
    /**
     * 기본 이미지 생성 (사진이 4개 미만일 때)
     */
    private fun createDefaultImage(size: Int): Bitmap {
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            // 기본 색상으로 채우기 (KTX 브랜드 컬러)
            val pixels = IntArray(size * size) { 
                0xFFE5E7EB.toInt() // KTX 실버
            }
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
    }
    
    /**
     * 이미지 메모리 해제
     */
    fun recycleBitmaps(bitmaps: List<Bitmap>) {
        bitmaps.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
    
    /**
     * 이미지 정보 가져오기 (파일명, 크기 등)
     */
    fun getImageInfo(uri: Uri): ImageInfo? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_ADDED
                ),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    ImageInfo(
                        name = cursor.getString(0) ?: "Unknown",
                        size = cursor.getLong(1),
                        dateAdded = cursor.getLong(2)
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 이미지 정보 데이터 클래스
 */
data class ImageInfo(
    val name: String,
    val size: Long,
    val dateAdded: Long
)
