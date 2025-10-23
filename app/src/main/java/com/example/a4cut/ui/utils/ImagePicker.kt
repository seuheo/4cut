package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        
        // 실제 이미지 로드 (샘플링 적용)
        val loadOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565 // 메모리 절약
            inDither = false // 디더링 비활성화로 성능 향상
            inTempStorage = ByteArray(16 * 1024) // 임시 스토리지 크기 제한
        }
        
        val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, loadOptions)
        } ?: throw IllegalStateException("이미지를 로드할 수 없습니다")
        
        // 정확한 크기로 리사이징 (고품질 리사이징)
        return if (bitmap.width != targetSize || bitmap.height != targetSize) {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
            // 원본 비트맵 메모리 해제
            if (bitmap != resizedBitmap) {
                bitmap.recycle()
            }
            resizedBitmap
        } else {
            bitmap
        }
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
