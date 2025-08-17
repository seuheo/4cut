package com.example.a4cut.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.a4cut.data.database.dao.PhotoDao
import com.example.a4cut.data.database.entity.PhotoEntity

/**
 * 앱의 메인 데이터베이스 클래스
 * Room 데이터베이스 설정 및 DAO 제공
 */
@Database(
    entities = [PhotoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * PhotoDao 인스턴스 반환
     */
    abstract fun photoDao(): PhotoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 데이터베이스 인스턴스 반환 (싱글톤 패턴)
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "ktx_4cut_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw e
                }
            }
        }
    }
}
