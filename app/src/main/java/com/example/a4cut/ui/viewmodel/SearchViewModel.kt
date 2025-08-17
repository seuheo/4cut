package com.example.a4cut.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.database.entity.PhotoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 검색 및 필터링 기능을 관리하는 ViewModel
 * 검색어, 필터 조건, 검색 결과 상태를 관리
 */
class SearchViewModel : ViewModel() {
    
    // 검색 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 검색 결과
    private val _searchResults = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val searchResults: StateFlow<List<PhotoEntity>> = _searchResults.asStateFlow()
    
    // 검색 히스토리
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    // 필터 상태
    private val _selectedSeasons = MutableStateFlow<Set<String>>(emptySet())
    val selectedSeasons: StateFlow<Set<String>> = _selectedSeasons.asStateFlow()
    
    private val _selectedMoods = MutableStateFlow<Set<String>>(emptySet())
    val selectedMoods: StateFlow<Set<String>> = _selectedMoods.asStateFlow()
    
    private val _selectedWeather = MutableStateFlow<Set<String>>(emptySet())
    val selectedWeather: StateFlow<Set<String>> = _selectedWeather.asStateFlow()
    
    private val _sortBy = MutableStateFlow(SortOption.LATEST)
    val sortBy: StateFlow<SortOption> = _sortBy.asStateFlow()
    
    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 에러 상태
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 검색어 업데이트
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    // 계절 필터 토글
    fun toggleSeason(season: String) {
        val currentSeasons = _selectedSeasons.value.toMutableSet()
        if (currentSeasons.contains(season)) {
            currentSeasons.remove(season)
        } else {
            currentSeasons.add(season)
        }
        _selectedSeasons.value = currentSeasons
    }
    
    // 감정 필터 토글
    fun toggleMood(mood: String) {
        val currentMoods = _selectedMoods.value.toMutableSet()
        if (currentMoods.contains(mood)) {
            currentMoods.remove(mood)
        } else {
            currentMoods.add(mood)
        }
        _selectedMoods.value = currentMoods
    }
    
    // 날씨 필터 토글
    fun toggleWeather(weather: String) {
        val currentWeather = _selectedWeather.value.toMutableSet()
        if (currentWeather.contains(weather)) {
            currentWeather.remove(weather)
        } else {
            currentWeather.add(weather)
        }
        _selectedWeather.value = currentWeather
    }
    
    // 정렬 옵션 변경
    fun updateSortOption(sortOption: SortOption) {
        _sortBy.value = sortOption
    }
    
    // 검색 실행
    fun performSearch() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _errorMessage.value = "검색어를 입력해주세요"
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // TODO: Repository를 통한 실제 검색 구현
                // val results = photoRepository.searchPhotos(
                //     query = query,
                //     seasons = _selectedSeasons.value,
                //     moods = _selectedMoods.value,
                //     weather = _selectedWeather.value,
                //     sortBy = _sortBy.value
                // )
                
                // 임시 더미 데이터로 검색 결과 생성
                val dummyResults = createDummySearchResults(query)
                _searchResults.value = dummyResults
                
                // 검색 히스토리에 추가
                addToSearchHistory(query)
                
            } catch (e: Exception) {
                _errorMessage.value = "검색 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // 검색 히스토리에 추가
    private fun addToSearchHistory(query: String) {
        val currentHistory = _searchHistory.value.toMutableList()
        // 중복 제거
        currentHistory.remove(query)
        // 맨 앞에 추가
        currentHistory.add(0, query)
        // 최대 10개만 유지
        if (currentHistory.size > 10) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        _searchHistory.value = currentHistory
    }
    
    // 검색 히스토리에서 검색어 선택
    fun selectFromHistory(query: String) {
        _searchQuery.value = query
        performSearch()
    }
    
    // 필터 초기화
    fun clearFilters() {
        _selectedSeasons.value = emptySet()
        _selectedMoods.value = emptySet()
        _selectedWeather.value = emptySet()
        _sortBy.value = SortOption.LATEST
    }
    
    // 검색 결과 초기화
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchQuery.value = ""
    }
    
    // 에러 메시지 제거
    fun clearError() {
        _errorMessage.value = null
    }
    
    // 임시 더미 검색 결과 생성
    private fun createDummySearchResults(query: String): List<PhotoEntity> {
        return listOf(
            PhotoEntity(
                id = 1,
                imagePath = "dummy_path_1",
                createdAt = System.currentTimeMillis() - 86400000, // 1일 전
                title = "KTX 추억 $query",
                location = "서울역",
                frameType = "ktx_signature",
                tags = "$query,여행,친구",
                description = "$query 함께한 KTX 여행",
                weather = "맑음",
                mood = "즐거움",
                companions = "친구들",
                travelPurpose = "여행",
                season = "여름",
                timeOfDay = "오후",
                isFavorite = true
            ),
            PhotoEntity(
                id = 2,
                imagePath = "dummy_path_2",
                createdAt = System.currentTimeMillis() - 172800000, // 2일 전
                title = "기차 여행 $query",
                location = "부산역",
                frameType = "ktx_signature",
                tags = "$query,바다,여행",
                description = "$query 있는 부산 여행",
                weather = "흐림",
                mood = "평온함",
                companions = "가족",
                travelPurpose = "휴가",
                season = "여름",
                timeOfDay = "아침",
                isFavorite = false
            )
        )
    }
}

/**
 * 정렬 옵션
 */
enum class SortOption(val displayName: String) {
    LATEST("최신순"),
    OLDEST("오래된순"),
    FAVORITE("즐겨찾기순"),
    LOCATION("위치순"),
    SEASON("계절순"),
    MOOD("감정순")
}

/**
 * 필터 옵션들
 */
object FilterOptions {
    val seasons = listOf("봄", "여름", "가을", "겨울")
    val moods = listOf("즐거움", "평온함", "설렘", "그리움", "신남", "감동")
    val weather = listOf("맑음", "흐림", "비", "눈", "안개")
}
