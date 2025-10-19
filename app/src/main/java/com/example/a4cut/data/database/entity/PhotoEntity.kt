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
    
    // 이미지 경로 (갤러리 URI) - 단일 이미지용
    val imagePath: String,
    
    // 4컷 이미지 URI 목록 (4컷 사진 지원용)
    val imageUris: List<String> = emptyList(),
    
    // 생성 날짜 (타임스탬프)
    val createdAt: Long,
    
    // 사진 제목 (사용자 입력 또는 자동 생성)
    val title: String = "",
    
    // 촬영 위치 (역 이름 등)
    val location: String = "",
    
    // GPS 좌표 저장을 위한 필드 추가
    val latitude: Double? = null,
    val longitude: Double? = null,
    
    // KTX 역 정보 (필터링용)
    val station: String? = null,
    
    // 즐겨찾기 여부
    val isFavorite: Boolean = false,
    
    // 프레임 타입 (KTX 시그니처 등)
    val frameType: String = "ktx_signature",
    
    // 컬러 테마 (프레임 색상 정보)
    val colorTheme: String = "ktx_blue",
    
    // 추가 메타데이터 (미래 기능 확장용)
    val tags: String = "",                 // 쉼표로 구분된 태그들
    val description: String = "",           // 사진 설명
    val weather: String = "",              // 촬영 당시 날씨
    val mood: String = "",                 // 사진의 분위기/감정
    val companions: String = "",           // 함께한 사람들
    val travelPurpose: String = "",        // 여행 목적 (출장, 여행, 출장 등)
    val season: String = "",               // 촬영 계절
    val timeOfDay: String = ""             // 촬영 시간대 (아침, 점심, 저녁, 밤)
)
