package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import com.gautam.background_remover.BackgroundRemover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 사진 배경 제거 유틸리티
 * auto-background-remover 라이브러리를 사용하여 비트맵의 배경을 제거
 */
object BackgroundRemover {
    /**
     * 비트맵의 배경을 제거합니다. (suspend 함수)
     * AI 모델 실행은 리소스를 많이 사용하므로 IO 스레드에서 실행합니다.
     * 
     * @param context Android Context
     * @param bitmap 배경을 제거할 원본 비트맵
     * @return 배경이 제거된 비트맵 (배경 제거 실패 시 원본 비트맵 반환)
     */
    suspend fun remove(context: Context, bitmap: Bitmap): Bitmap {
        // IO 스레드에서 작업을 실행
        return withContext(Dispatchers.IO) {
            try {
                // 실제 라이브러리 API 호출
                // .removeBackground()는 라이브러리가 제공하는 Bitmap의 확장 함수입니다.
                val resultBitmap = bitmap.removeBackground(context)
                println("배경 제거 성공")
                resultBitmap
            } catch (e: Exception) {
                // 실패 시 로그 출력 및 원본 비트맵 반환
                println("배경 제거 실패: ${e.message}")
                e.printStackTrace()
                bitmap
            }
        }
    }
}
