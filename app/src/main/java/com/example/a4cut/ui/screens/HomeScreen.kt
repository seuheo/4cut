package com.example.a4cut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a4cut.ui.components.InstagramStoryCircle
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.HomeViewModel
import com.example.a4cut.data.database.entity.PhotoEntity
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * 인스타그램 스타일 홈 화면
 * 상단에 스토리 UI, 하단에 격자형 피드 레이아웃을 적용합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPhotoDetail: (String) -> Unit,
    onNavigateToFrame: () -> Unit,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    
    // ViewModel의 상태들을 수집
    val latestPhoto by homeViewModel.latestPhoto.collectAsState()
    val datesWithPhotos by homeViewModel.datesWithPhotos.collectAsState()
    val allPhotos by homeViewModel.allPhotos.collectAsState()
    val isTestMode by homeViewModel.isTestMode.collectAsState()
    
    // 테스트 모드 토글
    LaunchedEffect(Unit) {
        homeViewModel.toggleTestMode()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // 인스타그램 스타일 상단 바
        TopAppBar(
            title = {
                Text(
                    text = "KTX 네컷",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceLight,
                titleContentColor = TextPrimary
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 스토리 섹션
            item {
                StorySection(
                    latestPhoto = latestPhoto,
                    onAddStory = onNavigateToFrame,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // 피드 섹션
            item {
                FeedSection(
                    photos = allPhotos,
                    onPhotoClick = onNavigateToPhotoDetail,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * 인스타그램 스타일 스토리 섹션
 */
@Composable
private fun StorySection(
    latestPhoto: PhotoEntity?,
    onAddStory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 스토리 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "스토리",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            TextButton(
                onClick = { /* TODO: 스토리 전체 보기 */ }
            ) {
                Text(
                    text = "모두 보기",
                    style = MaterialTheme.typography.labelLarge,
                    color = InstagramBlue
                )
            }
        }

        // 스토리 리스트
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 새 스토리 추가 버튼
            item {
                InstagramStoryCircle(
                    onClick = onAddStory,
                    isViewed = false
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "새 스토리 추가",
                        modifier = Modifier.size(24.dp),
                        tint = TextPrimary
                    )
                }
            }

            // 최근 사진 스토리
            if (latestPhoto != null) {
                item {
                    InstagramStoryCircle(
                        onClick = { /* TODO: 스토리 상세 보기 */ },
                        isViewed = false
                    ) {
                        AsyncImage(
                            model = latestPhoto.imagePath,
                            contentDescription = "최근 사진",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 더미 스토리들 (개발용)
            items(5) { index ->
                InstagramStoryCircle(
                    onClick = { /* TODO: 스토리 상세 보기 */ },
                    isViewed = index % 2 == 0
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = when (index % 3) {
                                    0 -> KTXBlue.copy(alpha = 0.3f)
                                    1 -> InstagramBlue.copy(alpha = 0.3f)
                                    else -> LikeRed.copy(alpha = 0.3f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 인스타그램 스타일 피드 섹션
 */
@Composable
private fun FeedSection(
    photos: List<PhotoEntity>,
    onPhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 피드 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "내 사진",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            
            Text(
                text = "${photos.size}개",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        if (photos.isEmpty()) {
            // 빈 상태
            EmptyFeedState(
                onAddPhoto = { /* TODO: 사진 추가 */ },
                modifier = Modifier.padding(32.dp)
            )
        } else {
            // 격자형 피드 레이아웃
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(photos) { photo ->
                    FeedPhotoItem(
                        photo = photo,
                        onClick = { onPhotoClick(photo.id.toString()) },
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

/**
 * 피드 사진 아이템
 */
@Composable
private fun FeedPhotoItem(
    photo: PhotoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(BackgroundSecondary)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = photo.imagePath,
            contentDescription = photo.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 오버레이 정보
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f)
                        )
                    )
                )
        )
        
        // 하단 정보
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = photo.title ?: "제목 없음",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1
            )
            
            if (photo.location != null) {
                Text(
                    text = photo.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 빈 피드 상태
 */
@Composable
private fun EmptyFeedState(
    onAddPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "아직 사진이 없어요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "첫 번째 KTX 네컷 사진을 만들어보세요!",
            style = MaterialTheme.typography.bodyLarge,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddPhoto,
            colors = ButtonDefaults.buttonColors(
                containerColor = InstagramBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "사진 만들기",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}