package com.example.a4cut.data.repository

import com.example.a4cut.data.model.KtxStation

class KtxStationRepository {

    private val gyeongbuLine = listOf(
        KtxStation("서울역", "Gyeongbu"),
        KtxStation("부산역", "Gyeongbu"),
        KtxStation("동대구역", "Gyeongbu"),
        KtxStation("대전역", "Gyeongbu"),
        KtxStation("수원역", "Gyeongbu")
    )

    private val honamLine = listOf(
        KtxStation("용산역", "Honam"),
        KtxStation("광주송정역", "Honam"),
        KtxStation("익산역", "Honam"),
        KtxStation("목포역", "Honam"),
        KtxStation("서대전역", "Honam")
    )

    fun getStationsByLine(line: String): List<KtxStation> {
        return when (line) {
            "Gyeongbu" -> gyeongbuLine
            "Honam" -> honamLine
            else -> emptyList()
        }
    }
}