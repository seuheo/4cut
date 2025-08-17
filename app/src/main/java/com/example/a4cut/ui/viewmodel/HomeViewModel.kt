package com.example.a4cut.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.database.AppDatabase
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 새로운 홈 화면의 ViewModel
 * 포토로그 기능과 KTX 브랜드 특화 데이터 관리
 */
class HomeViewModel : ViewModel() {
    
    private var photoRepository: PhotoRepository? = null
    
    // UI 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 포토로그 데이터
    private val _photoLogs = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photoLogs: StateFlow<List<PhotoEntity>> = _photoLogs.asStateFlow()
    
    private val _photoCount = MutableStateFlow(0)
    val photoCount: StateFlow<Int> = _photoCount.asStateFlow()
    
    private val _favoritePhotoCount = MutableStateFlow(0)
    val favoritePhotoCount: StateFlow<Int> = _favoritePhotoCount.asStateFlow()
    
    /**
     * Context 설정 및 데이터베이스 초기화
     */
    fun setContext(context: Context) {
        val database = AppDatabase.getDatabase(context)
        photoRepository = PhotoRepository(database.photoDao())
        loadPhotoLogs()
    }
    
    /**
     * 포토로그 데이터 로드
     */
    private fun loadPhotoLogs() {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // 사진 목록과 개수 동시 로드
                    launch {
                        repository.getAllPhotos().collect { photos ->
                            _photoLogs.value = photos
                        }
                    }
                    
                    launch {
                        repository.getPhotoCount().collect { count ->
                            _photoCount.value = count
                        }
                    }
                    
                    launch {
                        repository.getFavoritePhotoCount().collect { count ->
                            _favoritePhotoCount.value = count
                        }
                    }
                    
                    clearError()
                } catch (e: Exception) {
                    _errorMessage.value = "포토로그 로드 실패: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite(photo: PhotoEntity) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.toggleFavorite(photo)
                } catch (e: Exception) {
                    _errorMessage.value = "즐겨찾기 변경 실패: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 사진 삭제
     */
    fun deletePhoto(photo: PhotoEntity) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.deletePhoto(photo)
                    // 삭제 후 목록 새로고침
                    loadPhotoLogs()
                } catch (e: Exception) {
                    _errorMessage.value = "사진 삭제 실패: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 검색 기능
     */
    fun searchPhotos(query: String) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.searchPhotos(query).collect { photos ->
                        _photoLogs.value = photos
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "검색 실패: ${e.message}"
                }
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
     * 데이터 새로고침
     */
    fun refreshData() {
        loadPhotoLogs()
    }
}

