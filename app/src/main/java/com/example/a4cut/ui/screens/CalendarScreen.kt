package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.components.CalendarView
import com.example.a4cut.ui.viewmodel.HomeViewModel
import java.util.Calendar

/**
 * 달력 전용 화면
 * 홈 화면의 달력을 그대로 옮겨서 실제 날짜와 동일하게 표시
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
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "달력", 
                        fontWeight = FontWeight.Bold
                    ) 
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
                },
                onNextMonth = { 
                    if (currentMonth == 11) {
                        currentMonth = 0
                        currentYear++
                    } else {
                        currentMonth++
                    }
                },
                onDateSelect = { calendar ->
                    selectedDate = calendar
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "선택된 날짜",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selected.get(Calendar.YEAR)}년 ${selected.get(Calendar.MONTH) + 1}월 ${selected.get(Calendar.DATE)}일",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        // 해당 날짜에 사진이 있는지 확인
                        val year = selected.get(Calendar.YEAR)
                        val month = selected.get(Calendar.MONTH) + 1
                        val day = selected.get(Calendar.DAY_OF_MONTH)
                        val hasPhotos = datesWithPhotos.any { localDate ->
                            localDate.year == year && 
                            localDate.monthValue == month && 
                            localDate.dayOfMonth == day
                        }
                        
                        if (hasPhotos) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📸 이 날에 찍은 사진이 있습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
