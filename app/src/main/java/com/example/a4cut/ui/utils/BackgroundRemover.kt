package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
// import com.gautam.background_remover.BackgroundRemover // 임시로 제거 (Kotlin 2.2.0 호환성 문제)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 사진 배경 제거 유틸리티
 * auto-background-remover 라이브러리를 사용하여 비트맵의 배경을 제거
 * 
 * 현재 상태: 라이브러리 임시 제거 (Kotlin 2.2.0 호환성 문제)
 * TODO: Kotlin 2.2.0 호환 버전이 출시되면 다시 활성화
 */
object BackgroundRemover {
    /**
     * 비트맵의 배경을 제거합니다. (suspend 함수)
     * AI 모델 실행은 리소스를 많이 사용하므로 IO 스레드에서 실행합니다.
     * 
     * @param context Android Context
     * @param bitmap 배경을 제거할 원본 비트맵
     * @return 배경이 제거된 비트맵 (현재는 원본 비트맵 반환 - 라이브러리 미사용)
     */
    suspend fun remove(context: Context, bitmap: Bitmap): Bitmap {
        // IO 스레드에서 작업을 실행
        return withContext(Dispatchers.IO) {
            try {
                // TODO: 라이브러리가 다시 추가되면 아래 코드 활성화
                // val resultBitmap = bitmap.removeBackground(context)
                // println("배경 제거 성공")
                // resultBitmap
                
                // 임시: 원본 비트맵 반환
                println("배경 제거 기능이 현재 비활성화되어 있습니다 (Kotlin 호환성 문제)")
                bitmap
            } catch (e: Exception) {
                // 실패 시 로그 출력 및 원본 비트맵 반환
                println("배경 제거 실패: ${e.message}")
                e.printStackTrace()
                bitmap
            }
        }
    }
}
