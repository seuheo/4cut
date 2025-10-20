package com.example.a4cut.data.model

data class KtxStation(
    val name: String,
    val line: String, // "Gyeongbu" 또는 "Honam"
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val stationCode: String = ""
)