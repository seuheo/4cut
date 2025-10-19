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
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import android.util.Log

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
    
    // 선택된 날짜의 사진 목록 상태 (지도 표시용)
    private val _photosForSelectedDate = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photosForSelectedDate: StateFlow<List<PhotoEntity>> = _photosForSelectedDate.asStateFlow()
    
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
            
            // 데이터베이스 초기화 성공 후 데이터 로드 (재시도 포함)
            loadPhotoLogsWithRetry()
            
            // 초기화 성공 시 에러 메시지 제거
            clearError()
        } catch (e: Exception) {
            _errorMessage.value = "데이터베이스 초기화 실패: ${e.message}. 앱을 다시 시작해주세요."
            e.printStackTrace()
            // 초기화 실패 시에도 기본 상태 유지
        }
    }
    
    /**
     * 포토로그 데이터 로드 (재시도 메커니즘 포함)
     */
    private fun loadPhotoLogsWithRetry(maxRetries: Int = 3) {
        val repository = photoRepository
        if (repository == null) {
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다. 앱을 다시 시작해주세요."
            return
        }
        
        viewModelScope.launch {
            var retryCount = 0
            var success = false
            
            while (retryCount < maxRetries && !success) {
                try {
                    _isLoading.value = true
                    
                    // 사진 목록과 개수 동시 로드
                    launch {
                        try {
                            repository.getAllPhotos().collect { photos ->
                                _photoLogs.value = photos
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "사진 목록 로드 실패: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                    
                    launch {
                        try {
                            repository.getPhotoCount().collect { count ->
                                _photoCount.value = count
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "사진 개수 로드 실패: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                    
                    launch {
                        try {
                            repository.getFavoritePhotoCount().collect { count ->
                                _favoritePhotoCount.value = count
                            }
                        } catch (e: Exception) {
                            _errorMessage.value = "즐겨찾기 개수 로드 실패: ${e.message}"
                            e.printStackTrace()
                        }
                    }
                    
                    // 성공적으로 로드되면 에러 메시지 자동 제거
                    clearError()
                    success = true
                    
                } catch (e: Exception) {
                    retryCount++
                    if (retryCount < maxRetries) {
                        _errorMessage.value = "데이터 로드 실패 (재시도 ${retryCount}/${maxRetries}): ${e.message}"
                        // 재시도 전 잠시 대기
                        delay((1000 * retryCount).toLong())
                    } else {
                        _errorMessage.value = "데이터 로드 실패: ${e.message}. 앱을 다시 시작해주세요."
                    }
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
    
    /**
     * 포토로그 데이터 로드 (기본 버전)
     */
    private fun loadPhotoLogs() {
        val repository = photoRepository
        if (repository == null) {
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다. 앱을 다시 시작해주세요."
            return
        }
        
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
                        e.printStackTrace()
                    }
                }
                
                launch {
                    try {
                        repository.getPhotoCount().collect { count ->
                            _photoCount.value = count
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "사진 개수 로드 실패: ${e.message}"
                        e.printStackTrace()
                    }
                }
                
                launch {
                    try {
                        repository.getFavoritePhotoCount().collect { count ->
                            _favoritePhotoCount.value = count
                        }
                    } catch (e: Exception) {
                        _errorMessage.value = "즐겨찾기 개수 로드 실패: ${e.message}"
                        e.printStackTrace()
                    }
                }
                
                // 성공적으로 로드되면 에러 메시지 자동 제거
                clearError()
            } catch (e: Exception) {
                _errorMessage.value = "포토로그 로드 실패: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 즐겨찾기 토글
     */
    fun toggleFavorite(photo: PhotoEntity) {
        val repository = photoRepository
        if (repository == null) {
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다."
            return
        }
        
        viewModelScope.launch {
            try {
                repository.toggleFavorite(photo)
                // 즐겨찾기 변경 후 카운트 새로고침
                refreshFavoriteCount()
            } catch (e: Exception) {
                _errorMessage.value = "즐겨찾기 변경 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 사진 삭제
     */
    fun deletePhoto(photo: PhotoEntity) {
        val repository = photoRepository
        if (repository == null) {
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다."
            return
        }
        
        viewModelScope.launch {
            try {
                repository.deletePhoto(photo)
                // 삭제 후 목록 새로고침
                loadPhotoLogs()
            } catch (e: Exception) {
                _errorMessage.value = "사진 삭제 실패: ${e.message}"
                e.printStackTrace()
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
     * 에러 메시지 수동 제거 (UI에서 호출)
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * 데이터베이스 연결 상태 확인
     */
    fun isDatabaseReady(): Boolean {
        return photoRepository != null
    }
    
    /**
     * 강제 데이터베이스 재초기화
     */
    fun forceReinitializeDatabase(context: Context) {
        try {
            // 기존 Repository 초기화
            photoRepository = null
            
            // 새로운 데이터베이스 인스턴스 생성
            val database = AppDatabase.getDatabase(context)
            photoRepository = PhotoRepository(database.photoDao())
            
            // 데이터 로드 재시도
            loadPhotoLogsWithRetry()
            
            clearError()
        } catch (e: Exception) {
            _errorMessage.value = "데이터베이스 재초기화 실패: ${e.message}. 앱을 다시 시작해주세요."
            e.printStackTrace()
        }
    }
    
    /**
     * 즐겨찾기 개수만 새로고침
     */
    private fun refreshFavoriteCount() {
        val repository = photoRepository
        if (repository == null) return
        
        viewModelScope.launch {
            try {
                repository.getFavoritePhotoCount().collect { count ->
                    _favoritePhotoCount.value = count
                }
            } catch (e: Exception) {
                _errorMessage.value = "즐겨찾기 개수 새로고침 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 데이터 새로고침
     */
    fun refreshData() {
        loadPhotoLogsWithRetry()
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
    
    /**
     * 특정 날짜의 사진 목록을 로드 (지도 표시용)
     */
    fun loadPhotosForDate(calendar: Calendar) {
        val repository = photoRepository
        if (repository == null) {
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다."
            return
        }
        
        viewModelScope.launch {
            try {
                // 선택된 날짜의 00:00:00 시각
                val startOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                // 선택된 날짜의 23:59:59 시각
                val endOfDay = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                
                Log.d("CalendarTest", "날짜 범위: $startOfDay ~ $endOfDay")
                Log.d("CalendarTest", "선택된 날짜: ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}")
                
                // 해당 날짜 범위의 사진 조회
                val photos = repository.getPhotosByDateRange(startOfDay, endOfDay)
                
                // --- 로그 추가 ---
                Log.d("CalendarTest", "선택된 날짜의 사진 개수: ${photos.size}")
                photos.forEach { photo ->
                    Log.d("CalendarTest", "  - 위치: ${photo.location}, 위도: ${photo.latitude}, 경도: ${photo.longitude}")
                }
                // --- ---
                
                _photosForSelectedDate.value = photos
                
            } catch (e: Exception) {
                Log.e("CalendarTest", "사진 로딩 실패", e)
                _errorMessage.value = "선택한 날짜의 사진 로딩 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 선택된 날짜의 사진 목록 초기화 (지도 숨기기용)
     */
    fun clearPhotosForSelectedDate() {
        _photosForSelectedDate.value = emptyList()
    }
    
    /**
     * 테스트용 위치 데이터 추가 (지도 테스트용)
     */
    fun addTestLocationData() {
        val repository = photoRepository
        if (repository == null) {
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다."
            return
        }
        
        viewModelScope.launch {
            try {
                // 오늘 날짜의 타임스탬프 계산
                val today = Calendar.getInstance()
                val startOfDay = today.apply {
                    set(Calendar.HOUR_OF_DAY, 12) // 오후 12시로 설정
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                // 테스트용 사진 데이터 생성
                val testPhoto = PhotoEntity(
                    id = 0, // 새로운 ID로 생성
                    imagePath = "test/seoul_station.jpg",
                    createdAt = startOfDay,
                    title = "서울역 테스트 사진",
                    location = "서울역",
                    latitude = 37.5547,
                    longitude = 126.9704,
                    station = "서울역",
                    frameType = "ktx_signature",
                    isFavorite = false
                )
                
                val photoId = repository.insertPhoto(testPhoto)
                Log.d("CalendarTest", "테스트 데이터 추가 완료 - ID: $photoId")
                
                // 데이터 새로고침
                loadPhotoLogsWithRetry()
                
                _errorMessage.value = "테스트 위치 데이터가 추가되었습니다. (서울역)"
                
            } catch (e: Exception) {
                Log.e("CalendarTest", "테스트 데이터 추가 실패", e)
                _errorMessage.value = "테스트 데이터 추가 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
}

