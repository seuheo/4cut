package com.example.a4cut.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 사진 정보를 저장하는 Room Entity
 * 갤러리에 저장된 이미지의 메타데이터와 앱 내부 정보를 관리
 */
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // 이미지 경로 (갤러리 URI)
    val imagePath: String,
    
    // 생성 날짜 (타임스탬프)
    val createdAt: Long,
    
    // 사진 제목 (사용자 입력 또는 자동 생성)
    val title: String = "",
    
    // 촬영 위치 (역 이름 등)
    val location: String = "",
    
    // 즐겨찾기 여부
    val isFavorite: Boolean = false,
    
    // 프레임 타입 (KTX 시그니처 등)
    val frameType: String = "ktx_signature",
    
    // 컬러 테마 (프레임 색상 정보)
    val colorTheme: String = "ktx_blue"
)
