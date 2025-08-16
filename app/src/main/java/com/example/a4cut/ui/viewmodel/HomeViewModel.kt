package com.example.a4cut.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.repository.FrameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 홈 화면의 ViewModel
 * KTX 일러스트, 프레임 캐러셀, 캘린더 관련 상태와 로직을 관리
 */
class HomeViewModel : ViewModel() {
    
    private val frameRepository = FrameRepository()
    
    // 프레임 관련 상태
    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()
    
    // 캘린더 관련 상태
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()
    
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()
    
    private val _selectedDate = MutableStateFlow<Calendar?>(null)
    val selectedDate: StateFlow<Calendar?> = _selectedDate.asStateFlow()
    
    // UI 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadFrames()
    }
    
    /**
     * 프레임 목록 로드
     */
    private fun loadFrames() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // FrameRepository에서 프레임 데이터 수집
                frameRepository.frames.collect { frameList ->
                    _frames.value = frameList
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 이전 달로 이동
     */
    fun goToPreviousMonth() {
        if (_currentMonth.value == 0) {
            _currentMonth.value = 11
            _currentYear.value = _currentYear.value - 1
        } else {
            _currentMonth.value = _currentMonth.value - 1
        }
    }
    
    /**
     * 다음 달로 이동
     */
    fun goToNextMonth() {
        if (_currentMonth.value == 11) {
            _currentMonth.value = 0
            _currentYear.value = _currentYear.value + 1
        } else {
            _currentMonth.value = _currentMonth.value + 1
        }
    }
    
    /**
     * 날짜 선택
     */
    fun selectDate(date: Calendar) {
        _selectedDate.value = date
    }
    
    /**
     * 특별한 날인지 확인
     */
    fun isSpecialDay(date: Calendar): Boolean {
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DATE)
        
        // KTX 관련 특별한 날들
        return when {
            month == 4 && day == 1 -> true  // KTX 개통일 (예시)
            month == 12 && day == 25 -> true // 크리스마스
            month == 1 && day == 1 -> true   // 새해
            month == 8 && day == 15 -> true  // 광복절
            else -> false
        }
    }
    
    /**
     * 프레임 선택
     */
    fun selectFrame(frame: Frame) {
        // TODO: 선택된 프레임을 다른 ViewModel과 공유하는 로직 구현
        // 현재는 로깅만 수행
        println("선택된 프레임: ${frame.name}")
    }
    
    /**
     * 프리미엄 프레임만 가져오기
     */
    fun getPremiumFrames(): List<Frame> {
        return frameRepository.getPremiumFrames()
    }
}

