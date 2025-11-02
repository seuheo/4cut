package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.a4cut.ui.utils.ImagePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 사진 배경 제거 유틸리티
 * auto-background-remover 라이브러리를 사용하여 비트맵의 배경을 제거
 */
object BackgroundRemover {
    /**
     * 비트맵의 배경을 제거하여 새로운 비트맵 반환
     * 
     * @param bitmap 배경을 제거할 원본 비트맵
     * @param context Android Context (필요한 경우)
     * @return 배경이 제거된 비트맵 (배경 제거 실패 시 원본 비트맵 반환)
     */
    suspend fun removeBackground(
        bitmap: Bitmap,
        context: Context
    ): Bitmap = withContext(Dispatchers.IO) {
        try {
            // auto-background-remover 라이브러리 사용
            // 주의: 라이브러리 API를 실제 문서에 맞게 수정 필요
            // val remover = AutoBackgroundRemover(context)
            // val result = remover.removeBackground(bitmap)
            // result ?: bitmap
            
            // 임시 구현: 라이브러리 추가 후 실제 API 확인 필요
            // 현재는 원본 비트맵 반환 (배경 제거 기능은 라이브러리 연동 완료 후 활성화)
            bitmap
        } catch (e: Exception) {
            // 배경 제거 실패 시 원본 비트맵 반환
            e.printStackTrace()
            bitmap
        }
    }
    
    /**
     * URI에서 이미지를 로드하고 배경을 제거
     * 
     * @param uri 이미지 URI
     * @param context Android Context
     * @return 배경이 제거된 비트맵
     */
    suspend fun removeBackgroundFromUri(
        uri: Uri,
        context: Context
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // URI에서 비트맵 로드
            val bitmap = ImagePicker.decodeBitmapFromUri(context, uri, 2048)
            if (bitmap != null) {
                removeBackground(bitmap, context)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
