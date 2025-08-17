package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.components.CalendarView
import com.example.a4cut.ui.components.TrainWindowCarousel
import com.example.a4cut.ui.viewmodel.HomeViewModel
import java.time.format.DateTimeFormatter

/**
 * 새로운 홈 화면 - PDF 시안 기반
 * 상단에 대표 사진, 하단에 달력을 배치하는 구조
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPhotoDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    
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
    val latestPhotos by homeViewModel.latestPhotos.collectAsState()
    val datesWithPhotos by homeViewModel.datesWithPhotos.collectAsState()
    // val isLoading by homeViewModel.isLoading.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    val isTestMode by homeViewModel.isTestMode.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PHOTORAIL", fontWeight = FontWeight.Bold) },
                actions = {
                    // 테스트 모드 토글 버튼
                    Button(
                        onClick = { homeViewModel.toggleTestMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTestMode) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = if (isTestMode) "테스트 끄기" else "테스트 켜기",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // KTX 기차 창문 캐러셀 섹션
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "나의 여행", // PDF의 "나의 여행" 부분
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // KTX 기차 창문 캐러셀로 최신 사진들 표시
            TrainWindowCarousel(
                photos = latestPhotos,
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id.toString())
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 달력 뷰
            CalendarView(
                currentMonth = java.time.LocalDate.now().monthValue - 1, // Calendar는 0-based
                currentYear = java.time.LocalDate.now().year,
                selectedDate = null, // 현재 선택된 날짜 없음
                onPreviousMonth = { /* 이전 달로 이동 */ },
                onNextMonth = { /* 다음 달로 이동 */ },
                onDateSelect = { calendar ->
                    // 특정 날짜를 클릭했을 때의 동작을 여기에 구현할 수 있습니다.
                    // 예: 해당 날짜의 사진 목록으로 이동 등
                    println("Selected date: ${calendar.time}")
                },
                isSpecialDay = { calendar ->
                    // 사진이 있는 날짜를 특별한 날로 표시
                    val localDate = java.time.Instant.ofEpochMilli(calendar.timeInMillis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    datesWithPhotos.contains(localDate)
                }
            )
            
            // 에러 메시지 표시
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessageSection(message = message)
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
