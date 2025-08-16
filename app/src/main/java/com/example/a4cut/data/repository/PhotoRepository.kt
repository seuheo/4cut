package com.example.a4cut.data.repository

import com.example.a4cut.data.model.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 사진 데이터를 관리하는 Repository
 */
class PhotoRepository {
    
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    
    /**
     * 사진 목록 업데이트
     */
    fun updatePhotos(newPhotos: List<Photo>) {
        _photos.value = newPhotos
    }
    
    /**
     * 특정 위치의 사진 업데이트
     */
    fun updatePhotoAt(index: Int, photo: Photo) {
        val currentPhotos = _photos.value.toMutableList()
        if (index < currentPhotos.size) {
            currentPhotos[index] = photo
            _photos.value = currentPhotos
        }
    }
}
