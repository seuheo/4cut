# KTX 네컷 앱 MVP Ver2 개발 계획서

**프로젝트**: KTX 네컷 앱 MVP Ver2  
**작성일**: 2025년 1월 13일  
**목표**: 앱 안정성 확보 및 신규 기능 추가

---

## 📋 **총괄 개발 계획**

### **전체 일정**: 약 3-5일 (4주)  
### **단계별 구성**:  
1. **Phase A**: KTX 역 목록 동기화 및 안정성 검증 (1-2일)  
2. **Phase B**: 캘린더 월 이동 기능 구현 (1일)  
3. **Phase C**: '노선도(잇다)' 캠페인 기능 구현 (2일)

---

## 🎯 **Phase A: KTX 역 목록 동기화 및 안정성 검증**

### **목표**: FrameApplyScreen과 CalendarScreen의 KTX 역 목록 일관성 확보

### **현재 문제점**:
- `KtxStationData.kt`: 경부선 + 호남선만 포함 (corrected)
- `FrameApplyViewModel.kt`: `KtxStationData.availableStations` 사용 중 (corrected)
- `KTXStationRepository.kt`: 경전선, 중앙선 등 추가 노선 포함 (deprecated)

### **작업 내용**:

#### **1. KtxStationData 검증 및 확인** ✅
**파일**: `app/src/main/java/com/example/a4cut/data/model/KtxStationData.kt`
- [x] 경부선 역 목록: 15개 역 (행신, 서울, 광명, ... 부산)
- [x] 호남선 역 목록: 14개 역 (용산, 행신, 광명, ... 익산)
- [x] `availableStations`: 경부선 + 호남선, 중복 제거 후 정렬
- **검증 결과**: ✅ 이미 올바르게 구현됨

#### **2. FrameApplyViewModel 수정** (보완 필요)
**파일**: `app/src/main/java/com/example/a4cut/ui/viewmodel/FrameApplyViewModel.kt`
- 현재 상태: `KtxStationData.availableStations`를 사용 중
- 추가 작업: `loadStationsForLine(line: String)` 함수가 올바르게 경부선/호남선만 필터링하는지 확인
- **작업 시간**: 30분

#### **3. KTXStationRepository 정리** (권장)
**파일**: `app/src/main/java/com/example/a4cut/data/repository/KTXStationRepository.kt`
- 현재 사용처 검증 필요
- 사용되지 않는다면 deprecated 표시 또는 주석 처리
- **작업 시간**: 30분

### **예상 작업 시간**: 1-2시간

---

## 📅 **Phase B: 캘린더 월 이동 기능 구현**

### **목표**: CalendarScreen에서 월 단위 네비게이션 추가

### **작업 내용**:

#### **1. HomeViewModel 확장**
**파일**: `app/src/main/java/com/example/a4cut/ui/viewmodel/HomeViewModel.kt`
```kotlin
// 추가할 상태
private val _displayedMonth = MutableStateFlow<YearMonth>(YearMonth.now())
val displayedMonth: StateFlow<YearMonth> = _displayedMonth.asStateFlow()

// 추가할 함수
fun goToNextMonth() {
    val nextMonth = _displayedMonth.value.plusMonths(1)
    _displayedMonth.value = nextMonth
}

fun goToPreviousMonth() {
    val previousMonth = _displayedMonth.value.minusMonths(1)
    _displayedMonth.value = previousMonth
}

// _displayedMonth가 변경될 때마다 datesWithPhotos 조회
val datesWithPhotos: StateFlow<List<LocalDate>> = combine(
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
```
- **작업 시간**: 1시간

#### **2. CalendarScreen UI 수정**
**파일**: `app/src/main/java/com/example/a4cut/ui/screens/CalendarScreen.kt`
```kotlin
// CalendarView 위에 추가할 UI
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    IconButton(onClick = { homeViewModel.goToPreviousMonth() }) {
        Icon(Icons.Default.ArrowBack, contentDescription = "이전 월")
    }
    
    Text(
        text = "${year}년 ${month + 1}월",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    
    IconButton(onClick = { homeViewModel.goToNextMonth() }) {
        Icon(Icons.Default.ArrowForward, contentDescription = "다음 월")
    }
}
```
- **작업 시간**: 1시간

#### **3. CalendarView 수정**
**파일**: `app/src/main/java/com/example/a4cut/ui/components/CalendarView.kt`
- `displayedMonth: YearMonth` 파라미터 추가
- `LocalDate.now()` 기준이 아닌 `displayedMonth` 기준으로 달력 생성
- **작업 시간**: 1시간

### **예상 작업 시간**: 3-4시간

---

## 🚂 **Phase C: '노선도(잇다)' 캠페인 기능 구현**

### **목표**: 사용자가 방문한 KTX 역을 추적하고, 모든 역을 방문했는지 확인하는 게이미피케이션 기능 구현

### **작업 내용**:

#### **1. PhotoDao 쿼리 추가**
**파일**: `app/src/main/java/com/example/a4cut/data/database/dao/PhotoDao.kt`
```kotlin
/**
 * 특정 연도에 방문한 고유한 역 이름 목록을 조회
 */
@Query("SELECT DISTINCT location FROM photos WHERE strftime('%Y', createdAt / 1000, 'unixepoch') = :year AND location != '' AND location IS NOT NULL")
suspend fun getVisitedLocationsByYear(year: String): List<String>
```
- **작업 시간**: 30분

#### **2. PhotoRepository 메서드 추가**
**파일**: `app/src/main/java/com/example/a4cut/data/repository/PhotoRepository.kt`
```kotlin
/**
 * 특정 연도에 방문한 고유한 역 이름 목록 조회
 */
suspend fun getVisitedLocationsByYear(year: String): List<String> = 
    photoDao.getVisitedLocationsByYear(year)
```
- **작업 시간**: 15분

#### **3. CampaignViewModel 생성**
**파일**: `app/src/main/java/com/example/a4cut/ui/viewmodel/CampaignViewModel.kt` (신규)
```kotlin
class CampaignViewModel(
    private val photoRepository: PhotoRepository
) : ViewModel() {
    
    private val _selectedYear = MutableStateFlow("2025")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()
    
    // 방문한 역 목록 (연도별)
    val visitedStationsInYear: StateFlow<List<String>> = combine(
        _selectedYear,
        photoRepository.getAllPhotos()
    ) { year, photos ->
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
    
    // 경부선 역 방문 상태
    val gyeongbuLineStatus: StateFlow<List<Pair<String, Boolean>>> = combine(
        visitedStationsInYear,
        _selectedYear
    ) { visited, year ->
        KtxStationData.gyeongbuLineStations.map { station ->
            station.stationName to visited.contains(station.stationName)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 호남선 역 방문 상태
    val honamLineStatus: StateFlow<List<Pair<String, Boolean>>> = combine(
        visitedStationsInYear,
        _selectedYear
    ) { visited, year ->
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
    
    fun selectYear(year: String) {
        _selectedYear.value = year
    }
}
```
- **작업 시간**: 2-3시간

#### **4. CampaignScreen 생성**
**파일**: `app/src/main/java/com/example/a4cut/ui/screens/CampaignScreen.kt` (신규)
- UI 구성:
  - 연도 선택기 (드롭다운 또는 세그먼트 컨트롤)
  - 경부선 역 방문 상태 리스트 (체크박스 스타일)
  - 호남선 역 방문 상태 리스트 (체크박스 스타일)
  - 완주 배지/UI (모든 역 방문 시 표시)
- **작업 시간**: 3-4시간

#### **5. 네비게이션 라우팅**
**파일**: `app/src/main/java/com/example/a4cut/ui/navigation/Screen.kt`
```kotlin
object Campaign : Screen(
    route = "campaign",
    title = R.string.nav_campaign,
    icon = R.drawable.ic_campaign
)
```

**파일**: `app/src/main/java/com/example/a4cut/ui/navigation/AppNavigation.kt`
```kotlin
composable(Screen.Campaign.route) {
    CampaignScreen(
        viewModel = hiltViewModel<CampaignViewModel>()
    )
}
```

**파일**: `app/src/main/java/com/example/a4cut/ui/screens/ProfileScreen.kt`
- "노선도 보기" 버튼 추가 → CampaignScreen으로 이동
- **작업 시간**: 1시간

### **예상 작업 시간**: 7-9시간

---

## 📊 **전체 작업 일정 요약**

| Phase | 작업 내용 | 예상 시간 | 우선순위 |
|-------|----------|-----------|----------|
| **Phase A** | KTX 역 목록 동기화 및 안정성 검증 | 1-2시간 | 🔴 최우선 |
| **Phase B** | 캘린더 월 이동 기능 구현 | 3-4시간 | 🟡 높음 |
| **Phase C** | '노선도(잇다)' 캠페인 기능 구현 | 7-9시간 | 🟢 중간 |

**총 예상 시간**: 11-15시간 (약 3-5일)  
**시작 예정일**: 즉시  
**완료 목표일**: 2025년 1월 18일

---

## ✅ **체크리스트**

### **Phase A: KTX 역 목록 동기화**
- [ ] FrameApplyViewModel.kt 수정 및 검증
- [ ] KTXStationRepository.kt 정리 (deprecated)
- [ ] 일관성 테스트 수행

### **Phase B: 캘린더 월 이동**
- [ ] HomeViewModel.kt 확장 (_displayedMonth 추가)
- [ ] CalendarScreen.kt UI 수정 (월 네비게이션 추가)
- [ ] CalendarView.kt 수정 (displayedMonth 파라미터)
- [ ] 테스트: 월 이동 기능 정상 작동 확인

### **Phase C: '노선도(잇다)' 캠페인**
- [ ] PhotoDao.kt 쿼리 추가
- [ ] PhotoRepository.kt 메서드 추가
- [ ] CampaignViewModel.kt 생성
- [ ] CampaignScreen.kt 생성
- [ ] 네비게이션 라우팅 추가
- [ ] 테스트: 역 방문 상태 표시 및 완주 UI 동작 확인

---

## 🎯 **최종 목표**

### **기능 요구사항**:
1. ✅ KTX 역 목록 일관성 확보 (FrameApplyScreen ↔ CalendarScreen)
2. ✅ 캘린더 월 단위 이동 기능
3. ✅ '노선도(잇다)' 캠페인 기능 (방문 추적 및 완주 확인)

### **품질 요구사항**:
- 크래시 없는 안정적인 앱 동작
- 메모리 누수 없는 효율적인 성능
- 사용자 경험을 고려한 직관적인 UI

---

## 📝 **참고 사항**

### **기술 스택**:
- Jetpack Compose (UI)
- Room Database (데이터 영구 저장)
- Kotlin Coroutines & Flow (비동기 처리)
- StateFlow & MutableStateFlow (상태 관리)

### **아키텍처 패턴**:
- MVVM (Model-View-ViewModel)
- Repository Pattern
- Clean Architecture

---

**완료 후 다음 단계**: MVP Ver2 배포 준비 및 실제 사용자 테스트 진행

