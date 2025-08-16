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
        // 제목
        Text(
            text = "포토레일",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "PHOTORAIL",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // KTX 기차 일러스트
        KTXIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // 프레임 캐러셀
        FrameCarousel(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        // 캘린더 뷰
        CalendarView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
