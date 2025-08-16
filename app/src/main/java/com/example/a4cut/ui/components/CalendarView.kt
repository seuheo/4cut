package com.example.a4cut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

/**
 * 캘린더 뷰 컴포넌트
 * 현재 월의 달력을 표시합니다
 */
@Composable
fun CalendarView(
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작
    val currentYear = calendar.get(Calendar.YEAR)
    
    // 현재 월의 첫 번째 날과 마지막 날
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth - 1, 1)
    }
    val lastDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
        add(Calendar.DATE, -1)
    }
    
    // 이전 달의 마지막 날들 (첫 주를 채우기 위해)
    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
    val previousMonthDays = (1..<firstDayOfWeek).map { 
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth - 2, 1)
        cal.add(Calendar.DATE, -1)
        cal.add(Calendar.DATE, -(firstDayOfWeek - it - 1))
        cal
    }
    
    // 현재 월의 날짜들
    val currentMonthDays = (1..lastDayOfMonth.get(Calendar.DATE)).map { day ->
        Calendar.getInstance().apply {
            set(currentYear, currentMonth - 1, day)
        }
    }
    
    // 다음 달의 첫 번째 날들 (마지막 주를 채우기 위해)
    val lastDayOfWeek = lastDayOfMonth.get(Calendar.DAY_OF_WEEK)
    val nextMonthDays = (1..(7 - lastDayOfWeek)).map { day ->
        Calendar.getInstance().apply {
            set(currentYear, currentMonth, day)
        }
    }
    
    val allDays = previousMonthDays + currentMonthDays + nextMonthDays
    
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 월/년도 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "연도",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${currentMonth}월",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 요일 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("월", "화", "수", "목", "금", "토", "일").forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 날짜 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(allDays) { date ->
                    DayCell(
                        date = date,
                        isCurrentMonth = date.get(Calendar.MONTH) == currentMonth - 1,
                        isToday = date.get(Calendar.DATE) == calendar.get(Calendar.DATE) &&
                                 date.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                 date.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                    )
                }
            }
        }
    }
}

/**
 * 날짜 셀 컴포넌트
 */
@Composable
private fun DayCell(
    date: Calendar,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isCurrentMonth -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val textColor = when {
        isToday -> MaterialTheme.colorScheme.onPrimary
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.get(Calendar.DATE).toString(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
