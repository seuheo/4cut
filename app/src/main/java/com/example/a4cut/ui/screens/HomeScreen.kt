package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.components.PhotoLogCard
import com.example.a4cut.ui.viewmodel.HomeViewModel
import com.example.a4cut.data.database.entity.PhotoEntity

/**
 * 새로운 홈 화면 - 포토로그 중심
 * 사용자의 KTX 네컷 사진 기록을 보여주는 트렌디한 디자인
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    onNavigateToFrame: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // ViewModel 초기화 - 안전한 초기화
    LaunchedEffect(Unit) {
        try {
            homeViewModel.setContext(context)
        } catch (e: Exception) {
            // 초기화 실패 시 기본 상태 유지
            e.printStackTrace()
        }
    }
    
    // ViewModel의 상태들을 수집
    val photoLogs by homeViewModel.photoLogs.collectAsState()
    val photoCount by homeViewModel.photoCount.collectAsState()
    val favoritePhotoCount by homeViewModel.favoritePhotoCount.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 헤더 영역
        HeaderSection(
            photoCount = photoCount,
            favoritePhotoCount = favoritePhotoCount,
            onRefresh = { homeViewModel.refreshData() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 메인 콘텐츠 영역
        if (isLoading) {
            LoadingSection()
        } else if (photoLogs.isEmpty()) {
            EmptyStateSection(onNavigateToFrame = onNavigateToFrame)
        } else {
            PhotoLogSection(
                photoLogs = photoLogs,
                onFavoriteToggle = { photo -> homeViewModel.toggleFavorite(photo) },
                onCardClick = { photo -> /* TODO: 상세 보기 */ }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 하단 액션 버튼
        ActionButtonSection(onNavigateToFrame = onNavigateToFrame)
        
        // 에러 메시지 표시
        errorMessage?.let { message ->
            ErrorMessageSection(message = message)
        }
    }
}

/**
 * 상단 헤더 섹션
 */
@Composable
private fun HeaderSection(
    photoCount: Int,
    favoritePhotoCount: Int,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KTX와 함께한 ${photoCount}개의 추억",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (favoritePhotoCount > 0) {
                    Text(
                        text = "즐겨찾기 ${favoritePhotoCount}개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "새로고침",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 로딩 상태 섹션
 */
@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 빈 상태 섹션
 */
@Composable
private fun EmptyStateSection(onNavigateToFrame: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "추억",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "아직 저장된 추억이 없어요!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "첫 번째 KTX 네컷 사진을 만들어보세요",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 포토로그 섹션
 */
@Composable
private fun PhotoLogSection(
    photoLogs: List<PhotoEntity>,
    onFavoriteToggle: (PhotoEntity) -> Unit,
    onCardClick: (PhotoEntity) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(photoLogs) { photo ->
            PhotoLogCard(
                photo = photo,
                onFavoriteToggle = onFavoriteToggle,
                onCardClick = onCardClick
            )
        }
    }
}

/**
 * 액션 버튼 섹션
 */
@Composable
private fun ActionButtonSection(onNavigateToFrame: () -> Unit) {
    Button(
        onClick = onNavigateToFrame,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E3A8A), // KTX 블루
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "추가",
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "새로운 네컷사진 만들기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 에러 메시지 섹션
 */
@Composable
private fun ErrorMessageSection(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
