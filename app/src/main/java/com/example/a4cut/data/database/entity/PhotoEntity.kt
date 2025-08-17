package com.example.a4cut.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 사용자가 만든 4컷 사진 정보를 저장하는 Room Entity
 * 트렌디한 스타일과 KTX 브랜드 아이덴티티를 반영한 구조
 */
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // 이미지 경로 (갤러리 또는 앱 내부 저장소)
    val imagePath: String,
    
    // 생성 시간 (Unix timestamp)
    val createdAt: Long = System.currentTimeMillis(),
    
    // 추가 메타데이터 (트렌디한 기능을 위한 확장성)
    val title: String? = null,           // 사용자 입력 제목
    val location: String? = null,        // 촬영 위치 (역 이름 등)
    val tags: String? = null,            // 태그 (JSON 형태로 저장)
    val isFavorite: Boolean = false,     // 즐겨찾기 여부
    
    // KTX 브랜드 관련 정보
    val frameType: String = "ktx_signature", // 사용된 프레임 타입
    val colorTheme: String = "ktx_blue"     // 사용된 컬러 테마
)
