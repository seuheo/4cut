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
                name = "인생네컷 프레임",
                date = "25.01.13",
                station = "인생네컷",
                title = "인생네컷 프레임",
                isPremium = false,
                drawableId = R.drawable.life_4cut_frame
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

