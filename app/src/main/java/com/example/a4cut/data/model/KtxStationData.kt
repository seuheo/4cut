package com.example.a4cut.data.model

/**
 * KTX 노선 및 역 정보 데이터 모음
 */
object KtxStationData {

    // --- 경부선 역 목록 (실제 KTX 정차역) ---
    val gyeongbuLineStations = listOf(
        KtxStation("서울", "Gyeongbu", 37.5547, 126.9706, "SEO"),
        KtxStation("광명", "Gyeongbu", 37.4168, 126.8833, "GMG"),
        KtxStation("천안아산", "Gyeongbu", 36.7905, 127.1068, "CNA"),
        KtxStation("오송", "Gyeongbu", 36.6214, 127.3276, "OSN"),
        KtxStation("대전", "Gyeongbu", 36.3318, 127.4337, "DJN"),
        KtxStation("김천구미", "Gyeongbu", 36.1130, 128.3223, "KMG"),
        KtxStation("동대구", "Gyeongbu", 35.8797, 128.6280, "DGU"),
        KtxStation("밀양", "Gyeongbu", 35.5036, 128.7441, "MIL"),
        KtxStation("구포", "Gyeongbu", 35.2045, 128.9972, "GUP"),
        KtxStation("부산", "Gyeongbu", 35.1149, 129.0421, "BSN")
    )

    // --- 호남선 역 목록 (실제 KTX 정차역) ---
    val honamLineStations = listOf(
        KtxStation("용산", "Honam", 37.5298, 126.9647, "YOS"),
        KtxStation("광명", "Honam", 37.4168, 126.8833, "GMG"),
        KtxStation("천안아산", "Honam", 36.7905, 127.1068, "CNA"),
        KtxStation("오송", "Honam", 36.6214, 127.3276, "OSN"),
        KtxStation("서대전", "Honam", 36.3197, 127.4080, "SDJ"),
        KtxStation("익산", "Honam", 35.9458, 126.9537, "IKS"),
        KtxStation("정읍", "Honam", 35.5683, 126.8447, "JEO"),
        KtxStation("나주", "Honam", 35.0199, 126.7171, "NAJ"),
        KtxStation("광주송정", "Honam", 35.1388, 126.7877, "GJS"),
        KtxStation("목포", "Honam", 34.7828, 126.3860, "MOK")
    )

    // --- 전체 역 목록 (중복 제거) ---
    val allStations: List<KtxStation> = run {
        val all = gyeongbuLineStations + honamLineStations
        all.distinctBy { it.stationName }
    }

    // --- 노선별 역 목록 맵 ---
    val stationsByLine: Map<String, List<KtxStation>> = mapOf(
        "Gyeongbu" to gyeongbuLineStations,
        "Honam" to honamLineStations
    )

    // --- 역 이름으로 역 정보 찾기 ---
    fun findStationByName(stationName: String): KtxStation? {
        return allStations.find { it.stationName == stationName }
    }

    // --- 노선별 역 이름 목록 ---
    fun getStationNamesByLine(line: String): List<String> {
        return stationsByLine[line]?.map { it.stationName } ?: emptyList()
    }

    // --- 모든 역 이름 목록 ---
    fun getAllStationNames(): List<String> {
        return allStations.map { it.stationName }
    }
}
