package com.example.a4cut.data.model

/**
 * 프레임 데이터 모델
 * KTX 프레임의 정보를 담는 데이터 클래스
 */
data class Frame(
    val id: String,
    val name: String,
    val date: String,
    val station: String,
    val title: String,
    val previewImage: String? = null,
    val isPremium: Boolean = false,
    val drawableId: Int = com.example.a4cut.R.drawable.single_frame // 기본값으로 단일 프레임
)

