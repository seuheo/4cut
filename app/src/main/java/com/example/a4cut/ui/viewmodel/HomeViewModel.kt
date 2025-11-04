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
import java.time.YearMonth
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
    
    // 지도에 표시할 위치 필터 상태 (캘린더에서 위치 클릭 시 사용)
    private val _mapLocationFilter = MutableStateFlow<String?>(null)
    val mapLocationFilter: StateFlow<String?> = _mapLocationFilter.asStateFlow()
    
    // ✅ 추가: 캘린더에서 표시할 월 상태 (MVP Ver2) - 안전한 초기화
    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    val displayedMonth: StateFlow<YearMonth> = _displayedMonth.asStateFlow()
    
    // 선택된 날짜의 사진 목록 상태 (지도 표시용)
    private val _photosForSelectedDate = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val photosForSelectedDate: StateFlow<List<PhotoEntity>> = _photosForSelectedDate.asStateFlow()
    
    // 포토로그 데이터 - Repository의 Flow를 직접 구독하여 자동 업데이트
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
    
    // 모든 사진 (피드용) - 모든 사진 표시
    val allPhotos: StateFlow<List<PhotoEntity>> = photoLogs.map { photos ->
        photos.sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 필터가 적용된 사진 목록 (지도가 사용할 목록)
    // 안전한 초기화를 위해 초기값을 제공하고 지연 실행
    val filteredPhotosForMap: StateFlow<List<PhotoEntity>> = run {
        combine(
            photoLogs,
            _mapLocationFilter
        ) { photos, filter ->
            if (filter != null) {
                photos.filter { it.location == filter }
            } else {
                photos // 필터가 없으면 전체 목록
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    // ✅ 추가: 가장 최근 사진 최대 5개를 리스트로 제공 (캐러셀용)
    val latestPhotos: StateFlow<List<PhotoEntity>> = photoLogs.map { photos ->
        photos.sortedByDescending { it.createdAt }.take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // ✅ 추가: 사진이 존재하는 날짜 목록을 가져옵니다. (displayedMonth 기준으로 필터링)
    // 모든 날짜를 반환하여 달력에 점 표시 (월 단위 필터링만 적용)
    // 안전한 초기화를 위해 run 블록 사용
    val datesWithPhotos: StateFlow<List<LocalDate>> = run {
        combine(
            photoLogs,
            _displayedMonth
        ) { photos, displayedMonth ->
        photos
            .filter { photo ->
                // Calendar를 사용하여 API 호환성 확보
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = photo.createdAt
                val photoYear = calendar.get(Calendar.YEAR)
                val photoMonth = calendar.get(Calendar.MONTH) + 1
                
                // displayedMonth와 일치하는 사진만 필터링 (월만 확인)
                photoYear == displayedMonth.year && photoMonth == displayedMonth.monthValue
            }
            .map { photo ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = photo.createdAt
                LocalDate.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
            }
            .distinct()
            .sorted()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    /**
     * Context 설정 및 데이터베이스 초기화
     */
    fun setContext(context: Context) {
        try {
            val database = AppDatabase.getDatabase(context)
            photoRepository = PhotoRepository(database.photoDao())
            
            // Flow 구독 시작
            startFlowSubscriptions()
            
            // 초기화 성공 시 에러 메시지 제거
            clearError()
        } catch (e: Exception) {
            _errorMessage.value = "데이터베이스 초기화 실패: ${e.message}. 앱을 다시 시작해주세요."
            e.printStackTrace()
            // 초기화 실패 시에도 기본 상태 유지
        }
    }
    
    // Flow 구독 상태 관리
    private var isSubscribed = false
    
    /**
     * Flow 구독 시작 (중복 방지)
     */
    private fun startFlowSubscriptions() {
        val repository = photoRepository ?: return
        
        // 이미 구독 중이면 중복 구독 방지
        if (isSubscribed) {
            Log.d("HomeViewModel", "이미 Flow 구독 중이므로 중복 구독 방지")
            return
        }
        
        isSubscribed = true
        Log.d("HomeViewModel", "Flow 구독 시작")
        
        viewModelScope.launch {
            // 사진 목록 Flow 구독
            repository.getAllPhotos().collect { photos ->
                Log.d("HomeViewModel", "사진 목록 업데이트: ${photos.size} photos")
                if (photos.isNotEmpty()) {
                    Log.d("HomeViewModel", "저장된 사진 목록:")
                    photos.forEachIndexed { index, photo ->
                        Log.d("HomeViewModel", "  ${index + 1}. ID: ${photo.id}, 제목: ${photo.title}, 위치: ${photo.location}, 생성일: ${photo.createdAt}")
                    }
                }
                _photoLogs.value = photos
            }
        }
        
        viewModelScope.launch {
            // 사진 개수 Flow 구독
            repository.getPhotoCount().collect { count ->
                Log.d("HomeViewModel", "사진 개수 업데이트: $count photos")
                _photoCount.value = count
            }
        }
        
        viewModelScope.launch {
            // 즐겨찾기 개수 Flow 구독
            repository.getFavoritePhotoCount().collect { count ->
                Log.d("HomeViewModel", "즐겨찾기 개수 업데이트: $count favorites")
                _favoritePhotoCount.value = count
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
                // Flow 기반으로 자동 업데이트됨
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
                // Flow 기반으로 자동 업데이트됨
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
                    // Flow 기반으로 자동 업데이트됨
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
     * 검색 기능 (검색 결과는 별도 StateFlow로 관리)
     */
    private val _searchResults = MutableStateFlow<List<PhotoEntity>>(emptyList())
    val searchResults: StateFlow<List<PhotoEntity>> = _searchResults.asStateFlow()
    
    fun searchPhotos(query: String) {
        photoRepository?.let { repository ->
            viewModelScope.launch {
                try {
                    repository.searchPhotos(query).collect { photos ->
                        _searchResults.value = photos
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
            
            // Flow 기반으로 자동 업데이트됨
            
            clearError()
        } catch (e: Exception) {
            _errorMessage.value = "데이터베이스 재초기화 실패: ${e.message}. 앱을 다시 시작해주세요."
            e.printStackTrace()
        }
    }
    
    
    /**
     * 데이터 새로고침
     */
    fun refreshData() {
        // Flow 구독이 이미 활성화되어 있으면 자동으로 업데이트됨
        Log.d("HomeViewModel", "데이터 새로고침 요청 - Flow 구독 상태: $isSubscribed")
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
        // Flow 기반으로 자동 업데이트됨
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
                    
                    // 현재 로드된 사진 목록에서 테스트 데이터 삭제
                    val currentPhotos = _photoLogs.value
                    currentPhotos.forEach { photo ->
                        if (testTitles.contains(photo.title)) {
                            viewModelScope.launch {
                                repository.deletePhoto(photo)
                            }
                        }
                    }
                    
                    // Flow 기반으로 자동 업데이트됨
                    
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
     * 지도 위치 필터 설정 (캘린더에서 위치 클릭 시 사용)
     */
    fun setMapLocationFilter(location: String?) {
        _mapLocationFilter.value = location
        Log.d("HomeViewModel", "지도 위치 필터 설정: $location")
    }
    
    /**
     * 지도 위치 필터 해제 (지도를 벗어날 때 호출)
     */
    fun clearMapLocationFilter() {
        _mapLocationFilter.value = null
        Log.d("HomeViewModel", "지도 위치 필터 해제")
    }
    
    /**
     * 특정 날짜의 사진 목록을 로드 (지도 표시용)
     */
    fun loadPhotosForDate(calendar: Calendar) {
        val repository = photoRepository
        if (repository == null) {
            Log.e("CalendarTest", "Repository가 null입니다")
            _errorMessage.value = "데이터베이스가 초기화되지 않았습니다."
            return
        }
        
        Log.d("CalendarTest", "loadPhotosForDate 호출됨")
        
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
                
                Log.d("CalendarTest", "_photosForSelectedDate 업데이트 전: ${_photosForSelectedDate.value.size}")
                _photosForSelectedDate.value = photos
                Log.d("CalendarTest", "_photosForSelectedDate 업데이트 후: ${_photosForSelectedDate.value.size}")
                
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
                
                // Flow 기반으로 자동 업데이트됨
                
                _errorMessage.value = "테스트 위치 데이터가 추가되었습니다. (서울역)"
                
            } catch (e: Exception) {
                Log.e("CalendarTest", "테스트 데이터 추가 실패", e)
                _errorMessage.value = "테스트 데이터 추가 실패: ${e.message}"
                e.printStackTrace()
            }
        }
    }
    
    /**
     * ✅ 추가: 캘린더 월 이동 함수 (MVP Ver2)
     */
    fun goToNextMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(1)
        Log.d("HomeViewModel", "월 이동: ${_displayedMonth.value}")
    }
    
    fun goToPreviousMonth() {
        _displayedMonth.value = _displayedMonth.value.minusMonths(1)
        Log.d("HomeViewModel", "월 이동: ${_displayedMonth.value}")
    }
    
}

