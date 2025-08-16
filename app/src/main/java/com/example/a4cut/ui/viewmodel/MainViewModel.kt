package com.example.a4cut.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.model.Photo
import com.example.a4cut.data.repository.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 메인 화면의 ViewModel
 */
class MainViewModel : ViewModel() {
    
    private val repository = PhotoRepository()
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        // 초기 4컷 사진 데이터 생성 (플레이스홀더)
        val initialPhotos = List(4) { index ->
            Photo(id = "photo_$index")
        }
        repository.updatePhotos(initialPhotos)
        
        // UI 상태 업데이트
        viewModelScope.launch {
            repository.photos.collect { photos ->
                _uiState.value = _uiState.value.copy(photos = photos)
            }
        }
    }
    
    /**
     * 사진 선택 처리
     */
    fun selectPhoto(index: Int, uri: android.net.Uri) {
        val photo = Photo(uri = uri, id = "photo_$index")
        repository.updatePhotoAt(index, photo)
    }
    
    /**
     * 사진 저장 처리
     */
    fun savePhotos() {
        // TODO: 이미지 합성 및 저장 로직 구현
        _uiState.value = _uiState.value.copy(isSaving = true)
        
        viewModelScope.launch {
            // 저장 완료 후 상태 업데이트
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
    
    /**
     * 인스타그램 공유 처리
     */
    fun shareToInstagram() {
        // TODO: 인스타그램 공유 로직 구현
        _uiState.value = _uiState.value.copy(isSharing = true)
        
        viewModelScope.launch {
            // 공유 완료 후 상태 업데이트
            _uiState.value = _uiState.value.copy(isSharing = false)
        }
    }
}

/**
 * 메인 UI 상태
 */
data class MainUiState(
    val photos: List<Photo> = emptyList(),
    val isSaving: Boolean = false,
    val isSharing: Boolean = false
)
