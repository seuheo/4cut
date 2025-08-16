package com.example.a4cut.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.model.Photo
import com.example.a4cut.data.repository.FrameRepository
import com.example.a4cut.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

/**
 * 프레임 화면의 ViewModel
 * 사진 선택, 프레임 적용, 이미지 합성 등 핵심 로직을 담당
 */
class FrameViewModel : ViewModel() {
    
    private val frameRepository = FrameRepository()
    private val photoRepository = PhotoRepository()
    
    // 프레임 관련 상태
    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()
    
    private val _selectedFrame = MutableStateFlow<Frame?>(null)
    val selectedFrame: StateFlow<Frame?> = _selectedFrame.asStateFlow()
    
    // 사진 관련 상태
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    
    // UI 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadFrames()
        loadPhotos()
    }
    
    /**
     * 프레임 목록 로드
     */
    private fun loadFrames() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                frameRepository.frames.collect { frameList ->
                    _frames.value = frameList
                }
            } catch (e: Exception) {
                _errorMessage.value = "프레임 로드 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 사진 목록 로드
     */
    private fun loadPhotos() {
        viewModelScope.launch {
            try {
                photoRepository.photos.collect { photoList ->
                    _photos.value = photoList
                }
            } catch (e: Exception) {
                _errorMessage.value = "사진 로드 실패: ${e.message}"
            }
        }
    }
    
    /**
     * 프레임 선택
     */
    fun selectFrame(frame: Frame) {
        _selectedFrame.value = frame
        clearError()
    }
    
    /**
     * 사진 선택
     */
    fun selectPhoto(index: Int, uri: Uri) {
        if (index in 0..3) {
            val photo = Photo(uri = uri, id = "photo_$index")
            photoRepository.updatePhotoAt(index, photo)
            clearError()
        } else {
            _errorMessage.value = "잘못된 사진 인덱스입니다: $index"
        }
    }
    
    /**
     * 사진 제거
     */
    fun removePhoto(index: Int) {
        if (index in 0..3) {
            val emptyPhoto = Photo(id = "photo_$index")
            photoRepository.updatePhotoAt(index, emptyPhoto)
            clearError()
        }
    }
    
    /**
     * 이미지 합성 시작
     */
    fun startImageComposition() {
        if (_selectedFrame.value == null) {
            _errorMessage.value = "프레임을 선택해주세요"
            return
        }
        
        val hasPhotos = _photos.value.any { it.uri != null }
        if (!hasPhotos) {
            _errorMessage.value = "최소 한 장의 사진을 선택해주세요"
            return
        }
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // TODO: 실제 이미지 합성 로직 구현
                // 현재는 시뮬레이션만 수행
                kotlinx.coroutines.delay(2000) // 2초 대기
                
                // 성공 시 처리
                clearError()
            } catch (e: Exception) {
                _errorMessage.value = "이미지 합성 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 이미지 저장
     */
    fun saveImage() {
        if (_selectedFrame.value == null) {
            _errorMessage.value = "프레임을 선택해주세요"
            return
        }
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // TODO: 실제 이미지 저장 로직 구현
                kotlinx.coroutines.delay(1500) // 1.5초 대기
                
                clearError()
            } catch (e: Exception) {
                _errorMessage.value = "이미지 저장 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 인스타그램 공유
     */
    fun shareToInstagram() {
        if (_selectedFrame.value == null) {
            _errorMessage.value = "프레임을 선택해주세요"
            return
        }
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // TODO: 실제 인스타그램 공유 로직 구현
                kotlinx.coroutines.delay(1000) // 1초 대기
                
                clearError()
            } catch (e: Exception) {
                _errorMessage.value = "공유 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 에러 메시지 초기화
     */
    private fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 프리미엄 프레임만 가져오기
     */
    fun getPremiumFrames(): List<Frame> {
        return frameRepository.getPremiumFrames()
    }
    
    /**
     * 특정 ID의 프레임 가져오기
     */
    fun getFrameById(id: Int): Frame? {
        return frameRepository.getFrameById(id)
    }
}

