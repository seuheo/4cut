package com.example.a4cut.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.repository.FrameRepository
import com.example.a4cut.ui.utils.ImagePicker
import com.example.a4cut.ui.utils.PermissionHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 프레임 화면의 ViewModel
 * 사진 선택, 프레임 적용, 이미지 합성 등 핵심 로직을 담당
 * Phase 3: Bitmap 기반 이미지 처리 및 권한 관리 기능 추가
 */
class FrameViewModel : ViewModel() {
    
    private val frameRepository = FrameRepository()
    private var imagePicker: ImagePicker? = null
    private var permissionHelper: PermissionHelper? = null
    
    // 프레임 관련 상태
    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()
    
    private val _selectedFrame = MutableStateFlow<Frame?>(null)
    val selectedFrame: StateFlow<Frame?> = _selectedFrame.asStateFlow()
    
    // 사진 관련 상태 (Bitmap 기반)
    private val _photos = MutableStateFlow<List<Bitmap?>>(List(4) { null })
    val photos: StateFlow<List<Bitmap?>> = _photos.asStateFlow()
    
    // 권한 관련 상태
    private val _hasImagePermission = MutableStateFlow(false)
    val hasImagePermission: StateFlow<Boolean> = _hasImagePermission.asStateFlow()
    
    // UI 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 이미지 선택 결과
    private val _imagePickerResult = MutableStateFlow<List<Uri>?>(null)
    val imagePickerResult: StateFlow<List<Uri>?> = _imagePickerResult.asStateFlow()
    
    init {
        loadFrames()
    }
    
    /**
     * Context 설정 (권한 및 이미지 처리에 필요)
     */
    fun setContext(context: Context) {
        imagePicker = ImagePicker(context)
        permissionHelper = PermissionHelper(context)
        checkImagePermission()
    }
    
    /**
     * 이미지 권한 확인
     */
    private fun checkImagePermission() {
        permissionHelper?.let { helper ->
            _hasImagePermission.value = helper.isImagePermissionGranted()
        }
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
     * 프레임 선택
     */
    fun selectFrame(frame: Frame) {
        _selectedFrame.value = frame
        clearError()
    }
    
    /**
     * 사진 선택 (Bitmap 기반)
     */
    fun selectPhoto(index: Int, bitmap: Bitmap?) {
        if (index in 0..3) {
            val currentPhotos = _photos.value.toMutableList()
            currentPhotos[index] = bitmap
            _photos.value = currentPhotos
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
            val currentPhotos = _photos.value.toMutableList()
            // 기존 Bitmap 메모리 해제
            currentPhotos[index]?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            currentPhotos[index] = null
            _photos.value = currentPhotos
            clearError()
        }
    }
    
    /**
     * 사진 선택 토글 (있으면 제거, 없으면 추가 준비)
     */
    fun togglePhotoSelection(index: Int) {
        if (index in 0..3) {
            val currentPhotos = _photos.value.toMutableList()
            val currentPhoto = currentPhotos[index]
            
            if (currentPhoto != null) {
                // 사진이 있으면 제거
                if (!currentPhoto.isRecycled) {
                    currentPhoto.recycle()
                }
                currentPhotos[index] = null
            }
            // 사진이 없으면 추가 준비 (openImagePicker 호출 필요)
            
            _photos.value = currentPhotos
            clearError()
        } else {
            _errorMessage.value = "잘못된 사진 인덱스입니다: $index"
        }
    }
    
    /**
     * 이미지 선택기 열기
     */
    fun openImagePicker() {
        if (!_hasImagePermission.value) {
            _errorMessage.value = "갤러리 접근 권한이 필요합니다"
            return
        }
        
        // 이미지 선택 결과를 처리할 준비
        _imagePickerResult.value = emptyList()
    }
    
    /**
     * 이미지 선택 결과 처리
     */
    fun processSelectedImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                imagePicker?.let { picker ->
                    // 선택된 이미지들을 4컷 그리드에 맞게 처리
                    val processedBitmaps = picker.processImagesForGrid(uris, 512)
                    
                    // 기존 Bitmap들 메모리 해제
                    _photos.value.forEach { bitmap ->
                        bitmap?.let { 
                            if (!it.isRecycled) {
                                it.recycle()
                            }
                        }
                    }
                    
                    // 새로운 Bitmap들로 교체
                    _photos.value = processedBitmaps
                    clearError()
                } ?: run {
                    _errorMessage.value = "이미지 처리기를 초기화할 수 없습니다"
                }
            } catch (e: Exception) {
                _errorMessage.value = "이미지 처리 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
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
        
        val hasPhotos = _photos.value.any { it != null }
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
        
        val hasPhotos = _photos.value.any { it != null }
        if (!hasPhotos) {
            _errorMessage.value = "최소 한 장의 사진을 선택해주세요"
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
        
        val hasPhotos = _photos.value.any { it != null }
        if (!hasPhotos) {
            _errorMessage.value = "최소 한 장의 사진을 선택해주세요"
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
    
    /**
     * 권한 상태 업데이트
     */
    fun updatePermissionStatus(hasPermission: Boolean) {
        _hasImagePermission.value = hasPermission
    }
    
    /**
     * ViewModel 정리 시 Bitmap 메모리 해제
     */
    override fun onCleared() {
        super.onCleared()
        // 모든 Bitmap 메모리 해제
        _photos.value.forEach { bitmap ->
            bitmap?.let { 
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
        }
    }
}

