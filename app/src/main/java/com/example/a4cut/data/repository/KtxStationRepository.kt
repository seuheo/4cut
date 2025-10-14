package com.example.a4cut.data.repository

import com.example.a4cut.data.model.KtxLine
import com.example.a4cut.data.model.KtxStation
import com.example.a4cut.data.model.KtxStationGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * KTX역 데이터를 관리하는 Repository
 * 호남선과 경부선의 주요 KTX역 정보를 제공
 */
class KtxStationRepository {
    
    private val _stations = MutableStateFlow<List<KtxStation>>(emptyList())
    val stations: StateFlow<List<KtxStation>> = _stations.asStateFlow()
    
    private val _stationGroups = MutableStateFlow<List<KtxStationGroup>>(emptyList())
    val stationGroups: StateFlow<List<KtxStationGroup>> = _stationGroups.asStateFlow()
    
    init {
        // 초기 KTX역 데이터 로드
        try {
            loadInitialStations()
        } catch (e: Exception) {
            println("KtxStationRepository 초기화 오류: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 초기 KTX역 데이터 로드
     */
    private fun loadInitialStations() {
        val honamStations = listOf(
            KtxStation("honam_yongsan", "용산역", KtxLine.HONAM, 1),
            KtxStation("honam_daejeon", "대전역", KtxLine.HONAM, 2),
            KtxStation("honam_gwangju", "광주송정역", KtxLine.HONAM, 3),
            KtxStation("honam_mokpo", "목포역", KtxLine.HONAM, 4, true)
        )
        
        val gyeongbuStations = listOf(
            KtxStation("gyeongbu_seoul", "서울역", KtxLine.GYEONGBU, 1),
            KtxStation("gyeongbu_daejeon", "대전역", KtxLine.GYEONGBU, 2),
            KtxStation("gyeongbu_busan", "부산역", KtxLine.GYEONGBU, 3, true)
        )
        
        val allStations = honamStations + gyeongbuStations
        _stations.value = allStations
        
        val stationGroups = listOf(
            KtxStationGroup(KtxLine.HONAM, honamStations),
            KtxStationGroup(KtxLine.GYEONGBU, gyeongbuStations)
        )
        _stationGroups.value = stationGroups
    }
    
    /**
     * 모든 KTX역 목록 가져오기
     */
    fun getAllStations(): List<KtxStation> {
        return _stations.value
    }
    
    /**
     * 노선별 KTX역 그룹 가져오기
     */
    fun getStationGroups(): List<KtxStationGroup> {
        return _stationGroups.value
    }
    
    /**
     * 특정 노선의 KTX역 목록 가져오기
     */
    fun getStationsByLine(line: KtxLine): List<KtxStation> {
        return _stations.value.filter { it.line == line }
    }
    
    /**
     * 특정 ID의 KTX역 가져오기
     */
    fun getStationById(id: String): KtxStation? {
        return _stations.value.find { it.id == id }
    }
    
    /**
     * 특정 이름의 KTX역 가져오기
     */
    fun getStationByName(name: String): KtxStation? {
        return _stations.value.find { it.name == name }
    }
}
