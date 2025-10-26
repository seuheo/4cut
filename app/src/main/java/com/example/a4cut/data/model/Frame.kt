package com.example.a4cut.data.model

/**
 * 프레임 포맷을 나타내는 enum
 */
enum class FrameFormat {
    STANDARD,    // 표준 프레임 (기본)
    LONG_FORM    // 롱 폼 프레임 (2x6 인치 스타일)
}

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
    val drawableId: Int = com.example.a4cut.R.drawable.single_frame, // 기본값으로 단일 프레임
    val category: String? = null, // 프레임 카테고리 (예: "long form", "ktx", "basic")
    val format: FrameFormat = FrameFormat.STANDARD // 프레임 포맷 (기본값: STANDARD)
)

