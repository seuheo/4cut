package com.example.a4cut.data.model

/**
 * KTX 역 정보를 나타내는 데이터 클래스
 * @param stationName 역 이름
 * @param line 노선 이름 (예: "Gyeongbu", "Honam")
 * @param latitude 위도
 * @param longitude 경도
 * @param stationCode 역 코드 (예: "SEO", "BSN")
 */
data class KtxStation(
    val stationName: String,
    val line: String,
    val latitude: Double,
    val longitude: Double,
    val stationCode: String
)