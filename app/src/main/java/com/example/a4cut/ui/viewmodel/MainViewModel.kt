package com.example.a4cut.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 메인 앱의 ViewModel
 * 앱의 전반적인 상태와 네비게이션 상태만 관리
 * 구체적인 화면 로직은 각 화면별 ViewModel에서 처리
 */
class MainViewModel : ViewModel() {
    
    // 네비게이션 관련 상태
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()
    
    // 앱 전반 상태
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady: StateFlow<Boolean> = _isAppReady.asStateFlow()
    
    // 에러 상태
    private val _globalErrorMessage = MutableStateFlow<String?>(null)
    val globalErrorMessage: StateFlow<String?> = _globalErrorMessage.asStateFlow()
    
    init {
        // 앱 초기화 완료
        _isAppReady.value = true
    }
    
    /**
     * 현재 화면 변경
     */
    fun setCurrentScreen(screen: String) {
        _currentScreen.value = screen
    }
    
    /**
     * 전역 에러 메시지 설정
     */
    fun setGlobalError(message: String) {
        _globalErrorMessage.value = message
    }
    
    /**
     * 전역 에러 메시지 초기화
     */
    fun clearGlobalError() {
        _globalErrorMessage.value = null
    }
    
    /**
     * 앱 상태 확인
     */
    fun checkAppStatus(): Boolean {
        return _isAppReady.value
    }
}
