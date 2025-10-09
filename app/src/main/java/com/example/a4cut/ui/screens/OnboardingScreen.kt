package com.example.a4cut.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 온보딩 화면
 * 앱의 핵심 기능들을 소개하는 3페이지 온보딩
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.Star,
            title = "KTX 네컷으로\n특별한 순간을 기록하세요",
            description = "친구들과의 기차 여행을\n트렌디한 4컷 사진으로 만들어\n즉시 공유해보세요!",
            backgroundColor = Color(0xFF1E3A8A) // KTX 블루
        ),
        OnboardingPage(
            icon = Icons.Default.Info,
            title = "달력으로 추억을\n쉽게 찾아보세요",
            description = "날짜별로 정리된 사진들을\n달력에서 한눈에 확인하고\n특별한 날의 추억을 되새겨보세요.",
            backgroundColor = Color(0xFF059669) // 그린
        ),
        OnboardingPage(
            icon = Icons.Default.Search,
            title = "스마트한 검색으로\n원하는 사진을 찾으세요",
            description = "제목, 태그, 계절, 감정 등\n다양한 조건으로 사진을 검색하고\n완벽한 추억을 찾아보세요.",
            backgroundColor = Color(0xFF7C3AED) // 퍼플
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        pages[currentPage].backgroundColor,
                        pages[currentPage].backgroundColor.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단 여백
            Spacer(modifier = Modifier.height(60.dp))
            
            // 메인 콘텐츠
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // 아이콘
                val iconScale by animateFloatAsState(
                    targetValue = if (currentPage == 0) 1f else 0.8f,
                    animationSpec = tween(300),
                    label = "iconScale"
                )
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = pages[currentPage].icon,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }
                
                // 텍스트 콘텐츠
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = pages[currentPage].title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )
                    
                    Text(
                        text = pages[currentPage].description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
            
            // 하단 컨트롤
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 페이지 인디케이터
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isActive = index == currentPage
                        val width by animateFloatAsState(
                            targetValue = if (isActive) 24f else 8f,
                            animationSpec = tween(300),
                            label = "indicatorWidth"
                        )
                        
                        Box(
                            modifier = Modifier
                                .width(width.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isActive) Color.White else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
                
                // 버튼
                if (currentPage < pages.size - 1) {
                    Button(
                        onClick = { currentPage++ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = pages[currentPage].backgroundColor
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "다음",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = pages[currentPage].backgroundColor
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "시작하기",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // 건너뛰기 버튼
                if (currentPage < pages.size - 1) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text = "건너뛰기",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // 하단 여백
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * 온보딩 페이지 데이터 클래스
 */
data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val backgroundColor: Color
)
