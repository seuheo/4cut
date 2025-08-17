package com.example.a4cut.data.database.dao

import androidx.room.*
import com.example.a4cut.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * 사진 데이터베이스 접근을 위한 DAO (Data Access Object)
 * Room 데이터베이스의 CRUD 작업을 담당
 */
@Dao
interface PhotoDao {
    
    /**
     * 모든 사진을 생성일 기준 내림차순으로 조회
     */
    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    /**
     * 사진 추가
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long
    
    /**
     * 사진 정보 업데이트
     */
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    /**
     * 사진 삭제
     */
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    /**
     * 즐겨찾기된 사진만 조회
     */
    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritePhotos(): Flow<List<PhotoEntity>>
    
    /**
     * 특정 위치의 사진 조회
     */
    @Query("SELECT * FROM photos WHERE location = :location ORDER BY createdAt DESC")
    fun getPhotosByLocation(location: String): Flow<List<PhotoEntity>>
    
    /**
     * 특정 프레임 타입의 사진 조회
     */
    @Query("SELECT * FROM photos WHERE frameType = :frameType ORDER BY createdAt DESC")
    fun getPhotosByFrameType(frameType: String): Flow<List<PhotoEntity>>
    
    /**
     * 전체 사진 개수 조회
     */
    @Query("SELECT COUNT(*) FROM photos")
    fun getPhotoCount(): Flow<Int>
    
    /**
     * 즐겨찾기된 사진 개수 조회
     */
    @Query("SELECT COUNT(*) FROM photos WHERE isFavorite = 1")
    fun getFavoritePhotoCount(): Flow<Int>
    
    /**
     * 최근 N개의 사진 조회
     */
    @Query("SELECT * FROM photos ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPhotos(limit: Int): Flow<List<PhotoEntity>>
    
    /**
     * 제목이나 위치로 사진 검색
     */
    @Query("SELECT * FROM photos WHERE title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPhotos(query: String): Flow<List<PhotoEntity>>
    
    // 새로운 확장 쿼리들 (미래 기능을 위한 준비)
    /**
     * 계절별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE season = :season ORDER BY createdAt DESC")
    fun getPhotosBySeason(season: String): Flow<List<PhotoEntity>>
    
    /**
     * 시간대별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE timeOfDay = :timeOfDay ORDER BY createdAt DESC")
    fun getPhotosByTimeOfDay(timeOfDay: String): Flow<List<PhotoEntity>>
    
    /**
     * 날씨별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE weather = :weather ORDER BY createdAt DESC")
    fun getPhotosByWeather(weather: String): Flow<List<PhotoEntity>>
    
    /**
     * 여행 목적별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE travelPurpose = :purpose ORDER BY createdAt DESC")
    fun getPhotosByTravelPurpose(purpose: String): Flow<List<PhotoEntity>>
    
    /**
     * 태그별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getPhotosByTag(tag: String): Flow<List<PhotoEntity>>
    
    /**
     * 연도별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE strftime('%Y', datetime(createdAt/1000, 'unixepoch')) = :year ORDER BY createdAt DESC")
    fun getPhotosByYear(year: String): Flow<List<PhotoEntity>>
    
    /**
     * 월별 사진 조회
     */
    @Query("SELECT * FROM photos WHERE strftime('%Y-%m', datetime(createdAt/1000, 'unixepoch')) = :yearMonth ORDER BY createdAt DESC")
    fun getPhotosByYearMonth(yearMonth: String): Flow<List<PhotoEntity>>
}
