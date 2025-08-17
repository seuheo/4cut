package com.example.a4cut.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.database.entity.PhotoEntity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 사진 상세 보기 화면을 위한 ViewModel
 * 사진 정보 표시, 편집, 삭제, 즐겨찾기 기능을 관리
 */
class PhotoDetailViewModel : ViewModel() {
    
    // UI 상태 관리
    private val _uiState = MutableStateFlow(PhotoDetailUiState())
    val uiState: StateFlow<PhotoDetailUiState> = _uiState.asStateFlow()
    
    // 편집 모드 상태
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()
    
    // 편집 중인 데이터
    private val _editData = MutableStateFlow(EditData())
    val editData: StateFlow<EditData> = _editData.asStateFlow()
    
    /**
     * 사진 정보 설정
     */
    fun setPhoto(photo: PhotoEntity) {
        _uiState.value = PhotoDetailUiState(
            photo = photo,
            isLoading = false
        )
        _editData.value = EditData(
            title = photo.title,
            description = photo.description,
            tags = photo.tags
        )
    }
    
    /**
     * 편집 모드 토글
     */
    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
        if (!_isEditMode.value) {
            // 편집 모드 종료 시 원본 데이터로 복원
            _uiState.value.photo?.let { photo ->
                _editData.value = EditData(
                    title = photo.title,
                    description = photo.description,
                    tags = photo.tags
                )
            }
        }
    }
    
    /**
     * 편집 데이터 업데이트
     */
    fun updateEditData(
        title: String = _editData.value.title,
        description: String = _editData.value.description,
        tags: String = _editData.value.tags
    ) {
        _editData.value = _editData.value.copy(
            title = title,
            description = description,
            tags = tags
        )
    }
    
    /**
     * 편집 내용 저장
     */
    fun saveEditData() {
        val currentPhoto = _uiState.value.photo ?: return
        val editData = _editData.value
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val updatedPhoto = currentPhoto.copy(
                    title = editData.title,
                    description = editData.description,
                    tags = editData.tags
                )
                
                // TODO: Repository를 통한 실제 업데이트 구현
                // photoRepository.updatePhoto(updatedPhoto)
                
                _uiState.value = _uiState.value.copy(
                    photo = updatedPhoto,
                    isLoading = false,
                    message = "저장되었습니다"
                )
                
                _isEditMode.value = false
                
                // 메시지 자동 제거
                clearMessage()
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "저장 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite() {
        val currentPhoto = _uiState.value.photo ?: return
        
        viewModelScope.launch {
            try {
                // TODO: Repository를 통한 실제 즐겨찾기 토글 구현
                // photoRepository.toggleFavorite(currentPhoto)
                
                // UI 상태 업데이트
                val updatedPhoto = currentPhoto.copy(isFavorite = !currentPhoto.isFavorite)
                _uiState.value = _uiState.value.copy(photo = updatedPhoto)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "즐겨찾기 변경 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 사진 삭제
     */
    fun deletePhoto() {
        val currentPhoto = _uiState.value.photo ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // TODO: Repository를 통한 실제 삭제 구현
                // photoRepository.deletePhoto(currentPhoto)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shouldNavigateBack = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "삭제 중 오류가 발생했습니다: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 메시지 제거
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            errorMessage = null
        )
    }
    
    /**
     * 뒤로가기 네비게이션 상태 초기화
     */
    fun resetNavigationState() {
        _uiState.value = _uiState.value.copy(shouldNavigateBack = false)
    }
}

/**
 * 사진 상세 보기 UI 상태
 */
data class PhotoDetailUiState(
    val photo: PhotoEntity? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null,
    val shouldNavigateBack: Boolean = false
)

/**
 * 편집 데이터 모델
 */
data class EditData(
    val title: String = "",
    val description: String = "",
    val tags: String = ""
)
