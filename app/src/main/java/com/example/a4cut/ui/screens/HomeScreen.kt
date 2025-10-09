package com.example.a4cut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a4cut.ui.components.CalendarView
import com.example.a4cut.ui.components.TrainWindowCarousel
import com.example.a4cut.ui.components.TossPrimaryButton
import com.example.a4cut.ui.components.TossActionCard
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.HomeViewModel
import com.example.a4cut.data.database.entity.PhotoEntity
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * 토스 스타일 홈 화면 - 깔끔하고 직관적인 포토로그
 * 상단에 대표 사진, 하단에 달력을 배치하는 구조
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPhotoDetail: (String) -> Unit,
    onNavigateToFrame: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    
    // ViewModel의 상태들을 수집
    val latestPhotos by homeViewModel.latestPhotos.collectAsState()
    val datesWithPhotos by homeViewModel.datesWithPhotos.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    
    // ViewModel 초기화 - 안전한 초기화
    LaunchedEffect(Unit) {
        try {
            homeViewModel.setContext(context)
        } catch (e: Exception) {
            // 초기화 실패 시 기본 상태 유지
            e.printStackTrace()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "나의 포토로그",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        // 로딩 상태 처리
        if (isLoading) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = KTXBlue,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "사진을 불러오는 중...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(BackgroundLight)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // 대표 사진 섹션
            LatestPhotoSection(
                photos = latestPhotos,
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id.toString())
                }
            )
            
            // 새로운 네컷사진 만들기 버튼
            CreateNewPhotoButton(
                onClick = onNavigateToFrame
            )
            
            // 사진 그리드 섹션
            if (latestPhotos.isNotEmpty()) {
                PhotoGridSection(
                    photos = latestPhotos,
                    onPhotoClick = { photo ->
                        onNavigateToPhotoDetail(photo.id.toString())
                    }
                )
            }
            
            // 달력 섹션
            CalendarSection(
                datesWithPhotos = datesWithPhotos.toSet()
            )
            
            // 에러 메시지 표시
            errorMessage?.let { message ->
                ErrorMessageSection(message = message)
            }
            }
        }
    }
}







/**
 * 대표 사진 섹션 - 토스 스타일
 */
@Composable
private fun LatestPhotoSection(
    photos: List<PhotoEntity>,
    onPhotoClick: (PhotoEntity) -> Unit
) {
    if (photos.isNotEmpty()) {
        Column {
            Text(
                text = "최근 사진",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(photos.take(5).size) { index ->
                    val photo = photos.take(5)[index]
                    PhotoCard(
                        photo = photo,
                        onClick = { onPhotoClick(photo) },
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
        }
    }
}

/**
 * 새로운 네컷사진 만들기 버튼 - 토스 스타일
 */
@Composable
private fun CreateNewPhotoButton(
    onClick: () -> Unit
) {
    TossPrimaryButton(
        text = "새로운 네컷사진 만들기",
        onClick = onClick,
        icon = Icons.Default.Add,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * 사진 그리드 섹션 - 토스 스타일
 */
@Composable
private fun PhotoGridSection(
    photos: List<PhotoEntity>,
    onPhotoClick: (PhotoEntity) -> Unit
) {
    Column {
        Text(
            text = "모든 사진",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 2열 그리드로 사진 표시
        val rows = (photos.size + 1) / 2 // 올림 계산
        repeat(rows) { rowIndex ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 첫 번째 열
                if (rowIndex * 2 < photos.size) {
                    PhotoCard(
                        photo = photos[rowIndex * 2],
                        onClick = { onPhotoClick(photos[rowIndex * 2]) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                // 두 번째 열
                if (rowIndex * 2 + 1 < photos.size) {
                    PhotoCard(
                        photo = photos[rowIndex * 2 + 1],
                        onClick = { onPhotoClick(photos[rowIndex * 2 + 1]) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            
            // 행 간격
            if (rowIndex < rows - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

/**
 * 달력 섹션 - 토스 스타일
 */
@Composable
private fun CalendarSection(
    datesWithPhotos: Set<java.time.LocalDate>
) {
    Column {
        Text(
            text = "달력",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceLight
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            CalendarView(
                currentMonth = Calendar.getInstance().get(Calendar.MONTH),
                currentYear = Calendar.getInstance().get(Calendar.YEAR),
                selectedDate = null,
                onPreviousMonth = { /* 이전 달로 이동 */ },
                onNextMonth = { /* 다음 달로 이동 */ },
                onDateSelect = { calendar ->
                    println("Selected date: ${calendar.time}")
                },
                isSpecialDay = { calendar ->
                    val calendarDate = Calendar.getInstance().apply {
                        timeInMillis = calendar.timeInMillis
                    }
                    val year = calendarDate.get(Calendar.YEAR)
                    val month = calendarDate.get(Calendar.MONTH) + 1
                    val day = calendarDate.get(Calendar.DAY_OF_MONTH)
                    
                    datesWithPhotos.any { localDate ->
                        localDate.year == year && 
                        localDate.monthValue == month && 
                        localDate.dayOfMonth == day
                    }
                }
            )
        }
    }
}

/**
 * 사진 카드 - 토스 스타일
 */
@Composable
private fun PhotoCard(
    photo: PhotoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = SurfaceLight
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // 이미지 로딩 안전성 개선
            val imageUri = photo.imageUris.firstOrNull() ?: photo.imagePath
            if (!imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "사진",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 이미지 URI가 없는 경우 플레이스홀더
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TextTertiary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "이미지 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
            
            // 오버레이 정보
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = photo.title.ifEmpty { "제목 없음" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = photo.location.ifEmpty { "위치 없음" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 에러 메시지 섹션 - 토스 스타일
 */
@Composable
private fun ErrorMessageSection(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
