package com.example.a4cut.data.repository

import com.example.a4cut.data.database.dao.PhotoDao
import com.example.a4cut.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 사진 데이터에 대한 비즈니스 로직을 담당하는 Repository
 * 트렌디한 기능과 KTX 브랜드 특화 로직 포함
 */
class PhotoRepository(
    private val photoDao: PhotoDao
) {
    
    // 기본 CRUD 작업
    fun getAllPhotos(): Flow<List<PhotoEntity>> = photoDao.getAllPhotos()
    
    suspend fun insertPhoto(photo: PhotoEntity): Long = photoDao.insertPhoto(photo)
    
    suspend fun updatePhoto(photo: PhotoEntity) = photoDao.updatePhoto(photo)
    
    suspend fun deletePhoto(photo: PhotoEntity) = photoDao.deletePhoto(photo)
    
    // 트렌디한 기능
    fun getFavoritePhotos(): Flow<List<PhotoEntity>> = photoDao.getFavoritePhotos()
    
    fun getPhotosByLocation(location: String): Flow<List<PhotoEntity>> = 
        photoDao.getPhotosByLocation(location)
    
    fun getPhotosByFrameType(frameType: String): Flow<List<PhotoEntity>> = 
        photoDao.getPhotosByFrameType(frameType)
    
    // 통계 및 분석
    fun getPhotoCount(): Flow<Int> = photoDao.getPhotoCount()
    
    fun getFavoritePhotoCount(): Flow<Int> = photoDao.getFavoritePhotoCount()
    
    // 홈 화면용 최근 사진
    fun getRecentPhotos(limit: Int = 10): Flow<List<PhotoEntity>> = 
        photoDao.getRecentPhotos(limit)
    
    // 검색 기능
    fun searchPhotos(query: String): Flow<List<PhotoEntity>> = 
        photoDao.searchPhotos(query)
    
    // KTX 브랜드 특화 기능
    suspend fun createKTXPhoto(
        imagePath: String,
        title: String? = null,
        location: String? = null,
        tags: String? = null
    ): Long {
        val photo = PhotoEntity(
            imagePath = imagePath,
            title = title,
            location = location,
            tags = tags,
            frameType = "ktx_signature",
            colorTheme = "ktx_blue"
        )
        return insertPhoto(photo)
    }
    
    // 즐겨찾기 토글
    suspend fun toggleFavorite(photo: PhotoEntity) {
        val updatedPhoto = photo.copy(isFavorite = !photo.isFavorite)
        updatePhoto(updatedPhoto)
    }
}
