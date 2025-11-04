package com.example.a4cut.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.database.AppDatabase
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.model.KtxStationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ✅ MVP Ver2: 노선도(잇다) 캠페인 기능 ViewModel
 * 사용자가 방문한 KTX 역을 추적하고, 모든 역을 방문했는지 확인하는 게이미피케이션 기능
 */
class CampaignViewModel : ViewModel() {
    
    private var photoRepository: PhotoRepository? = null
    
    // 선택된 연도 상태
    private val _selectedYear = MutableStateFlow("2025")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()
    
    // 방문한 역 목록 (연도별)
    val visitedStationsInYear: StateFlow<List<String>> = combine(
        getPhotoLogs(),
        _selectedYear
    ) { photos, year ->
        photos
            .filter { photo ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = photo.createdAt
                calendar.get(Calendar.YEAR).toString() == year
            }
            .map { it.location }
            .filter { it.isNotEmpty() && it.isNotBlank() }
            .distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 경부선 역 방문 상태 (역 이름, 방문 여부)
    val gyeongbuLineStatus: StateFlow<List<Pair<String, Boolean>>> = combine(
        visitedStationsInYear,
        _selectedYear
    ) { visited, _ ->
        KtxStationData.gyeongbuLineStations.map { station ->
            station.stationName to visited.contains(station.stationName)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 호남선 역 방문 상태 (역 이름, 방문 여부)
    val honamLineStatus: StateFlow<List<Pair<String, Boolean>>> = combine(
        visitedStationsInYear,
        _selectedYear
    ) { visited, _ ->
        KtxStationData.honamLineStations.map { station ->
            station.stationName to visited.contains(station.stationName)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 경부선 완주 여부
    val isGyeongbuComplete: StateFlow<Boolean> = gyeongbuLineStatus.map { stations ->
        stations.all { it.second }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    // 호남선 완주 여부
    val isHonamComplete: StateFlow<Boolean> = honamLineStatus.map { stations ->
        stations.all { it.second }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    // 사진 로그 데이터
    private val _photoLogs = MutableStateFlow<List<com.example.a4cut.data.database.entity.PhotoEntity>>(emptyList())
    
    /**
     * 사진 로그 Flow 반환
     */
    private fun getPhotoLogs(): StateFlow<List<com.example.a4cut.data.database.entity.PhotoEntity>> {
        return if (photoRepository != null) {
            photoRepository!!.getAllPhotos().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        } else {
            _photoLogs.asStateFlow()
        }
    }
    
    /**
     * Context 설정 및 Repository 초기화
     */
    fun setContext(context: Context) {
        val database = AppDatabase.getDatabase(context)
        photoRepository = PhotoRepository(database.photoDao())
        
        // Flow 구독 시작
        photoRepository?.let { repo ->
            viewModelScope.launch {
                repo.getAllPhotos().collect { photos ->
                    _photoLogs.value = photos
                }
            }
        }
    }
    
    /**
     * 연도 선택
     */
    fun selectYear(year: String) {
        _selectedYear.value = year
    }
}

