package com.example.a4cut.data.repository

import com.example.a4cut.data.service.LocationService

/**
 * KTX 역 정보를 관리하는 Repository
 * GPS 좌표를 기반으로 주변 KTX 역을 찾는 기능을 제공
 */
class KTXStationRepository {
    
    /**
     * KTX 역 정보 데이터 클래스
     */
    data class KTXStation(
        val name: String,           // 역 이름 (예: "서울역")
        val latitude: Double,       // 위도
        val longitude: Double,      // 경도
        val line: String,           // 노선 (예: "경부선", "호남선")
        val stationCode: String     // 역 코드 (예: "SEO")
    )
    
    /**
     * 주요 KTX 역 목록 (실제 좌표 기반)
     * 실제 KTX가 정차하는 주요 역들의 좌표를 포함
     */
    private val ktxStations = listOf(
        // 경부선
        KTXStation("서울역", 37.5547, 126.9706, "경부선", "SEO"),
        KTXStation("영등포역", 37.5155, 126.9076, "경부선", "YDP"),
        KTXStation("수원역", 37.2659, 126.9997, "경부선", "SUW"),
        KTXStation("평택역", 36.9907, 127.0856, "경부선", "PTK"),
        KTXStation("천안아산역", 36.7944, 127.1044, "경부선", "CNA"),
        KTXStation("오송역", 36.6178, 127.3311, "경부선", "OSN"),
        KTXStation("대전역", 36.3317, 127.4339, "경부선", "DJN"),
        KTXStation("김천구미역", 36.1136, 128.2044, "경부선", "KMG"),
        KTXStation("동대구역", 35.8786, 128.6283, "경부선", "DGU"),
        KTXStation("신경주역", 35.8514, 129.1956, "경부선", "SGU"),
        KTXStation("울산역", 35.5389, 129.3111, "경부선", "USN"),
        KTXStation("부산역", 35.1156, 129.0403, "경부선", "BSN"),
        
        // 호남선
        KTXStation("익산역", 35.9403, 126.9542, "호남선", "IKS"),
        KTXStation("정읍역", 35.5669, 126.8561, "호남선", "JEP"),
        KTXStation("광주송정역", 35.1372, 126.7922, "호남선", "GJS"),
        KTXStation("나주역", 35.0314, 126.7119, "호남선", "NAJ"),
        KTXStation("목포역", 34.8122, 126.3922, "호남선", "MOK"),
        
        // 경전선
        KTXStation("진주역", 35.1922, 128.0856, "경전선", "JIN"),
        KTXStation("진영역", 35.3042, 128.7319, "경전선", "JIY"),
        KTXStation("창원역", 35.2214, 128.6819, "경전선", "CWN"),
        KTXStation("마산역", 35.1969, 128.5722, "경전선", "MAS"),
        
        // 중앙선
        KTXStation("청량리역", 37.5806, 127.0481, "중앙선", "CGR"),
        KTXStation("덕소역", 37.5869, 127.2081, "중앙선", "DKS"),
        KTXStation("양평역", 37.4922, 127.4919, "중앙선", "YPG"),
        KTXStation("원주역", 37.3447, 127.9206, "중앙선", "WJN"),
        KTXStation("제천역", 37.1369, 128.2119, "중앙선", "JCN"),
        KTXStation("단양역", 36.9856, 128.3656, "중앙선", "DNY"),
        KTXStation("영주역", 36.8081, 128.6256, "중앙선", "YJU"),
        
        // 전라선
        KTXStation("여수엑스포역", 34.7606, 127.7619, "전라선", "YSE"),
        
        // 동해선
        KTXStation("포항역", 36.0192, 129.3439, "동해선", "POH"),
        KTXStation("경주역", 35.8431, 129.2119, "동해선", "GJU"),
        KTXStation("울산역", 35.5389, 129.3111, "동해선", "USN"),
        KTXStation("부산역", 35.1156, 129.0403, "동해선", "BSN")
    )
    
    /**
     * 주어진 좌표에서 반경 내의 KTX 역을 찾음
     * @param latitude 현재 위치의 위도
     * @param longitude 현재 위치의 경도
     * @param radiusMeters 검색 반경 (미터, 기본값: 500m)
     * @return 반경 내의 KTX 역 목록 (거리순으로 정렬)
     */
    fun findNearbyKTXStations(
        latitude: Double, 
        longitude: Double, 
        radiusMeters: Int = 500
    ): List<KTXStation> {
        // 거리 계산을 위한 임시 LocationService (Context 없이 사용)
        val locationService = object {
            fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
                val results = FloatArray(1)
                android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
                return results[0]
            }
        }
        
        return ktxStations
            .map { station ->
                val distance = locationService.calculateDistance(
                    latitude, longitude,
                    station.latitude, station.longitude
                )
                station to distance
            }
            .filter { (_, distance) -> distance <= radiusMeters }
            .sortedBy { (_, distance) -> distance }
            .map { (station, _) -> station }
    }
    
    /**
     * 가장 가까운 KTX 역을 찾음
     * @param latitude 현재 위치의 위도
     * @param longitude 현재 위치의 경도
     * @param radiusMeters 검색 반경 (미터, 기본값: 500m)
     * @return 가장 가까운 KTX 역 또는 null (반경 내에 역이 없는 경우)
     */
    fun findNearestKTXStation(
        latitude: Double, 
        longitude: Double, 
        radiusMeters: Int = 500
    ): KTXStation? {
        return findNearbyKTXStations(latitude, longitude, radiusMeters).firstOrNull()
    }
    
    /**
     * 역 이름으로 KTX 역 정보를 찾음
     * @param stationName 역 이름
     * @return KTX 역 정보 또는 null
     */
    fun findStationByName(stationName: String): KTXStation? {
        return ktxStations.find { 
            it.name.contains(stationName) || stationName.contains(it.name)
        }
    }
    
    /**
     * 노선별 KTX 역 목록을 반환 (기존 KtxStation 타입으로 변환)
     * @param line 노선명 (예: "Gyeongbu", "Honam")
     * @return 해당 노선의 KTX 역 목록
     */
    fun getStationsByLine(line: String): List<com.example.a4cut.data.model.KtxStation> {
        return ktxStations.filter { 
            when (line) {
                "Gyeongbu" -> it.line == "경부선"
                "Honam" -> it.line == "호남선"
                else -> it.line == line
            }
        }.map { station ->
            com.example.a4cut.data.model.KtxStation(
                name = station.name,
                line = line
            )
        }
    }
    
    /**
     * 모든 KTX 역 목록을 반환
     * @return 전체 KTX 역 목록
     */
    fun getAllStations(): List<KTXStation> {
        return ktxStations
    }
}