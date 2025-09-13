package com.example.a4cut.data.repository

import com.example.a4cut.R
import com.example.a4cut.data.model.Frame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 프레임 데이터를 관리하는 Repository
 * 현재는 로컬 더미 데이터를 제공하지만, 추후 서버나 데이터베이스 연동 가능
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
     */
    private fun loadInitialFrames() {
        val initialFrames = listOf(
            Frame(
                id = 1,
                name = "KTX 기본",
                date = "25.07.18",
                station = "서울역",
                title = "기본 프레임",
                isPremium = false,
                drawableId = R.drawable.ktx_frame_signature
            ),
            Frame(
                id = 2,
                name = "부산 갈매기",
                date = "23.05.05",
                station = "부산역",
                title = "부산과 함께한 추억",
                isPremium = true,
                drawableId = R.drawable.ktx_frame_busan
            ),
            Frame(
                id = 3,
                name = "전주 한옥",
                date = "16.03.01",
                station = "전주역",
                title = "전주와 함께한 추억",
                isPremium = false,
                drawableId = R.drawable.ktx_frame_jeonju
            ),
            Frame(
                id = 4,
                name = "서울 네온",
                date = "12.08.20",
                station = "서울역",
                title = "서울과 함께한 추억",
                isPremium = true,
                drawableId = R.drawable.ktx_frame_seoul
            ),
            Frame(
                id = 5,
                name = "경주 역사",
                date = "10.04.15",
                station = "경주역",
                title = "경주와 함께한 추억",
                isPremium = false,
                drawableId = R.drawable.ktx_frame_gyeongju
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

