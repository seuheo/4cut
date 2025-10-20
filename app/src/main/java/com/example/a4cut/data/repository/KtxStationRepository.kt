package com.example.a4cut.data.repository

import com.example.a4cut.data.model.KtxStation
import com.example.a4cut.data.service.LocationService

/**
 * KTX 역 정보를 관리하는 Repository
 * GPS 좌표를 기반으로 주변 KTX 역을 찾는 기능을 제공
 */
class KTXStationRepository {
    
    /**
     * 주요 KTX 역 목록 (실제 좌표 기반)
     * 실제 KTX가 정차하는 주요 역들의 좌표를 포함
     */
    private val ktxStations = listOf(
        // 경부선
        KtxStation("서울역", "Gyeongbu", 37.5547, 126.9706, "SEO"),
        KtxStation("영등포역", "Gyeongbu", 37.5155, 126.9076, "YDP"),
        KtxStation("수원역", "Gyeongbu", 37.2659, 126.9997, "SUW"),
        KtxStation("평택역", "Gyeongbu", 36.9907, 127.0856, "PTK"),
        KtxStation("천안아산역", "Gyeongbu", 36.7944, 127.1044, "CNA"),
        KtxStation("오송역", "Gyeongbu", 36.6178, 127.3311, "OSN"),
        KtxStation("대전역", "Gyeongbu", 36.3317, 127.4339, "DJN"),
        KtxStation("김천구미역", "Gyeongbu", 36.1136, 128.2044, "KMG"),
        KtxStation("동대구역", "Gyeongbu", 35.8786, 128.6283, "DGU"),
        KtxStation("신경주역", "Gyeongbu", 35.8514, 129.1956, "SGU"),
        KtxStation("울산역", "Gyeongbu", 35.5389, 129.3111, "USN"),
        KtxStation("부산역", "Gyeongbu", 35.1156, 129.0403, "BSN"),
        
        // 호남선
        KtxStation("익산역", "Honam", 35.9403, 126.9542, "IKS"),
        KtxStation("정읍역", "Honam", 35.5669, 126.8561, "JEP"),
        KtxStation("광주송정역", "Honam", 35.1372, 126.7922, "GJS"),
        KtxStation("나주역", "Honam", 35.0314, 126.7119, "NAJ"),
        KtxStation("목포역", "Honam", 34.8122, 126.3922, "MOK"),
        
        // 경전선
        KtxStation("진주역", "Gyeongjeon", 35.1922, 128.0856, "JIN"),
        KtxStation("진영역", "Gyeongjeon", 35.3042, 128.7319, "JIY"),
        KtxStation("창원역", "Gyeongjeon", 35.2214, 128.6819, "CWN"),
        KtxStation("마산역", "Gyeongjeon", 35.1969, 128.5722, "MAS"),
        
        // 중앙선
        KtxStation("청량리역", "Jungang", 37.5806, 127.0481, "CGR"),
        KtxStation("덕소역", "Jungang", 37.5869, 127.2081, "DKS"),
        KtxStation("양평역", "Jungang", 37.4922, 127.4919, "YPG"),
        KtxStation("원주역", "Jungang", 37.3447, 127.9206, "WJN"),
        KtxStation("제천역", "Jungang", 37.1369, 128.2119, "JCN"),
        KtxStation("단양역", "Jungang", 36.9856, 128.3656, "DNY"),
        KtxStation("영주역", "Jungang", 36.8081, 128.6256, "YJU"),
        
        // 전라선
        KtxStation("여수엑스포역", "Jeolla", 34.7606, 127.7619, "YSE"),
        
        // 동해선
        KtxStation("포항역", "Donghae", 36.0192, 129.3439, "POH"),
        KtxStation("경주역", "Donghae", 35.8431, 129.2119, "GJU"),
        KtxStation("울산역", "Donghae", 35.5389, 129.3111, "USN"),
        KtxStation("부산역", "Donghae", 35.1156, 129.0403, "BSN")
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
    ): List<KtxStation> {
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
    ): KtxStation? {
        return findNearbyKTXStations(latitude, longitude, radiusMeters).firstOrNull()
    }
    
    /**
     * 역 이름으로 KTX 역 정보를 찾음
     * @param stationName 역 이름
     * @return KTX 역 정보 또는 null
     */
    fun findStationByName(stationName: String): KtxStation? {
        return ktxStations.find { 
            it.name.contains(stationName) || stationName.contains(it.name)
        }
    }
    
    /**
     * 노선별 KTX 역 목록을 반환 (기존 KtxStation 타입으로 변환)
     * @param line 노선명 (예: "Gyeongbu", "Honam")
     * @return 해당 노선의 KTX 역 목록
     */
    fun getStationsByLine(line: String): List<KtxStation> {
        return ktxStations.filter { 
            it.line == line
        }
    }
    
    /**
     * 모든 KTX 역 목록을 반환
     * @return 전체 KTX 역 목록
     */
    fun getAllStations(): List<KtxStation> {
        return ktxStations
    }
    
    /**
     * 사용 가능한 노선 목록을 반환
     * @return 노선 목록
     */
    fun getLines(): List<String> {
        return listOf("Gyeongbu", "Honam", "Gyeongjeon", "Jungang", "Jeolla", "Donghae")
    }
}