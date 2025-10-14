package com.example.a4cut.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a4cut.ui.components.InstagramStoryCircle
import com.example.a4cut.ui.components.KtxStationSelector
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.HomeViewModel
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.data.model.KtxStation
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * iOS 스타일 홈 화면
 * 20대 사용자들이 선호하는 세련되고 깔끔한 미니멀리즘 디자인
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
    val selectedKtxStation by homeViewModel.selectedKtxStation.collectAsState()
    
    // Context 설정
    LaunchedEffect(Unit) {
        homeViewModel.setContext(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosColors.systemBackground)
    ) {
        // iOS 스타일 상단 바
        TopAppBar(
            title = {
                Text(
                    text = "KTX 네컷",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = IosColors.label
                )
            },
            actions = {
                // 모든 사진 삭제 버튼
                if (allPhotos.isNotEmpty()) {
                    IconButton(onClick = { 
                        homeViewModel.deleteAllPhotos()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "모든 사진 삭제",
                            tint = IosColors.SystemRed
                        )
                    }
                }
                
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        tint = IosColors.label
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = IosColors.systemBackground,
                titleContentColor = IosColors.label
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // KTX역 선택 섹션 (임시 비활성화)
            // item {
            //     KtxStationSelector(
            //         selectedStation = selectedKtxStation,
            //         onStationSelected = { station ->
            //             homeViewModel.selectKtxStation(station)
            //         },
            //         modifier = Modifier.padding(vertical = 8.dp)
            //     )
            // }

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
                    onNavigateToFrame = onNavigateToFrame,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * iOS 스타일 스토리 섹션
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
        // iOS 스타일 스토리 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "최근 사진",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = IosColors.label
            )
            
            TextButton(
                onClick = { /* TODO: 전체 보기 */ }
            ) {
                Text(
                    text = "모두 보기",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IosColors.SystemBlue
                )
            }
        }

        // iOS 스타일 스토리 리스트
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 새 사진 추가 버튼
            item {
                IOSStoryCircle(
                    onClick = onAddStory,
                    isAddButton = true
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "새 사진 추가",
                        modifier = Modifier.size(20.dp),
                        tint = IosColors.White
                    )
                }
            }

            // 최근 사진 스토리
            if (latestPhoto != null) {
                item {
                    IOSStoryCircle(
                        onClick = { /* TODO: 사진 상세 보기 */ },
                        isAddButton = false
                    ) {
                        AsyncImage(
                            model = latestPhoto.imagePath,
                            contentDescription = "최근 사진",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 더미 스토리들 (개발용)
            items(5) { index ->
                IOSStoryCircle(
                    onClick = { /* TODO: 사진 상세 보기 */ },
                    isAddButton = false
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = IosColors.systemGray5,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = IosColors.label
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
    onNavigateToFrame: () -> Unit,
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
                    color = IosColors.label
            )
            
            Text(
                text = "${photos.size}개",
                style = MaterialTheme.typography.bodySmall,
                    color = IosColors.secondaryLabel
            )
        }

        if (photos.isEmpty()) {
            // 빈 상태
            EmptyFeedState(
                onAddPhoto = onNavigateToFrame,
                modifier = Modifier.padding(32.dp)
            )
        } else {
            // 격자형 피드 레이아웃 - 높이 제한으로 무한 스크롤 문제 해결
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp) // 최대 높이 제한
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "photo_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .background(IosColors.secondarySystemBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
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
                    color = IosColors.secondaryLabel,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "첫 번째 KTX 네컷 사진을 만들어보세요!",
            style = MaterialTheme.typography.bodyLarge,
                    color = IosColors.tertiaryLabel,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddPhoto,
            colors = ButtonDefaults.buttonColors(
                containerColor = IosColors.SystemBlue,
                contentColor = IosColors.White
            ),
            shape = RoundedCornerShape(12.dp)
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

/**
 * iOS 스타일 스토리 서클 컴포넌트
 */
@Composable
private fun IOSStoryCircle(
    onClick: () -> Unit,
    isAddButton: Boolean,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(if (isAddButton) 60.dp else 56.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 외부 원 (테두리)
        Box(
            modifier = Modifier
                .size(if (isAddButton) 60.dp else 56.dp)
                .background(
                    color = if (isAddButton) IosColors.SystemBlue else IosColors.systemGray4,
                    shape = CircleShape
                )
        )
        
        // 내부 원 (콘텐츠)
        Box(
            modifier = Modifier
                .size(if (isAddButton) 52.dp else 48.dp)
                .background(
                    color = IosColors.systemBackground,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}