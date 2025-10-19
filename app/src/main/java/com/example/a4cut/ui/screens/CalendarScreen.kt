package com.example.a4cut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.ui.components.CalendarView
import com.example.a4cut.ui.components.KtxStationSelector
import com.example.a4cut.data.repository.KTXStationRepository
import com.example.a4cut.ui.theme.IosColors
import com.example.a4cut.ui.viewmodel.HomeViewModel
import java.util.Calendar
// OpenStreetMap (osmdroid) 관련 import
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.saveable.rememberSaveable
import android.graphics.drawable.BitmapDrawable
import android.util.Log

/**
 * iOS 미니멀 스타일 달력 화면
 * 20대 사용자들이 선호하는 세련되고 깔끔한 디자인
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToPhotoDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    
    // 현재 날짜 상태 관리
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    
    // ViewModel 초기화 - 안전한 초기화
    LaunchedEffect(Unit) {
        try {
            homeViewModel.setContext(context)
        } catch (e: Exception) {
            // 초기화 실패 시 기본 상태 유지
            e.printStackTrace()
        }
    }
    
    // ViewModel의 상태들을 수집
    val datesWithPhotos by homeViewModel.datesWithPhotos.collectAsState()
    val allPhotos by homeViewModel.allPhotos.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    val selectedStation by homeViewModel.selectedStation.collectAsState()
    // 선택된 날짜의 사진 목록 구독 (지도 표시용)
    val photosForSelectedDate by homeViewModel.photosForSelectedDate.collectAsState()
    
    // KTX 역 선택을 위한 상태 변수 및 리포지토리
    val ktxStationRepository = remember { KTXStationRepository() }
    var selectedLine by remember { mutableStateOf("Gyeongbu") }
    val stations by remember(selectedLine) {
        mutableStateOf(ktxStationRepository.getStationsByLine(selectedLine))
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "캘린더", 
                        fontWeight = FontWeight.SemiBold,
                        color = IosColors.label
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = IosColors.secondarySystemBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(IosColors.secondarySystemBackground)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // KTX 노선 및 역 선택 UI 추가
            Column {
                TabRow(selectedTabIndex = if (selectedLine == "Gyeongbu") 0 else 1) {
                    Tab(
                        selected = selectedLine == "Gyeongbu",
                        onClick = { selectedLine = "Gyeongbu" },
                        text = { Text("경부선") }
                    )
                    Tab(
                        selected = selectedLine == "Honam",
                        onClick = { selectedLine = "Honam" },
                        text = { Text("호남선") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                KtxStationSelector(
                    stations = stations,
                    selectedStation = selectedStation,
                    onStationSelected = { stationName ->
                        homeViewModel.selectStation(stationName)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 달력 뷰
            CalendarView(
                currentMonth = currentMonth,
                currentYear = currentYear,
                selectedDate = selectedDate,
                onPreviousMonth = { 
                    if (currentMonth == 0) {
                        currentMonth = 11
                        currentYear--
                    } else {
                        currentMonth--
                    }
                    selectedDate = null // 월 변경 시 선택 해제
                    homeViewModel.clearPhotosForSelectedDate() // 월 변경 시 지도 숨기기
                },
                onNextMonth = { 
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                    selectedDate = null // 월 변경 시 선택 해제
                    homeViewModel.clearPhotosForSelectedDate() // 월 변경 시 지도 숨기기
                },
                onDateSelect = { calendar ->
                    selectedDate = calendar
                    // ViewModel에 선택된 날짜의 사진 로드 요청
                    homeViewModel.loadPhotosForDate(calendar)
                    
                    // 특정 날짜를 클릭했을 때의 동작
                    println("Selected date: ${calendar.time}")
                    
                    // 해당 날짜의 첫 번째 사진 ID 찾기
                    val photosOnDate = allPhotos.filter { photo ->
                        val photoDate = java.util.Calendar.getInstance().apply {
                            timeInMillis = photo.createdAt
                        }
                        photoDate.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR) &&
                        photoDate.get(java.util.Calendar.MONTH) == calendar.get(java.util.Calendar.MONTH) &&
                        photoDate.get(java.util.Calendar.DAY_OF_MONTH) == calendar.get(java.util.Calendar.DAY_OF_MONTH)
                    }
                    
                    if (photosOnDate.isNotEmpty()) {
                        // 해당 날짜의 첫 번째 사진으로 이동
                        onNavigateToPhotoDetail(photosOnDate.first().id.toString())
                    } else {
                        // 해당 날짜에 사진이 없으면 기본 사진으로 이동 (또는 아무 동작 안 함)
                        onNavigateToPhotoDetail("1")
                    }
                },
                isSpecialDay = { calendar ->
                    // 사진이 있는 날짜를 특별한 날로 표시
                    val calendarDate = Calendar.getInstance().apply {
                        timeInMillis = calendar.timeInMillis
                    }
                    val year = calendarDate.get(Calendar.YEAR)
                    val month = calendarDate.get(Calendar.MONTH) + 1
                    val day = calendarDate.get(Calendar.DAY_OF_MONTH)
                    
                    // LocalDate 대신 Calendar를 사용하여 API 호환성 확보
                    datesWithPhotos.any { localDate ->
                        localDate.year == year && 
                        localDate.monthValue == month && 
                        localDate.dayOfMonth == day
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 선택된 날짜 정보 표시
            selectedDate?.let { selected ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = IosColors.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "선택된 날짜",
                            style = MaterialTheme.typography.labelMedium,
                            color = IosColors.secondaryLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selected.get(Calendar.YEAR)}년 ${selected.get(Calendar.MONTH) + 1}월 ${selected.get(Calendar.DATE)}일",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = IosColors.label
                        )
                        
                        // 해당 날짜에 사진이 있는지 확인 (ViewModel의 새 상태 사용)
                        val hasPhotos = photosForSelectedDate.isNotEmpty()
                        
                        if (hasPhotos) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // 위치 정보가 있는 첫 번째 사진의 역 이름을 표시
                            val locationText = photosForSelectedDate
                                .firstNotNullOfOrNull { it.location.ifBlank { null } }
                                ?.let { " ($it)" } ?: ""
                            
                            Text(
                                text = "📸 이 날에 찍은 사진이 있습니다$locationText",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            // KTX 역에서 촬영한 사진이 있는지 확인
                            val ktxStationPhotos = photosForSelectedDate.filter { photo ->
                                photo.location.isNotBlank() && photo.location.contains("역")
                            }
                            
                            if (ktxStationPhotos.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val uniqueStations = ktxStationPhotos.map { it.location }.distinct()
                                Text(
                                    text = "🚉 ${uniqueStations.joinToString(", ")}에서 촬영",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
            
            // 선택된 날짜의 사진 목록 표시 (ViewModel의 새 상태 사용)
            if (photosForSelectedDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "이 날에 찍은 사진들 (${photosForSelectedDate.size}장)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // 사진 목록을 그리드로 표시
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(photosForSelectedDate) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = { onNavigateToPhotoDetail(photo.id.toString()) }
                        )
                    }
                }
            } else if (selectedDate != null) {
                // 해당 날짜에 사진이 없을 때 빈 상태 표시
                Spacer(modifier = Modifier.height(16.dp))
                EmptyCalendarDate()
            }
            
            // 캘린더 하단에 지도 표시 (위치 정보가 있는 사진이 있을 때만)
            if (photosForSelectedDate.isNotEmpty()) {
                Log.d("CalendarTest", "UI: 선택된 날짜의 사진 개수: ${photosForSelectedDate.size}")
                
                // 위치 정보(위도/경도)가 있는 사진만 필터링
                val photosWithLocation = photosForSelectedDate.mapNotNull { photo ->
                    if (photo.latitude != null && photo.longitude != null) {
                        Log.d("CalendarTest", "UI: 위치 정보 있는 사진 - ${photo.location} (${photo.latitude}, ${photo.longitude})")
                        Triple(GeoPoint(photo.latitude, photo.longitude), photo.location, photo)
                    } else {
                        Log.d("CalendarTest", "UI: 위치 정보 없는 사진 - ${photo.location}")
                        null
                    }
                }
                
                Log.d("CalendarTest", "UI: 지도에 표시할 사진 개수: ${photosWithLocation.size}")
                
                if (photosWithLocation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 지도 상태 저장 (줌 레벨, 스크롤 위치)
                    var mapViewState by rememberSaveable { mutableStateOf<Pair<GeoPoint, Double>?>(null) }
                    
                    // 초기 위치 설정 (첫 번째 사진 또는 저장된 상태)
                    val initialCenter = mapViewState?.first ?: photosWithLocation.first().first
                    val initialZoom = mapViewState?.second ?: 15.0
                    
                    Log.d("CalendarTest", "UI: OSM 지도 표시 시작. 사진 개수: ${photosWithLocation.size}")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = IosColors.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        // AndroidView를 사용하여 osmdroid MapView 통합
                        AndroidView(
                            factory = { context ->
                                MapView(context).apply {
                                    setTileSource(TileSourceFactory.MAPNIK) // OSM 기본 타일 소스
                                    setMultiTouchControls(true)
                                    controller.setZoom(initialZoom)
                                    controller.setCenter(initialCenter)
                                }
                            },
                            update = { mapView ->
                                Log.d("CalendarTest", "UI: MapView 업데이트. 마커 ${photosWithLocation.size}개 추가 시도")
                                
                                // 기존 마커 제거
                                mapView.overlays.clear()
                                
                                var mapCenterSet = false
                                photosWithLocation.forEach { (geoPoint, title, _) ->
                                    val marker = Marker(mapView)
                                    marker.position = geoPoint
                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                    marker.title = title.ifBlank { "사진 위치" }
                                    marker.snippet = "이곳에서 사진을 찍었습니다."
                                    
                                    mapView.overlays.add(marker)
                                    Log.d("CalendarTest", "UI: 마커 추가됨 - ${title} (${geoPoint.latitude})")
                                    
                                    // 첫 번째 마커 위치로 카메라 이동 (한 번만)
                                    if (!mapCenterSet) {
                                        mapView.controller.animateTo(geoPoint, initialZoom, 1000L)
                                        mapCenterSet = true
                                    }
                                }
                                mapView.invalidate() // 마커 추가 후 지도 갱신
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // MapView 라이프사이클 관리
                        val lifecycleOwner = LocalLifecycleOwner.current
                        DisposableEffect(lifecycleOwner) {
                            val observer = LifecycleEventObserver { _, event ->
                                when (event) {
                                    Lifecycle.Event.ON_PAUSE -> {
                                        // MapView 일시정지
                                    }
                                    Lifecycle.Event.ON_RESUME -> {
                                        // MapView 재개
                                    }
                                    Lifecycle.Event.ON_DESTROY -> {
                                        // 현재 지도 상태 저장
                                        // mapViewState = Pair(mapView.mapCenter as GeoPoint, mapView.zoomLevelDouble)
                                    }
                                    else -> {}
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)
                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // 하단 여백
                }
            }
            
            // 에러 메시지 표시
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessageSection(message = message)
            }
        }
    }
}

/**
 * 달력 날짜에 사진이 없을 때 표시하는 빈 상태
 */
@Composable
private fun EmptyCalendarDate() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "이 날에는 기록된 사진이 없네요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                text = "다른 날짜를 선택해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * 사진 그리드 아이템 (위치 정보 포함)
 */
@Composable
private fun PhotoGridItem(
    photo: PhotoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
        onClick = onClick
    ) {
        Box {
            AsyncImage(
                model = photo.imagePath,
                contentDescription = photo.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // 위치 정보 오버레이 (KTX 역인 경우에만)
            if (photo.location.isNotBlank() && photo.location.contains("역")) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(bottomStart = 8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🚉 ${photo.location}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 에러 메시지 섹션
 */
@Composable
private fun ErrorMessageSection(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
