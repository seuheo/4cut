package com.example.a4cut.data.repository

import com.example.a4cut.data.model.Frame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 프레임 데이터를 관리하는 Repository
 * Phase 4.3.2: KTX 프레임 시스템 확장 및 고품질 테마 프레임 추가
 */
class FrameRepository {
    
    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()
    
    init {
        // 초기 프레임 데이터 로드
        loadInitialFrames()
    }
    
    /**
     * 초기 프레임 데이터 로드
     * Phase 4.3.2: 신규 KTX 테마 프레임 2종 추가
     */
    private fun loadInitialFrames() {
        val initialFrames = listOf(
            Frame(
                id = 1,
                name = "KTX 기본",
                date = "25.07.18",
                station = "서울역",
                title = "기본 프레임",
                isPremium = false
            ),
            Frame(
                id = 2,
                name = "KTX 프리미엄",
                date = "23.05.05",
                station = "부산역",
                title = "프리미엄 프레임",
                isPremium = true
            ),
            Frame(
                id = 3,
                name = "KTX 특별",
                date = "16.03.01",
                station = "전주역",
                title = "특별 프레임",
                isPremium = false
            ),
            Frame(
                id = 4,
                name = "KTX 클래식",
                date = "12.08.20",
                station = "대구역",
                title = "클래식 프레임",
                isPremium = false
            ),
            // Phase 4.3.2: 신규 KTX 테마 프레임 추가
            Frame(
                id = 5,
                name = "KTX 봄날",
                date = "24.03.15",
                station = "여수엑스포역",
                title = "봄날 프레임",
                isPremium = false
            ),
            Frame(
                id = 6,
                name = "KTX 가을",
                date = "24.10.20",
                station = "강릉역",
                title = "가을 프레임",
                isPremium = true
            )
        )
        
        _frames.value = initialFrames
    }
    
    /**
     * 프레임 목록 가져오기
     */
    fun getFrames(): List<Frame> {
        return _frames.value
    }
    
    /**
     * 특정 ID의 프레임 가져오기
     */
    fun getFrameById(id: Int): Frame? {
        return _frames.value.find { it.id == id }
    }
    
    /**
     * 프리미엄 프레임만 가져오기
     */
    fun getPremiumFrames(): List<Frame> {
        return _frames.value.filter { it.isPremium }
    }
    
    /**
     * 테마별 프레임 가져오기
     * Phase 4.3.2: 테마 기반 프레임 필터링 기능 추가
     */
    fun getFramesByTheme(theme: String): List<Frame> {
        return when (theme.lowercase()) {
            "ktx" -> _frames.value.filter { it.name.contains("KTX", ignoreCase = true) }
            "premium" -> getPremiumFrames()
            "seasonal" -> _frames.value.filter { 
                it.name.contains("봄날", ignoreCase = true) || 
                it.name.contains("가을", ignoreCase = true) 
            }
            else -> _frames.value
        }
    }
    
    /**
     * 프레임 추가
     */
    fun addFrame(frame: Frame) {
        val currentFrames = _frames.value.toMutableList()
        currentFrames.add(frame)
        _frames.value = currentFrames
    }
    
    /**
     * 프레임 업데이트
     */
    fun updateFrame(frame: Frame) {
        val currentFrames = _frames.value.toMutableList()
        val index = currentFrames.indexOfFirst { it.id == frame.id }
        if (index != -1) {
            currentFrames[index] = frame
            _frames.value = currentFrames
        }
    }
}

