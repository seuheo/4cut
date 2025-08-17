package com.example.a4cut.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Calendar

/**
 * 캘린더 뷰 컴포넌트
 * 현재 월의 달력을 표시합니다
 * Phase 2: ViewModel 연동 및 상태 관리 개선
 * Phase 3: State Hoisting 패턴 적용으로 재사용성 향상
 */
@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    currentMonth: Int,
    currentYear: Int,
    selectedDate: Calendar?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelect: (Calendar) -> Unit,
    isSpecialDay: (Calendar) -> Boolean
) {
    // 현재 월의 첫 번째 날과 마지막 날
    val firstDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth, 1)
    }
    val lastDayOfMonth = Calendar.getInstance().apply {
        set(currentYear, currentMonth + 1, 1)
        add(Calendar.DATE, -1)
    }
    
    // 이전 달의 마지막 날들 (첫 주를 채우기 위해)
    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
    val previousMonthDays = (1..<firstDayOfWeek).map { 
        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth - 1, 1)
        cal.add(Calendar.DATE, -1)
        cal.add(Calendar.DATE, -(firstDayOfWeek - it - 1))
        cal
    }
    
    // 현재 월의 날짜들
    val currentMonthDays = (1..lastDayOfMonth.get(Calendar.DATE)).map { day ->
        Calendar.getInstance().apply {
            set(currentYear, currentMonth, day)
        }
    }
    
    // 다음 달의 첫 번째 날들 (마지막 주를 채우기 위해)
    val lastDayOfWeek = lastDayOfMonth.get(Calendar.DATE)
    val lastDayOfWeekInCalendar = Calendar.getInstance().apply {
        set(currentYear, currentMonth, lastDayOfWeek)
    }.get(Calendar.DAY_OF_WEEK)
    
    val nextMonthDays = (1..(7 - lastDayOfWeekInCalendar)).map { day ->
        Calendar.getInstance().apply {
            set(currentYear, currentMonth + 1, day)
        }
    }
    
    val allDays = previousMonthDays + currentMonthDays + nextMonthDays
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    when {
                        dragAmount.x > 50 -> onPreviousMonth() // 오른쪽으로 스와이프
                        dragAmount.x < -50 -> onNextMonth()     // 왼쪽으로 스와이프
                    }
                }
            },
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
            // 월/년도 헤더 (화살표 버튼 포함)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(1.2f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 달",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${currentYear}년",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currentMonth + 1}월",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(40.dp)
                        .scale(1.2f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 달",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
                        color = if (dayOfWeek == "토") {
                            Color(0xFF3B82F6) // 파란색
                        } else if (dayOfWeek == "일") {
                            Color(0xFFEF4444) // 빨간색
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
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
                        isCurrentMonth = date.get(Calendar.MONTH) == currentMonth,
                        isToday = date.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE) &&
                                 date.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                                 date.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR),
                        isSelected = selectedDate?.let { selected ->
                            date.get(Calendar.DATE) == selected.get(Calendar.DATE) &&
                            date.get(Calendar.MONTH) == selected.get(Calendar.MONTH) &&
                            date.get(Calendar.YEAR) == selected.get(Calendar.YEAR)
                        } ?: false,
                        isSpecialDay = isSpecialDay(date), // 전달받은 함수로 특별한 날 체크
                        onClick = {
                            if (date.get(Calendar.MONTH) == currentMonth) {
                                onDateSelect(date)
                            }
                        }
                    )
                }
            }
            
            // 선택된 날짜 정보 표시
            selectedDate?.let { selected ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "선택된 날짜: ${selected.get(Calendar.YEAR)}년 ${selected.get(Calendar.MONTH) + 1}월 ${selected.get(Calendar.DATE)}일",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 날짜 셀 컴포넌트 (상호작용 포함)
 */
@Composable
private fun DayCell(
    date: Calendar,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isSpecialDay: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200)
    )
    
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        isSpecialDay -> Color(0xFFF59E0B) // KTX 오렌지
        isCurrentMonth -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimary
        isSpecialDay -> Color.White
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    
    Box(
        modifier = modifier
            .size(32.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(enabled = isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.get(Calendar.DATE).toString(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center
        )
        
        // 특별한 날 마커
        if (isSpecialDay && !isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
