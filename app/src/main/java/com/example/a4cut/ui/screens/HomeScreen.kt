package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.components.KTXIllustration
import com.example.a4cut.ui.components.FrameCarousel
import com.example.a4cut.ui.components.CalendarView
import com.example.a4cut.ui.viewmodel.HomeViewModel
import java.util.Calendar

/**
 * 홈 화면
 * KTX 기차 배경 + 프레임 캐러셀 + 캘린더
 * Phase 2: ViewModel 연동 및 상태 관리 개선
 * Phase 3: State Hoisting 패턴 적용으로 코드 품질 향상
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel()
) {
    // ViewModel의 상태들을 수집
    val frames by homeViewModel.frames.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val currentMonth by homeViewModel.currentMonth.collectAsState()
    val currentYear by homeViewModel.currentYear.collectAsState()
    val selectedDate by homeViewModel.selectedDate.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 제목 영역 (고정 높이)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "포토레일",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "PHOTORAIL",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // KTX 기차 일러스트 (40% - 시각적 중심)
        KTXIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
        )
        
        // 프레임 캐러셀 (35% - 사용자 상호작용)
        FrameCarousel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f),
            frames = frames,
            isLoading = isLoading,
            onFrameClick = { frame -> homeViewModel.selectFrame(frame) }
        )
        
        // 캘린더 뷰 (25% - 정보 표시)
        CalendarView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f),
            currentMonth = currentMonth,
            currentYear = currentYear,
            selectedDate = selectedDate,
            onPreviousMonth = { homeViewModel.goToPreviousMonth() },
            onNextMonth = { homeViewModel.goToNextMonth() },
            onDateSelect = { date -> homeViewModel.selectDate(date) },
            isSpecialDay = { date -> homeViewModel.isSpecialDay(date) }
        )
    }
}
