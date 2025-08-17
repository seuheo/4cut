package com.example.a4cut.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.repository.FrameRepository
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.database.AppDatabase
import com.example.a4cut.ui.utils.ImagePicker
import com.example.a4cut.ui.utils.PermissionHelper
import com.example.a4cut.ui.utils.ImageComposer
import com.example.a4cut.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 프레임 화면 ViewModel
 * Phase 4.3.2: UI 상태 관리 및 프레임 적용 기능 구현
 */
class FrameViewModel : ViewModel() {
    
    private val frameRepository = FrameRepository()
    
    // 1. UI 상태를 정의하는 데이터 클래스
    data class FrameUiState(
        val selectedImageUris: List<String> = emptyList(),
        val frames: List<Frame> = emptyList(),
        val selectedFrame: Frame? = null,
        val isProcessing: Boolean = false, // 이미지 합성 중 상태
        val composedImage: android.graphics.Bitmap? = null, // 합성된 이미지
        val errorMessage: String? = null // 에러 메시지
    )
    
    // 2. StateFlow로 UI 상태 관리
    private val _uiState = MutableStateFlow(FrameUiState())
    val uiState: StateFlow<FrameUiState> = _uiState.asStateFlow()
    
    // 기존 상태들 (하위 호환성 유지)
    private val _frames = frameRepository.frames
    val frames: StateFlow<List<Frame>> = _frames
    
    private val _selectedFrame = MutableStateFlow<Frame?>(null)
    val selectedFrame: StateFlow<Frame?> = _selectedFrame
    
    private val _photos = MutableStateFlow<List<android.graphics.Bitmap?>>(emptyList())
    val photos: StateFlow<List<android.graphics.Bitmap?>> = _photos
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing
    
    private val _composedImage = MutableStateFlow<android.graphics.Bitmap?>(null)
    val composedImage: StateFlow<android.graphics.Bitmap?> = _composedImage
    
    init {
        loadFrames()
    }
    
    // 3. 사용자 이벤트에 따라 상태를 변경하는 함수들
    fun onImagesSelected(uris: List<String>) {
        _uiState.update { it.copy(selectedImageUris = uris) }
        // 기존 상태도 업데이트 (하위 호환성)
        _photos.value = uris.map { null } // 실제 Bitmap은 나중에 로드
    }
    
    fun onFrameSelected(frame: Frame) {
        _uiState.update { it.copy(selectedFrame = frame) }
        // 기존 상태도 업데이트 (하위 호환성)
        _selectedFrame.value = frame
    }
    
    private fun loadFrames() {
        viewModelScope.launch {
            val frameList = frameRepository.getFrames()
            _uiState.update {
                it.copy(
                    frames = frameList,
                    selectedFrame = frameList.firstOrNull() // 기본 프레임 선택
                )
            }
            // 기존 상태도 업데이트 (하위 호환성)
            _selectedFrame.value = frameList.firstOrNull()
        }
    }
    
    fun startImageComposition() {
        _uiState.update { it.copy(isProcessing = true) }
        // 기존 상태도 업데이트 (하위 호환성)
        _isProcessing.value = true
        
        // TODO: Phase 4.3.2 Week 2에서 이미지 합성 로직 구현
        // ImageComposer.compose() 호출하여 백그라운드에서 실행
    }
    
    // 기존 함수들 (하위 호환성 유지)
    fun selectFrame(frame: Frame) {
        _selectedFrame.value = frame
        onFrameSelected(frame)
    }
    
    fun saveImage() {
        // TODO: 이미지 저장 로직 구현
    }
    
    fun shareToInstagram() {
        // TODO: 인스타그램 공유 로직 구현
    }
}

