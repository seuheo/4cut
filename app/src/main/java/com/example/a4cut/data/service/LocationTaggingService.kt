package com.example.a4cut.data.service

import android.content.Context
import com.example.a4cut.data.repository.KTXStationRepository
import com.example.a4cut.data.model.KtxStation

/**
 * 위치 기반 자동 태깅 서비스
 * GPS 위치를 기반으로 KTX 역을 자동으로 태깅하는 기능을 제공
 */
class LocationTaggingService(private val context: Context) {
    
    private val locationService = LocationService(context)
    private val ktxStationRepository = KTXStationRepository()
    
    /**
     * 현재 위치를 기반으로 KTX 역을 자동 태깅
     * @param radiusMeters 검색 반경 (미터, 기본값: 500m)
     * @return 태깅된 KTX 역 정보 또는 null (위치를 가져올 수 없거나 반경 내에 역이 없는 경우)
     */
    suspend fun getCurrentLocationTag(
        radiusMeters: Int = 500
    ): KtxStation? {
        // 위치 권한 확인
        if (!locationService.hasLocationPermission()) {
            return null
        }
        
        // 현재 위치 가져오기 (고정밀도 우선, 실패 시 마지막 위치)
        val currentLocation = locationService.getCurrentLocation() 
            ?: locationService.getLastKnownLocation()
        
        if (currentLocation == null) {
            return null
        }
        
        // 주변 KTX 역 찾기
        return ktxStationRepository.findNearestKTXStation(
            currentLocation.latitude,
            currentLocation.longitude,
            radiusMeters
        )
    }
    
    /**
     * 주어진 좌표를 기반으로 KTX 역을 태깅
     * @param latitude 위도
     * @param longitude 경도
     * @param radiusMeters 검색 반경 (미터, 기본값: 500m)
     * @return 태깅된 KTX 역 정보 또는 null
     */
    fun getLocationTag(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 500
    ): KtxStation? {
        return ktxStationRepository.findNearestKTXStation(
            latitude,
            longitude,
            radiusMeters
        )
    }
    
    /**
     * 위치 정보를 기반으로 사진 메타데이터를 생성
     * @param radiusMeters 검색 반경 (미터, 기본값: 500m)
     * @return 위치 태깅 결과 (역 이름, 좌표, 거리 등)
     */
    suspend fun generateLocationMetadata(
        radiusMeters: Int = 500
    ): LocationMetadata? {
        val ktxStation = getCurrentLocationTag(radiusMeters) ?: return null
        
        val currentLocation = locationService.getCurrentLocation() 
            ?: locationService.getLastKnownLocation()
        
        if (currentLocation == null) {
            return null
        }
        
        val distance = locationService.calculateDistance(
            currentLocation.latitude,
            currentLocation.longitude,
            ktxStation.latitude,
            ktxStation.longitude
        )
        
        return LocationMetadata(
            stationName = ktxStation.stationName,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            stationLatitude = ktxStation.latitude,
            stationLongitude = ktxStation.longitude,
            distance = distance,
            line = ktxStation.line,
            stationCode = ktxStation.stationCode
        )
    }
    
    /**
     * 위치 메타데이터 데이터 클래스
     */
    data class LocationMetadata(
        val stationName: String,        // KTX 역 이름
        val latitude: Double,          // 촬영 위치 위도
        val longitude: Double,         // 촬영 위치 경도
        val stationLatitude: Double,   // KTX 역 위도
        val stationLongitude: Double,  // KTX 역 경도
        val distance: Float,           // 역까지의 거리 (미터)
        val line: String,              // 노선명
        val stationCode: String        // 역 코드
    )
}
