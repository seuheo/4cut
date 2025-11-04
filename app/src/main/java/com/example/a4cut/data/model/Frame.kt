package com.example.a4cut.data.model

/**
 * 프레임 포맷을 나타내는 enum
 */
enum class FrameFormat {
    STANDARD,    // 표준 프레임 (기본)
    LONG_FORM    // 롱 폼 프레임 (2x6 인치 스타일)
}

/**
 * 프레임 내 사진 슬롯 정보
 * JSON 기반 프레임 시스템을 위한 데이터 클래스
 * @param x 정규화된 X 좌표 (0.0 ~ 1.0)
 * @param y 정규화된 Y 좌표 (0.0 ~ 1.0)
 * @param width 정규화된 너비 (0.0 ~ 1.0)
 * @param height 정규화된 높이 (0.0 ~ 1.0)
 */
data class Slot(
    val x: Float,      // 정규화된 X 좌표 (0.0 ~ 1.0)
    val y: Float,      // 정규화된 Y 좌표 (0.0 ~ 1.0)
    val width: Float,  // 정규화된 너비 (0.0 ~ 1.0)
    val height: Float  // 정규화된 높이 (0.0 ~ 1.0)
)

/**
 * 프레임 데이터 모델
 * KTX 프레임의 정보를 담는 데이터 클래스
 * 
 * @param slots JSON 기반 슬롯 정보 (null이면 기존 하드코딩 방식 사용)
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
    val format: FrameFormat = FrameFormat.STANDARD, // 프레임 포맷 (기본값: STANDARD)
    val slots: List<Slot>? = null // JSON 기반 슬롯 정보 (null이면 기존 방식 사용)
)

