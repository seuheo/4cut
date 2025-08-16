# KTX 네컷 앱 - 시스템 패턴

## 🏗️ 아키텍처 개요

### 전체 시스템 구조
```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │   MainScreen    │  │   PhotoGrid     │  │ ActionBtns  │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     ViewModel Layer                         │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                  MainViewModel                          │ │
│  │  - UI State Management                                 │ │
│  │  - Business Logic                                      │ │
│  │  - User Actions                                        │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Repository Layer                         │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                PhotoRepository                          │ │
│  │  - Data Operations                                     │ │
│  │  - State Flow Management                               │ │
│  │  - Data Consistency                                    │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐ │
│  │     Photo       │  │   ImageProc     │  │  FileMgr    │ │
│  │   (Model)       │  │   (Utils)       │  │  (Utils)    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 핵심 디자인 패턴

### 1. MVVM (Model-View-ViewModel)
```kotlin
// ViewModel에서 UI 상태 관리
class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // UI 상태 업데이트
    fun updateState(newState: MainUiState) {
        _uiState.value = newState
    }
}

// Composable에서 상태 구독
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // UI 렌더링
}
```

### 2. Repository Pattern
```kotlin
class PhotoRepository {
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()
    
    fun updatePhotos(newPhotos: List<Photo>) {
        _uiState.value = newPhotos
    }
}
```

### 3. State Hoisting
```kotlin
// 상태를 상위 컴포넌트로 끌어올림
@Composable
fun PhotoGrid(
    photos: List<Photo>,
    onPhotoClick: (Int) -> Unit,  // 상태 변경 콜백
    modifier: Modifier = Modifier
) {
    // UI 렌더링만 담당
}
```

## 🎨 UI 패턴

### 1. Compose UI 구조
```kotlin
// 메인 화면 구조
@Composable
fun MainScreen() {
    Scaffold(
        topBar = { TopAppBar() },
        modifier = Modifier
    ) { innerPadding ->
        Column {
            PhotoGrid(modifier = Modifier.weight(1f))
            ActionButtons()
        }
    }
}
```

### 2. 그리드 레이아웃 패턴
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),  // 2열 고정
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    itemsIndexed(photos) { index, photo ->
        PhotoGridItem(photo, index, onClick)
    }
}
```

### 3. Material Design 3 컴포넌트
```kotlin
// 카드 기반 UI
Card(
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    // 콘텐츠
}
```

## 🔄 상태 관리 패턴

### 1. StateFlow 사용
```kotlin
// 단방향 데이터 플로우
private val _uiState = MutableStateFlow(MainUiState())
val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

// 상태 업데이트
fun updateState(update: (MainUiState) -> MainUiState) {
    _uiState.value = update(_uiState.value)
}
```

### 2. UI 상태 모델
```kotlin
data class MainUiState(
    val photos: List<Photo> = emptyList(),
    val isSaving: Boolean = false,
    val isSharing: Boolean = false,
    val errorMessage: String? = null
)
```

### 3. 상태 변경 처리
```kotlin
// 로딩 상태 관리
fun savePhotos() {
    _uiState.value = _uiState.value.copy(isSaving = true)
    
    viewModelScope.launch {
        try {
            // 저장 로직
            _uiState.value = _uiState.value.copy(isSaving = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                errorMessage = e.message
            )
        }
    }
}
```

## 🖼️ 이미지 처리 패턴

### 1. 이미지 로딩 (Coil)
```kotlin
AsyncImage(
    model = photo.uri,
    contentDescription = "Photo ${index + 1}",
    contentScale = ContentScale.Crop,
    modifier = Modifier.fillMaxWidth()
)
```

### 2. 이미지 합성 (Canvas API)
```kotlin
// 4컷 이미지를 하나로 합성하는 패턴
fun combinePhotos(photos: List<Bitmap>): Bitmap {
    val resultBitmap = Bitmap.createBitmap(
        totalWidth, totalHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(resultBitmap)
    
    photos.forEachIndexed { index, photo ->
        // 각 사진을 적절한 위치에 그리기
        val rect = calculateRect(index)
        canvas.drawBitmap(photo, null, rect, null)
    }
    
    return resultBitmap
}
```

## 🔐 권한 관리 패턴

### 1. Activity Result Contracts
```kotlin
// 권한 요청
private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        // 권한 승인 시 처리
    } else {
        // 권한 거부 시 처리
    }
}
```

### 2. 권한 상태 관리
```kotlin
sealed class PermissionState {
    object Granted : PermissionState()
    object Denied : PermissionState()
    object Requesting : PermissionState()
}
```

## 🧪 테스트 패턴

### 1. ViewModel 테스트
```kotlin
@Test
fun `사진 저장 시 로딩 상태가 true로 변경된다`() = runTest {
    // Given
    val viewModel = MainViewModel()
    
    // When
    viewModel.savePhotos()
    
    // Then
    assertTrue(viewModel.uiState.value.isSaving)
}
```

### 2. Compose UI 테스트
```kotlin
@Test
fun `4컷 그리드가 올바르게 표시된다`() {
    composeTestRule.setContent {
        PhotoGrid(photos = testPhotos, onPhotoClick = {})
    }
    
    composeTestRule.onNodeWithText("Photo 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Photo 2").assertIsDisplayed()
    composeTestRule.onNodeWithText("Photo 3").assertIsDisplayed()
    composeTestRule.onNodeWithText("Photo 4").assertIsDisplayed()
}
```

## 📱 플랫폼 특화 패턴

### 1. Android 생명주기 관리
```kotlin
class MainViewModel : ViewModel() {
    override fun onCleared() {
        super.onCleared()
        // 리소스 정리
    }
}
```

### 2. 에러 처리 패턴
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

### 3. 메모리 관리
```kotlin
// Bitmap 재사용
private var cachedBitmap: WeakReference<Bitmap>? = null

fun processImage(): Bitmap {
    cachedBitmap?.get()?.let { return it }
    
    val newBitmap = createNewBitmap()
    cachedBitmap = WeakReference(newBitmap)
    return newBitmap
}
```
