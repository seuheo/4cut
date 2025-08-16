package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a4cut.ui.components.KTXIllustration
import com.example.a4cut.ui.components.FrameCarousel
import com.example.a4cut.ui.components.CalendarView

/**
 * 홈 화면
 * KTX 기차 배경 + 프레임 캐러셀 + 캘린더
 * Phase 2: 레이아웃 비율 최적화 및 시각적 균형 개선
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
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
                .weight(0.35f)
        )
        
        // 캘린더 뷰 (25% - 정보 표시)
        CalendarView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
        )
    }
}
