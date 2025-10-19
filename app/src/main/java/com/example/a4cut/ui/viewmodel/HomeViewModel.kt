package com.example.a4cut.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.database.AppDatabase
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.repository.KTXStationRepository
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.data.model.KtxStation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

/**
 * 새로운 홈 화면의 ViewModel
 * 포토로그 기능과 KTX 브랜드 특화 데이터 관리
 */
class HomeViewModel : ViewModel() {
    
    private var photoRepository: PhotoRepository? = null
    private val ktxStationRepository by lazy { KTXStationRepository() }
    
    // UI 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 테스트 모드 상태
    private val _isTestMode = MutableStateFlow(false)
    val isTestMode: StateFlow<Boolean> = _isTestMode.asStateFlow()
    
    // KTX역 선택 상태
    private val _selectedKtxStation = MutableStateFlow<KtxStation?>(null)
    val selectedKtxStation: StateFlow<KtxStation?> = _selectedKtxStation.asStateFlow()
    
    // 역 선택 상태 관리 (필터링용)
    private val _selectedStation = MutableStateFlow<String?>(null)
    val selectedStation: StateFlow<String?> = _selectedStation.asStateFlow()
    
    // 포토로그 데이터
    private val _photoLogs = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photoLogs: StateFlow<List<PhotoEntity>> = _photoLogs.asStateFlow()
    
    private val _photoCount = MutableStateFlow(0)
    val photoCount: StateFlow<Int> = _photoCount.asStateFlow()
    
    private val _favoritePhotoCount = MutableStateFlow(0)
    val favoritePhotoCount: StateFlow<Int> = _favoritePhotoCount.asStateFlow()
    
    // 최신 사진 (스토리용)
    val latestPhoto: StateFlow<PhotoEntity?> = photoLogs.map { photos ->
        photos.maxByOrNull { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    // 모든 사진 (피드용) - 역 선택에 따라 필터링
    val allPhotos: StateFlow<List<PhotoEntity>> = combine(photoLogs, _selectedStation) { photos, station ->
        val filteredPhotos = if (station == null) {
            photos
        } else {
            photos.filter { it.station == station }
        }
        filteredPhotos.sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // ✅ 추가: 가장 최근 사진 최대 5개를 리스트로 제공 (캐러셀용)
    val latestPhotos: StateFlow<List<PhotoEntity>> = photoLogs.map { photos ->
        photos.sortedByDescending { it.createdAt }.take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // ✅ 추가: 사진이 존재하는 모든 날짜 목록을 가져옵니다. (달력 표시용)
    val datesWithPhotos: StateFlow<List<LocalDate>> = photoLogs.map { photos ->
        photos.map { photo ->
            // Calendar를 사용하여 API 호환성 확보
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = photo.createdAt
            LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }.distinct().sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    /**
     * Context 설정 및 데이터베이스 초기화
     */
    fun setContext(context: Context) {
        try {
            val database = AppDatabase.getDatabase(context)
            photoRepository = PhotoRepository(database.photoDao())
            loadPhotoLogs()
            // 자동 테스트 데이터 추가 제거 - 테스트 버튼으로 제어
        } catch (e: Exception) {
            _errorMessage.value = "데이터베이스 초기화 실패: ${e.message}"
            e.printStackTrace()
            // 초기화 실패 시에도 기본 상태 유지
        }
    }
    
    /**
     * 포토로그 데이터 로드
     */
    private fun loadPhotoLogs() {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    // 사진 목록과 개수 동시 로드
                    launch {
                        try {
                            repository.getAllPhotos().collect { photos ->
                                _photoLogs.value = photos
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "사진 목록 로드 실패: ${e.message}"
                        }
                    }
                    
                    launch {
                        try {
                            repository.getPhotoCount().collect { count ->
                                _photoCount.value = count
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "사진 개수 로드 실패: ${e.message}"
                        }
                    }
                    
                    launch {
                        try {
                            repository.getFavoritePhotoCount().collect { count ->
                                _favoritePhotoCount.value = count
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "즐겨찾기 개수 로드 실패: ${e.message}"
                        }
                    }
                    
                    clearError()
                } catch (e: Exception) {
                    _errorMessage.value = "포토로그 로드 실패: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite(photo: PhotoEntity) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.toggleFavorite(photo)
                } catch (e: Exception) {
                    _errorMessage.value = "즐겨찾기 변경 실패: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 사진 삭제
     */
    fun deletePhoto(photo: PhotoEntity) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.deletePhoto(photo)
                    // 삭제 후 목록 새로고침
                    loadPhotoLogs()
                } catch (e: Exception) {
                    _errorMessage.value = "사진 삭제 실패: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 모든 사진 삭제
     */
    fun deleteAllPhotos() {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    repository.deleteAllPhotos()
                    // 삭제 후 목록 새로고침
                    loadPhotoLogs()
                    println("모든 사진이 삭제되었습니다.")
                } catch (e: Exception) {
                    _errorMessage.value = "모든 사진 삭제 실패: ${e.message}"
                    println("모든 사진 삭제 실패: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * 검색 기능
     */
    fun searchPhotos(query: String) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.searchPhotos(query).collect { photos ->
                        _photoLogs.value = photos
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "검색 실패: ${e.message}"
                }
            }
        }
    }
    
    /**
     * 에러 메시지 초기화
     */
    private fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 데이터 새로고침
     */
    fun refreshData() {
        loadPhotoLogs()
    }
    
    /**
     * 테스트 모드 토글
     */
    fun toggleTestMode() {
        val newTestMode = !_isTestMode.value
        _isTestMode.value = newTestMode
        
        // 디버깅 로그
        println("테스트 모드 토글: $newTestMode")
        
        if (newTestMode) {
            insertTestData()
        } else {
            clearTestData()
        }
        // 테스트 모드 변경 후 데이터 새로고침
        loadPhotoLogs()
    }
    
    // TEST CODE START: 테스트 데이터를 생성하고 삽입하는 함수
    private fun insertTestData() {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    // 테스트 데이터 추가
                    val testPhotos = listOf(
                        PhotoEntity(
                            imagePath = "https://picsum.photos/id/1/400/600",
                            createdAt = System.currentTimeMillis() - (86400000L * 30), // 30일 전
                            title = "첫 번째 KTX 여행",
                            location = "서울역",
                            frameType = "ktx_signature",
                            tags = "풍경,도시,KTX",
                            description = "새해 첫날 서울역에서 찍은 KTX 네컷 사진입니다.",
                            weather = "맑음",
                            mood = "행복",
                            companions = "친구들",
                            travelPurpose = "휴가",
                            season = "겨울",
                            timeOfDay = "오전"
                        ),
                        PhotoEntity(
                            imagePath = "https://picsum.photos/id/10/400/600",
                            createdAt = System.currentTimeMillis() - (86400000L * 15), // 15일 전
                            title = "부산 해운대 여행",
                            location = "부산역",
                            frameType = "ktx_signature",
                            tags = "바다,여행,부산",
                            description = "해운대 해수욕장으로 가는 KTX 여행",
                            weather = "흐림",
                            mood = "평온",
                            companions = "가족",
                            travelPurpose = "가족여행",
                            season = "봄",
                            timeOfDay = "오후"
                        ),
                        PhotoEntity(
                            imagePath = "https://picsum.photos/id/20/400/600",
                            createdAt = System.currentTimeMillis() - (86400000L * 7), // 7일 전
                            title = "제주도 한라산 등반",
                            location = "제주역",
                            frameType = "ktx_signature",
                            tags = "산,등산,자연,제주",
                            description = "한라산 등반 중 멋진 경치를 발견했습니다.",
                            weather = "구름 많음",
                            mood = "상쾌함",
                            companions = "혼자",
                            travelPurpose = "운동",
                            season = "봄",
                            timeOfDay = "점심"
                        ),
                        PhotoEntity(
                            imagePath = "https://picsum.photos/id/30/400/600",
                            createdAt = System.currentTimeMillis() - (86400000L * 3), // 3일 전
                            title = "전주 한옥마을 탐방",
                            location = "전주역",
                            frameType = "ktx_signature",
                            tags = "한옥,전통,문화",
                            description = "전주 한옥마을의 아름다운 전통 건축물",
                            weather = "맑음",
                            mood = "감동",
                            companions = "동료",
                            travelPurpose = "문화탐방",
                            season = "봄",
                            timeOfDay = "저녁"
                        ),
                        PhotoEntity(
                            imagePath = "https://picsum.photos/id/40/400/600",
                            createdAt = System.currentTimeMillis() - (86400000L * 1), // 1일 전
                            title = "강릉 커피거리",
                            location = "강릉역",
                            frameType = "ktx_signature",
                            tags = "커피,카페,강릉",
                            description = "강릉 커피거리에서 즐긴 특별한 시간",
                            weather = "비",
                            mood = "로맨틱",
                            companions = "연인",
                            travelPurpose = "데이트",
                            season = "봄",
                            timeOfDay = "새벽"
                        )
                    )
                    
                    testPhotos.forEach { photo ->
                        repository.insertPhoto(photo)
                    }
                    
                    // 테스트 데이터 추가 후 성공 메시지
                    _errorMessage.value = "테스트 데이터가 추가되었습니다."
                } catch (e: Exception) {
                    _errorMessage.value = "테스트 데이터 추가 실패: ${e.message}"
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 테스트 데이터 삭제
     */
    private fun clearTestData() {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    // 테스트 데이터만 삭제 (제목으로 구분)
                    val testTitles = listOf(
                        "첫 번째 KTX 여행",
                        "부산 해운대 여행", 
                        "제주도 한라산 등반",
                        "전주 한옥마을 탐방",
                        "강릉 커피거리"
                    )
                    
                    _photoLogs.value.forEach { photo ->
                        if (testTitles.contains(photo.title)) {
                            repository.deletePhoto(photo)
                        }
                    }
                    
                    // 목록 새로고침
                    loadPhotoLogs()
                    
                    // 테스트 데이터 삭제 후 성공 메시지
                    _errorMessage.value = "테스트 데이터가 삭제되었습니다."
                } catch (e: Exception) {
                    _errorMessage.value = "테스트 데이터 삭제 실패: ${e.message}"
                    e.printStackTrace()
                }
            }
        }
    }
    // TEST CODE END
    
    /**
     * KTX역 선택
     */
    fun selectKtxStation(station: KtxStation) {
        _selectedKtxStation.value = station
    }
    
    /**
     * KTX역 선택 해제
     */
    fun clearKtxStationSelection() {
        _selectedKtxStation.value = null
    }
    
    /**
     * 역 선택 함수 (필터링용)
     */
    fun selectStation(stationName: String?) {
        _selectedStation.value = stationName
    }
    
}

