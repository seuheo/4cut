package com.example.a4cut

import android.app.Application
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import android.util.Log

/**
 * Application 클래스
 * 앱 전역 설정 및 Coil ImageLoader 최적화
 */
class AppApplication : Application() {
    
    companion object {
        lateinit var imageLoader: ImageLoader
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Coil ImageLoader 최적화 설정
        setupImageLoader()
        
        Log.d("AppApplication", "Application 초기화 완료")
    }
    
    /**
     * Coil ImageLoader 최적화 설정
     * 메모리 및 디스크 캐시 최적화로 성능 향상
     */
    private fun setupImageLoader() {
        imageLoader = ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // 앱 메모리의 25% 사용
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB 디스크 캐시
                    .build()
            }
            .respectCacheHeaders(false) // 캐시 헤더 무시로 성능 향상
            .crossfade(true) // 부드러운 전환 효과
            .crossfade(300) // 300ms 전환 시간
            .apply {
                // 디버그 모드에서만 로깅 활성화 (개발 중에는 항상 활성화)
                logger(DebugLogger())
            }
            .build()
            
        Log.d("AppApplication", "Coil ImageLoader 최적화 설정 완료")
    }
}
