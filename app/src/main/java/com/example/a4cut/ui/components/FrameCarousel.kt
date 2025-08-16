package com.example.a4cut.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * 프레임 캐러셀 컴포넌트
 * KTX 프레임들을 가로로 스크롤하여 선택할 수 있습니다
 * Phase 2: 애니메이션 효과 추가 (카드 회전, 하이라이트, 페이지 인디케이터)
 */
@Composable
fun FrameCarousel(
    modifier: Modifier = Modifier
) {
    // 임시 프레임 데이터 (나중에 실제 데이터로 교체)
    val frames = remember {
        listOf(
            FrameData("KTX 기본", "25.07.18", "서울역", "제목"),
            FrameData("KTX 프리미엄", "23.05.05", "부산역", "제목"),
            FrameData("KTX 특별", "16.03.01", "전주역", "제목")
        )
    }
    
    // LazyRow 상태 관리
    val listState = rememberLazyListState()
    
    // 현재 보이는 첫 번째 아이템 인덱스
    val firstVisibleItemIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(frames) { index, frame ->
                FrameCard(
                    frame = frame,
                    index = index,
                    isSelected = index == firstVisibleItemIndex,
                    modifier = Modifier.padding(
                        start = if (index == 0) 16.dp else 0.dp,
                        end = if (index == frames.size - 1) 16.dp else 0.dp
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 페이지 인디케이터 (애니메이션 포함)
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            frames.forEachIndexed { index, _ ->
                AnimatedPageIndicator(
                    isSelected = index == firstVisibleItemIndex,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

/**
 * 프레임 카드 컴포넌트 (애니메이션 포함)
 */
@Composable
private fun FrameCard(
    frame: FrameData,
    index: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    // 선택 상태에 따른 애니메이션 값들
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 2f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "rotation"
    )
    
    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 16f else 8f,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )
    
    Card(
        modifier = modifier
            .width(200.dp)
            .height(280.dp)
            .scale(scale)
            .rotate(rotation),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 날짜
            Text(
                text = frame.date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 프레임 미리보기 (1행 4열) - KTX 티켓 모티브
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            ) {
                // KTX 티켓 테두리 효과
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                )
                            ),
                            RoundedCornerShape(6.dp)
                        )
                )
                
                // 4컷 사진 그리드
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    repeat(4) { photoIndex ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    ),
                                    RoundedCornerShape(6.dp)
                                )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 역 이름
            Text(
                text = frame.station,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 제목
            Text(
                text = frame.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 애니메이션 페이지 인디케이터
 */
@Composable
private fun AnimatedPageIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val size by animateFloatAsState(
        targetValue = if (isSelected) 12f else 8f,
        animationSpec = tween(durationMillis = 300),
        label = "size"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.3f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
            )
    )
}

/**
 * 프레임 데이터 클래스
 */
private data class FrameData(
    val name: String,
    val date: String,
    val station: String,
    val title: String
)
