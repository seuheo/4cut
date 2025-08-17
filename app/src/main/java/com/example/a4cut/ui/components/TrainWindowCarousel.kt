package com.example.a4cut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.a4cut.data.database.entity.PhotoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainWindowCarousel(
    photos: List<PhotoEntity>,
    onPhotoClick: (PhotoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (photos.isEmpty()) {
        // 사진이 없을 때는 빈 상태 표시
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "아직 기록된 여행이 없어요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val lazyListState = rememberLazyListState()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // KTX 기차 배경 일러스트
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            // 기차 창문 효과를 위한 배경
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF2C3E50),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
            
            // 기차 창문 프레임들
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) { _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = Color(0xFF34495E),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 2.dp,
                                color = Color(0xFF95A5A6),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
        
        // 사진 캐러셀
        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(photos) { _, photo ->
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(200.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    // 사진 표시
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = if (photo.title.isNotEmpty()) photo.title else "여행 사진",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 사진 정보 오버레이
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 상단: 제목
                        if (photo.title.isNotEmpty()) {
                            Text(
                                text = photo.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        
                        // 하단: 설명
                        if (photo.description.isNotEmpty()) {
                            Text(
                                text = photo.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier
                                    .background(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    
                    // 클릭 이벤트
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onPhotoClick(photo) }
                    )
                }
            }
        }
        
        // 페이지 인디케이터
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(photos.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .background(
                            color = if (lazyListState.firstVisibleItemIndex == index) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
