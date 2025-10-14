package com.example.a4cut.data.model

/**
 * KTX역 데이터 모델
 * 호남선과 경부선의 주요 KTX역 정보를 담는 데이터 클래스
 */
data class KtxStation(
    val id: String,
    val name: String,
    val line: KtxLine,
    val order: Int, // 노선상 순서
    val isTerminal: Boolean = false // 종착역 여부
)

/**
 * KTX 노선 열거형
 */
enum class KtxLine(val displayName: String) {
    HONAM("호남선"),
    GYEONGBU("경부선")
}

/**
 * KTX역 그룹 데이터 클래스
 */
data class KtxStationGroup(
    val line: KtxLine,
    val stations: List<KtxStation>
)
