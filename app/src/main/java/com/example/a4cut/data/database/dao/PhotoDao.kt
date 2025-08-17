package com.example.a4cut.data.database.dao

import androidx.room.*
import com.example.a4cut.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

/**
 * PhotoEntity에 대한 데이터베이스 접근 객체 (DAO)
 * 트렌디한 기능과 KTX 브랜드 특화 쿼리 제공
 */
@Dao
interface PhotoDao {
    
    // 기본 CRUD 작업
    @Query("SELECT * FROM photos ORDER BY createdAt DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    // 트렌디한 기능을 위한 쿼리
    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoritePhotos(): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE location LIKE '%' || :location || '%' ORDER BY createdAt DESC")
    fun getPhotosByLocation(location: String): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE frameType = :frameType ORDER BY createdAt DESC")
    fun getPhotosByFrameType(frameType: String): Flow<List<PhotoEntity>>
    
    // 통계 및 분석
    @Query("SELECT COUNT(*) FROM photos")
    fun getPhotoCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM photos WHERE isFavorite = 1")
    fun getFavoritePhotoCount(): Flow<Int>
    
    // 최근 사진 조회 (홈 화면용)
    @Query("SELECT * FROM photos ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPhotos(limit: Int): Flow<List<PhotoEntity>>
    
    // 검색 기능
    @Query("SELECT * FROM photos WHERE title LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPhotos(query: String): Flow<List<PhotoEntity>>
}
