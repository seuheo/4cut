package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.a4cut.data.database.entity.PhotoEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * 사진 상세 보기 화면
 * 선택된 사진의 전체 화면 보기 및 모든 메타데이터 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    viewModel: com.example.a4cut.ui.viewmodel.PhotoDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val photo = uiState.photo
    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault()) }
    
    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 사진이 없으면 로딩 표시
    if (photo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // 삭제 확인 다이얼로그
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("사진 삭제") },
            text = { Text("정말 이 사진을 삭제하시겠습니까?\n삭제된 사진은 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhoto()
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    // 뒤로가기 네비게이션 처리
    LaunchedEffect(uiState.shouldNavigateBack) {
        if (uiState.shouldNavigateBack) {
            onNavigateBack()
            viewModel.resetNavigationState()
        }
    }
    
    // 메시지 표시
    LaunchedEffect(uiState.message, uiState.errorMessage) {
        if (uiState.message != null || uiState.errorMessage != null) {
            // 3초 후 메시지 자동 제거
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사진 상세 보기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    // 즐겨찾기 토글 버튼
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (photo.isFavorite) "즐겨찾기 해제" else "즐겨찾기 추가",
                            tint = if (photo.isFavorite) Color.Red else Color.Gray
                        )
                    }
                    
                    // 편집 버튼
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "편집")
                    }
                    
                    // 삭제 버튼
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 메시지 표시
            if (uiState.message != null) {
                MessageCard(
                    message = uiState.message!!,
                    isError = false,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            if (uiState.errorMessage != null) {
                MessageCard(
                    message = uiState.errorMessage!!,
                    isError = true,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // 1. 전체 화면 사진 표시
            PhotoDisplaySection(photo = photo)
            
            // 2. 메타데이터 정보 섹션
            MetadataSection(photo = photo, dateFormat = dateFormat)
            
            // 3. KTX 브랜드 태그
            KTXBrandSection()
        }
    }
}

/**
 * 사진 전체 화면 표시 섹션
 */
@Composable
private fun PhotoDisplaySection(
    photo: PhotoEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AsyncImage(
            model = photo.imagePath,
            contentDescription = "KTX 네컷 사진",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * 메타데이터 정보 표시 섹션
 */
@Composable
private fun MetadataSection(
    photo: PhotoEntity,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 제목
        if (photo.title.isNotEmpty()) {
            Text(
                text = photo.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 기본 정보 그리드
        BasicInfoGrid(photo = photo, dateFormat = dateFormat)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 상세 메타데이터
        DetailedMetadataSection(photo = photo)
    }
}

/**
 * 기본 정보 그리드 (위치, 날짜, 프레임 타입)
 */
@Composable
private fun BasicInfoGrid(
    photo: PhotoEntity,
    dateFormat: SimpleDateFormat,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 위치 정보
        InfoCard(
            icon = Icons.Default.LocationOn,
            title = "위치",
            value = photo.location.ifEmpty { "위치 정보 없음" },
            modifier = Modifier.weight(1f)
        )
        
        // 날짜 정보
        InfoCard(
            icon = Icons.Default.DateRange,
            title = "촬영일",
            value = dateFormat.format(Date(photo.createdAt)),
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
                // 프레임 타입
            InfoCard(
                icon = Icons.Default.Info,
                title = "프레임",
                value = photo.frameType.replace("_", " ").replaceFirstChar { it.uppercase() },
                modifier = Modifier.fillMaxWidth()
            )
}

/**
 * 상세 메타데이터 섹션
 */
@Composable
private fun DetailedMetadataSection(
    photo: PhotoEntity,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "추억 정보",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 계절 및 시간대
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (photo.season.isNotEmpty()) {
                InfoChip(
                    icon = Icons.Default.Star,
                    label = photo.season,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (photo.timeOfDay.isNotEmpty()) {
                InfoChip(
                    icon = Icons.Default.Info,
                    label = photo.timeOfDay,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 날씨 및 감정
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (photo.weather.isNotEmpty()) {
                InfoChip(
                    icon = Icons.Default.Info,
                    label = photo.weather,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (photo.mood.isNotEmpty()) {
                InfoChip(
                    icon = Icons.Default.Favorite,
                    label = photo.mood,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 동반자 및 여행 목적
        if (photo.companions.isNotEmpty()) {
            InfoCard(
                icon = Icons.Default.Person,
                title = "동반자",
                value = photo.companions,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        if (photo.travelPurpose.isNotEmpty()) {
            InfoCard(
                icon = Icons.Default.Info,
                title = "여행 목적",
                value = photo.travelPurpose,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // 태그
        if (photo.tags.isNotEmpty()) {
            TagsSection(tags = photo.tags)
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // 설명
        if (photo.description.isNotEmpty()) {
            InfoCard(
                icon = Icons.Default.Info,
                title = "설명",
                value = photo.description,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 정보 카드 컴포넌트
 */
@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 정보 칩 컴포넌트
 */
@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = { },
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(label)
            }
        },
        modifier = modifier
    )
}

/**
 * 태그 섹션
 */
@Composable
private fun TagsSection(
    tags: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "태그",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.split(",").forEach { tag ->
                if (tag.trim().isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(tag.trim()) }
                    )
                }
            }
        }
    }
}

/**
 * KTX 브랜드 섹션
 */
@Composable
private fun KTXBrandSection(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "KTX",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "KTX와 함께한 특별한 추억",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * FlowRow 컴포넌트 (태그 배치용)
 */
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    // 간단한 FlowRow 구현
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}

/**
 * 메시지 표시 카드
 */
@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = if (isError) "오류" else "성공",
                tint = if (isError) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else 
                    MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else 
                    MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
