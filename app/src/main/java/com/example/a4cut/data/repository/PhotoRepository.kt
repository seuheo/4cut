package com.example.a4cut.data.repository

import com.example.a4cut.data.database.dao.PhotoDao
import com.example.a4cut.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 사진 데이터 접근을 위한 Repository 클래스
 * 데이터베이스와 ViewModel 간의 중간 계층 역할
 */
class PhotoRepository(private val photoDao: PhotoDao) {
    
    /**
     * 모든 사진 조회
     */
    fun getAllPhotos(): Flow<List<PhotoEntity>> = photoDao.getAllPhotos()
    
    /**
     * 사진 추가
     */
    suspend fun insertPhoto(photo: PhotoEntity): Long = photoDao.insertPhoto(photo)
    
    /**
     * 사진 정보 업데이트
     */
    suspend fun updatePhoto(photo: PhotoEntity) = photoDao.updatePhoto(photo)
    
    /**
     * 사진 삭제
     */
    suspend fun deletePhoto(photo: PhotoEntity) = photoDao.deletePhoto(photo)
    
    /**
     * 즐겨찾기된 사진 조회
     */
    fun getFavoritePhotos(): Flow<List<PhotoEntity>> = photoDao.getFavoritePhotos()
    
    /**
     * 특정 위치의 사진 조회
     */
    fun getPhotosByLocation(location: String): Flow<List<PhotoEntity>> = 
        photoDao.getPhotosByLocation(location)
    
    /**
     * 특정 프레임 타입의 사진 조회
     */
    fun getPhotosByFrameType(frameType: String): Flow<List<PhotoEntity>> = 
        photoDao.getPhotosByFrameType(frameType)
    
    /**
     * 전체 사진 개수 조회
     */
    fun getPhotoCount(): Flow<Int> = photoDao.getPhotoCount()
    
    /**
     * 즐겨찾기된 사진 개수 조회
     */
    fun getFavoritePhotoCount(): Flow<Int> = photoDao.getFavoritePhotoCount()
    
    /**
     * 최근 N개의 사진 조회
     */
    fun getRecentPhotos(limit: Int): Flow<List<PhotoEntity>> = photoDao.getRecentPhotos(limit)
    
    /**
     * 사진 검색
     */
    fun searchPhotos(query: String): Flow<List<PhotoEntity>> = photoDao.searchPhotos(query)
    
    /**
     * KTX 4컷 사진 생성 (편의 메서드)
     */
    suspend fun createKTXPhoto(
        imagePath: String,
        title: String = "",
        location: String = "",
        frameType: String = "ktx_signature"
    ): Long {
        val photo = PhotoEntity(
            imagePath = imagePath,
            createdAt = System.currentTimeMillis(),
            title = title,
            location = location,
            frameType = frameType,
            colorTheme = "ktx_blue"
        )
        return insertPhoto(photo)
    }
    
    /**
     * 즐겨찾기 토글
     */
    suspend fun toggleFavorite(photo: PhotoEntity) {
        val updatedPhoto = photo.copy(isFavorite = !photo.isFavorite)
        updatePhoto(updatedPhoto)
    }
}
