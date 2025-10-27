package com.example.a4cut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.a4cut.data.model.KtxStationData
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
    homeViewModel: HomeViewModel,
    onNavigateToPhotoDetail: (String) -> Unit,
    onNavigateToHomeWithLocation: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // 현재 날짜 상태 관리 (주석 처리: HomeViewModel의 displayedMonth 사용)
    // var currentMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    // var currentYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    
    // ✅ 추가: ViewModel의 displayedMonth 상태 수집
    val displayedMonth by homeViewModel.displayedMonth.collectAsState()
    
    // 마커 클릭 시 사진 미리보기 상태
    var selectedPhotoForPreview by remember { mutableStateOf<PhotoEntity?>(null) }
    
    // ViewModel 초기화 - 안전한 초기화 (AppNavigation에서 이미 설정됨)
    LaunchedEffect(Unit) {
        try {
            if (!homeViewModel.isDatabaseReady()) {
                homeViewModel.setContext(context)
            }
        } catch (e: Exception) {
            // 초기화 실패 시 기본 상태 유지
            e.printStackTrace()
        }
    }
    
    // ViewModel의 상태들을 수집
    val datesWithPhotos by homeViewModel.datesWithPhotos.collectAsState()
    val allPhotos by homeViewModel.allPhotos.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    val mapLocationFilter by homeViewModel.mapLocationFilter.collectAsState()
    // 선택된 날짜의 사진 목록 구독 (지도 표시용)
    val photosForSelectedDate by homeViewModel.photosForSelectedDate.collectAsState()
    
    // 디버깅을 위한 로그
    androidx.compose.runtime.LaunchedEffect(photosForSelectedDate) {
        Log.d("CalendarTest", "UI: photosForSelectedDate 변경됨 - 개수: ${photosForSelectedDate.size}")
        photosForSelectedDate.forEach { photo ->
            Log.d("CalendarTest", "UI: 사진 정보 - 위치: ${photo.location}, 위도: ${photo.latitude}, 경도: ${photo.longitude}")
        }
    }
    
    // 사진이 있는 날짜가 있으면 자동으로 선택
    androidx.compose.runtime.LaunchedEffect(allPhotos) {
        if (allPhotos.isNotEmpty() && selectedDate == null) {
            // 가장 최근 사진의 날짜를 자동 선택
            val latestPhoto = allPhotos.maxByOrNull { it.createdAt }
            if (latestPhoto != null) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = latestPhoto.createdAt
                selectedDate = calendar
                homeViewModel.loadPhotosForDate(calendar)
                Log.d("CalendarTest", "UI: 자동으로 최근 사진 날짜 선택: ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}")
            }
        }
    }
    
    // KTX 역 선택을 위한 상태 변수
    var selectedLine by remember { mutableStateOf("Gyeongbu") }
    val stations by remember(selectedLine) {
        mutableStateOf(KtxStationData.getStationNamesByLine(selectedLine))
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
                    stations = KtxStationData.stationsByLine[selectedLine] ?: emptyList(),
                    selectedStation = mapLocationFilter,
                    onStationSelected = { stationName ->
                        homeViewModel.setMapLocationFilter(stationName)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // ✅ 추가: 월 네비게이션 UI (MVP Ver2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { homeViewModel.goToPreviousMonth() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "이전 월"
                    )
                }
                
                Text(
                    text = "${displayedMonth.year}년 ${displayedMonth.monthValue}월",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { homeViewModel.goToNextMonth() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "다음 월"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 달력 뷰
            CalendarView(
                currentMonth = displayedMonth.monthValue - 1, // YearMonth.monthValue는 1-12, Calendar.MONTH는 0-11
                currentYear = displayedMonth.year,
                selectedDate = selectedDate,
                onPreviousMonth = { 
                    homeViewModel.goToPreviousMonth()
                    selectedDate = null // 월 변경 시 선택 해제
                    homeViewModel.clearPhotosForSelectedDate() // 월 변경 시 지도 숨기기
                },
                onNextMonth = { 
                    homeViewModel.goToNextMonth()
                    selectedDate = null // 월 변경 시 선택 해제
                    homeViewModel.clearPhotosForSelectedDate() // 월 변경 시 지도 숨기기
                },
                onDateSelect = { calendar ->
                    selectedDate = calendar
                    Log.d("CalendarTest", "UI: 날짜 선택됨 - ${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}")
                    
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
                    
                    Log.d("CalendarTest", "UI: 해당 날짜의 사진 개수: ${photosOnDate.size}")
                    
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
                            onClick = { onNavigateToPhotoDetail(photo.id.toString()) },
                            onLocationClick = { location ->
                                // 현재 캘린더 탭의 지도에서 해당 역만 표시
                                homeViewModel.setMapLocationFilter(location)
                                Log.d("CalendarScreen", "지도 필터 설정: $location")
                            }
                        )
                    }
                }
            } else if (selectedDate != null) {
                // 해당 날짜에 사진이 없을 때 빈 상태 표시
                Spacer(modifier = Modifier.height(16.dp))
                EmptyCalendarDate()
            }
            
            // 캘린더 하단에 지도 표시 (위치 정보가 있는 사진이 있을 때만)
            Log.d("CalendarTest", "UI: photosForSelectedDate 상태 확인 - 개수: ${photosForSelectedDate.size}")
            Log.d("CalendarTest", "UI: selectedDate 상태: $selectedDate")
            Log.d("CalendarTest", "UI: allPhotos 개수: ${allPhotos.size}")
            
            // 지도 표시 조건: 날짜가 선택되었거나 사진이 있으면 지도 표시
            if (selectedDate != null || allPhotos.isNotEmpty()) {
                Log.d("CalendarTest", "UI: 선택된 날짜의 사진 개수: ${photosForSelectedDate.size}")
                
                // 지도에 표시할 사진 데이터 준비 - 필터링된 사진 표시
                val photosForMap = if (mapLocationFilter != null) {
                    // 특정 위치가 필터링된 경우 해당 위치의 사진만 표시
                    allPhotos.filter { it.location == mapLocationFilter }
                } else {
                    // 필터가 없으면 모든 사진 표시
                    allPhotos
                }
                
                val testPhotosWithLocation = photosForMap.mapNotNull { photo ->
                    try {
                        // 사진에 좌표가 있으면 사용, 없으면 역 이름으로 실제 좌표 찾기
                        val (latitude, longitude) = if (photo.latitude != null && photo.longitude != null) {
                            Pair(photo.latitude, photo.longitude)
                        } else {
                            // 역 이름으로 실제 좌표 찾기
                            val station = KtxStationData.findStationByName(photo.location ?: "")
                            if (station != null) {
                                Pair(station.latitude, station.longitude)
                            } else {
                                // 기본값 (서울역)
                                Pair(37.5547, 126.9706)
                            }
                        }
                        
                        Log.d("CalendarTest", "UI: 필터링된 사진 위치 정보 - ${photo.location} (${latitude}, ${longitude})")
                        Triple(GeoPoint(latitude, longitude), photo.location ?: "사진 위치", photo)
                    } catch (e: Exception) {
                        Log.e("CalendarTest", "UI: 사진 위치 정보 처리 중 오류", e)
                        null
                    }
                }
                
                Log.d("CalendarTest", "UI: 지도에 표시할 사진 개수: ${testPhotosWithLocation.size}")
                
                if (testPhotosWithLocation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 지도 상태 저장 (줌 레벨, 스크롤 위치)
                    var mapViewState by rememberSaveable { mutableStateOf<Pair<GeoPoint, Double>?>(null) }
                    
                    // 초기 위치 설정 (첫 번째 사진 또는 저장된 상태)
                    val initialCenter = mapViewState?.first ?: testPhotosWithLocation.first().first
                    val initialZoom = mapViewState?.second ?: 15.0
                    
                    Log.d("CalendarTest", "UI: OSM 지도 표시 시작. 사진 개수: ${testPhotosWithLocation.size}")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = IosColors.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            // 지도 제목 및 필터 해제 버튼
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (mapLocationFilter != null) {
                                        "📍 $mapLocationFilter"
                                    } else {
                                        "🗺️ 사진 위치"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IosColors.label
                                )
                                
                                if (mapLocationFilter != null) {
                                    TextButton(
                                        onClick = { 
                                            homeViewModel.clearMapLocationFilter()
                                            Log.d("CalendarScreen", "지도 필터 해제")
                                        }
                                    ) {
                                        Text(
                                            text = "전체 보기",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                            ) { mapView ->
                                try {
                                    Log.d("CalendarTest", "UI: MapView 업데이트. 마커 ${testPhotosWithLocation.size}개 추가 시도")
                                    
                                    // 기존 마커 제거
                                    mapView.overlays.clear()
                                    
                                    // 필터링된 마커만 추가
                                    val validMarkers = mutableListOf<Marker>()
                                    val filteredPhotos = if (mapLocationFilter != null) {
                                        testPhotosWithLocation.filter { (_, title, _) -> title == mapLocationFilter }
                                    } else {
                                        testPhotosWithLocation
                                    }
                                    
                                    filteredPhotos.forEach { (geoPoint, title, _) ->
                                        try {
                                            // GeoPoint 유효성 검사
                                            if (geoPoint.latitude.isFinite() && geoPoint.longitude.isFinite()) {
                                                val marker = Marker(mapView)
                                                marker.position = geoPoint
                                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                                marker.title = title.ifBlank { "사진 위치" }
                                                marker.snippet = "이곳에서 사진을 찍었습니다."
                                                
                                                // 마커 클릭 이벤트 설정
                                                marker.setOnMarkerClickListener { clickedMarker, mapView ->
                                                    try {
                                                        Log.d("CalendarTest", "UI: 마커 클릭됨 - ${clickedMarker.title}")
                                                        
                                                        // 클릭된 마커의 위치로 지도 이동 및 확대
                                                        mapView.controller.animateTo(clickedMarker.position, 17.0, 1000L)
                                                        
                                                        // 마커 정보 표시 (선택사항)
                                                        clickedMarker.showInfoWindow()
                                                        
                                                        // 해당 위치의 사진 찾기
                                                        val clickedLocation = clickedMarker.title
                                                        val photoAtLocation = allPhotos.find { photo ->
                                                            photo.location == clickedLocation
                                                        }
                                                        
                                                        if (photoAtLocation != null) {
                                                            selectedPhotoForPreview = photoAtLocation
                                                            Log.d("CalendarTest", "UI: 해당 위치의 사진 찾음: ${photoAtLocation.title}")
                                                        } else {
                                                            Log.d("CalendarTest", "UI: 해당 위치의 사진을 찾을 수 없음: $clickedLocation")
                                                        }
                                                        
                                                        true // 이벤트 처리됨
                                                    } catch (e: Exception) {
                                                        Log.e("CalendarTest", "UI: 마커 클릭 처리 중 오류", e)
                                                        false
                                                    }
                                                }
                                                
                                                mapView.overlays.add(marker)
                                                validMarkers.add(marker)
                                                Log.d("CalendarTest", "UI: 마커 추가됨 - ${title} (${geoPoint.latitude})")
                                            } else {
                                                Log.e("CalendarTest", "UI: 유효하지 않은 GeoPoint - ${title} (${geoPoint.latitude}, ${geoPoint.longitude})")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("CalendarTest", "UI: 마커 생성 중 오류 - ${title}", e)
                                        }
                                    }
                                    
                                    // 동적 지도 중심점 설정
                                    if (validMarkers.isNotEmpty()) {
                                        try {
                                            if (validMarkers.size == 1) {
                                                // 마커가 1개인 경우: 해당 위치로 이동 (필터링된 경우 확대)
                                                val singleMarker = validMarkers.first()
                                                val zoomLevel = if (mapLocationFilter != null) 18.0 else 17.0
                                                mapView.controller.animateTo(singleMarker.position, zoomLevel, 1000L)
                                                Log.d("CalendarTest", "UI: 단일 마커 위치로 지도 이동 (필터: $mapLocationFilter)")
                                            } else {
                                                // 마커가 여러 개인 경우: 모든 마커를 포함하는 범위로 설정
                                                val latitudes = validMarkers.map { it.position.latitude }
                                                val longitudes = validMarkers.map { it.position.longitude }
                                                
                                                val minLat = latitudes.minOrNull() ?: 0.0
                                                val maxLat = latitudes.maxOrNull() ?: 0.0
                                                val minLon = longitudes.minOrNull() ?: 0.0
                                                val maxLon = longitudes.maxOrNull() ?: 0.0
                                                
                                                // 중심점 계산
                                                val centerLat = (minLat + maxLat) / 2
                                                val centerLon = (minLon + maxLon) / 2
                                                val centerPoint = GeoPoint(centerLat, centerLon)
                                                
                                                // 줌 레벨 계산 (마커들 간의 거리에 따라 조정)
                                                val latSpan = maxLat - minLat
                                                val lonSpan = maxLon - minLon
                                                val maxSpan = maxOf(latSpan, lonSpan)
                                                
                                                val zoomLevel = when {
                                                    maxSpan > 0.1 -> 10.0  // 매우 넓은 범위
                                                    maxSpan > 0.05 -> 12.0 // 넓은 범위
                                                    maxSpan > 0.01 -> 14.0 // 중간 범위
                                                    else -> 16.0          // 좁은 범위
                                                }
                                                
                                                mapView.controller.animateTo(centerPoint, zoomLevel, 1000L)
                                                Log.d("CalendarTest", "UI: 다중 마커 범위로 지도 설정 - 중심: ($centerLat, $centerLon), 줌: $zoomLevel")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("CalendarTest", "UI: 지도 중심점 설정 중 오류", e)
                                            // 오류 발생 시 첫 번째 마커로 이동
                                            mapView.controller.animateTo(validMarkers.first().position, initialZoom, 1000L)
                                        }
                                    }
                                    mapView.invalidate() // 마커 추가 후 지도 갱신
                                } catch (e: Exception) {
                                    Log.e("CalendarTest", "UI: MapView 업데이트 중 오류", e)
                                }
                            }
                        }

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
        
        // 마커 클릭 시 사진 미리보기 다이얼로그
        selectedPhotoForPreview?.let { photo ->
            // 간단한 사진 미리보기 다이얼로그 (ImagePreviewDialog 대신 기본 다이얼로그 사용)
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { selectedPhotoForPreview = null }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = photo.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (photo.location.isNotBlank()) {
                            Text(
                                text = "📍 ${photo.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AsyncImage(
                            model = photo.imagePath,
                            contentDescription = photo.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    selectedPhotoForPreview = null
                                    onNavigateToPhotoDetail(photo.id.toString())
                                }
                            ) {
                                Text("상세 보기")
                            }
                            
                            OutlinedButton(
                                onClick = { selectedPhotoForPreview = null }
                            ) {
                                Text("닫기")
                            }
                        }
                    }
                }
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
    onLocationClick: (String) -> Unit,
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
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(bottomStart = 8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    // 역 이름과 역 아이콘 클릭 가능 영역
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onLocationClick(photo.location) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚉 ${photo.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    
                    // 지도 아이콘 버튼
                    IconButton(
                        onClick = { onLocationClick(photo.location) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "지도에서 보기",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
