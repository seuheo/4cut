package com.example.a4cut.data.model

/**
 * 프레임 데이터 모델
 * KTX 프레임의 정보를 담는 데이터 클래스
 */
data class Frame(
    val id: Int,
    val name: String,
    val date: String,
    val station: String,
    val title: String,
    val previewImage: String? = null,
    val isPremium: Boolean = false
)

