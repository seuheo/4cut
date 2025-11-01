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
     * 특정 ID의 사진 조회
     */
    suspend fun getPhotoById(id: Int): PhotoEntity? = photoDao.getPhotoById(id)
    
    /**
     * 사진 정보 업데이트
     */
    suspend fun updatePhoto(photo: PhotoEntity) = photoDao.updatePhoto(photo)
    
    /**
     * 사진 삭제
     */
    suspend fun deletePhoto(photo: PhotoEntity) = photoDao.deletePhoto(photo)
    
    /**
     * 모든 사진 삭제
     */
    suspend fun deleteAllPhotos() = photoDao.deleteAllPhotos()
    
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
     * 고급 검색: 쿼리, 계절, 감정, 날씨 필터링을 포함한 검색
     */
    fun searchPhotosAdvanced(
        query: String,
        seasons: List<String>,
        moods: List<String>,
        weather: List<String>,
        sortBy: String
    ): Flow<List<PhotoEntity>> = photoDao.searchPhotosAdvanced(query, seasons, moods, weather, sortBy)
    
    /**
     * KTX 4컷 사진 생성 (편의 메서드) - 확장된 메타데이터 및 GPS 좌표 지원
     */
    suspend fun createKTXPhoto(
        imagePath: String,
        title: String = "",
        location: String = "",
        latitude: Double? = null,
        longitude: Double? = null,
        frameType: String = "ktx_signature",
        tags: String = "",
        description: String = "",
        weather: String = "",
        mood: String = "",
        companions: String = "",
        travelPurpose: String = "",
        season: String = "",
        timeOfDay: String = "",
        videoPath: String? = null
    ): Long {
        val photo = PhotoEntity(
            imagePath = imagePath,
            createdAt = System.currentTimeMillis(),
            title = title,
            location = location,
            latitude = latitude,
            longitude = longitude,
            frameType = frameType,
            colorTheme = "ktx_blue",
            tags = tags,
            description = description,
            weather = weather,
            mood = mood,
            companions = companions,
            travelPurpose = travelPurpose,
            season = season,
            timeOfDay = timeOfDay,
            videoPath = videoPath
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
    
    /**
     * 특정 PhotoEntity의 videoPath 필드만 업데이트 (백그라운드 동영상 생성 완료 후 사용)
     * @param photoId 업데이트할 사진 ID
     * @param videoPath 업데이트할 동영상 경로 (null 가능)
     */
    suspend fun updateVideoPath(photoId: Int, videoPath: String?) {
        val photo = getPhotoById(photoId)
        if (photo != null) {
            val updatedPhoto = photo.copy(videoPath = videoPath)
            updatePhoto(updatedPhoto)
        } else {
            throw IllegalStateException("PhotoEntity를 찾을 수 없습니다: photoId=$photoId")
        }
    }
    
    // 새로운 확장 메서드들 (미래 기능을 위한 준비)
    /**
     * 계절별 사진 조회
     */
    fun getPhotosBySeason(season: String): Flow<List<PhotoEntity>> = photoDao.getPhotosBySeason(season)
    
    /**
     * 시간대별 사진 조회
     */
    fun getPhotosByTimeOfDay(timeOfDay: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByTimeOfDay(timeOfDay)
    
    /**
     * 날씨별 사진 조회
     */
    fun getPhotosByWeather(weather: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByWeather(weather)
    
    /**
     * 여행 목적별 사진 조회
     */
    fun getPhotosByTravelPurpose(purpose: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByTravelPurpose(purpose)
    
    /**
     * 태그별 사진 조회
     */
    fun getPhotosByTag(tag: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByTag(tag)
    
    /**
     * 연도별 사진 조회
     */
    fun getPhotosByYear(year: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByYear(year)
    
    /**
     * 월별 사진 조회
     */
    fun getPhotosByYearMonth(yearMonth: String): Flow<List<PhotoEntity>> = photoDao.getPhotosByYearMonth(yearMonth)
    
    /**
     * 특정 날짜 범위의 사진 조회 (지도 표시용)
     */
    suspend fun getPhotosByDateRange(startTime: Long, endTime: Long): List<PhotoEntity> = 
        photoDao.getPhotosByDateRange(startTime, endTime)
    
    /**
     * 특정 날짜 범위의 사진 조회 (Flow 버전)
     */
    fun getPhotosByDateRangeFlow(startTime: Long, endTime: Long): Flow<List<PhotoEntity>> = 
        photoDao.getPhotosByDateRangeFlow(startTime, endTime)
    
    /**
     * ✅ MVP Ver2: 특정 연도에 방문한 고유한 역 이름 목록 조회 (노선도 캠페인 기능용)
     */
    suspend fun getVisitedLocationsByYear(year: String): List<String> = 
        photoDao.getVisitedLocationsByYear(year)
}
