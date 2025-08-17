package com.example.a4cut.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.a4cut.data.database.dao.PhotoDao
import com.example.a4cut.data.database.entity.PhotoEntity

/**
 * KTX 네컷 앱의 메인 데이터베이스
 * 트렌디한 기능과 KTX 브랜드 특화 데이터 구조 지원
 */
@Database(
    entities = [PhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    // DAO 접근자
    abstract fun photoDao(): PhotoDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 데이터베이스 인스턴스를 가져오는 팩토리 메서드
         * 싱글톤 패턴으로 구현하여 메모리 효율성 확보
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ktx_4cut_database"
                )
                .fallbackToDestructiveMigration() // 개발 단계에서만 사용
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
