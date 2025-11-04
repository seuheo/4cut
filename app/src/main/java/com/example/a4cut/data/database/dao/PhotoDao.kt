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
     * 특정 ID의 사진 조회
     */
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Int): PhotoEntity?
    
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
     * 모든 사진 삭제
     */
    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()
    
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
    
    /**
     * 고급 검색: 쿼리, 계절, 감정, 날씨 필터링을 포함한 검색
     */
    @Query("SELECT * FROM photos WHERE " +
           "(:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') " +
           "AND (:seasons IS NULL OR :seasons = '[]' OR season IN (:seasons)) " +
           "AND (:moods IS NULL OR :moods = '[]' OR mood IN (:moods)) " +
           "AND (:weather IS NULL OR :weather = '[]' OR weather IN (:weather)) " +
           "ORDER BY " +
           "CASE " +
           "WHEN :sortBy = 'latest' THEN createdAt END DESC, " +
           "CASE " +
           "WHEN :sortBy = 'oldest' THEN createdAt END ASC, " +
           "CASE " +
           "WHEN :sortBy = 'favorite' THEN isFavorite END DESC, " +
           "CASE " +
           "WHEN :sortBy = 'location' THEN location END ASC, " +
           "CASE " +
           "WHEN :sortBy = 'season' THEN season END ASC, " +
           "CASE " +
           "WHEN :sortBy = 'mood' THEN mood END ASC"
    )
    fun searchPhotosAdvanced(
        query: String,
        seasons: List<String>,
        moods: List<String>,
        weather: List<String>,
        sortBy: String
    ): Flow<List<PhotoEntity>>
    
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
    
    /**
     * 특정 날짜 범위의 사진 조회 (지도 표시용)
     */
    @Query("SELECT * FROM photos WHERE createdAt BETWEEN :startTime AND :endTime ORDER BY createdAt DESC")
    suspend fun getPhotosByDateRange(startTime: Long, endTime: Long): List<PhotoEntity>
    
    /**
     * 특정 날짜 범위의 사진 조회 (Flow 버전)
     */
    @Query("SELECT * FROM photos WHERE createdAt BETWEEN :startTime AND :endTime ORDER BY createdAt DESC")
    fun getPhotosByDateRangeFlow(startTime: Long, endTime: Long): Flow<List<PhotoEntity>>
    
    /**
     * ✅ MVP Ver2: 특정 연도에 방문한 고유한 역 이름 목록 조회 (노선도 캠페인 기능용)
     */
    @Query("SELECT DISTINCT location FROM photos WHERE strftime('%Y', datetime(createdAt/1000, 'unixepoch')) = :year AND location != '' AND location IS NOT NULL")
    suspend fun getVisitedLocationsByYear(year: String): List<String>
}
