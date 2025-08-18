package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.viewmodel.FrameApplyViewModel

/**
 * 프레임 적용 화면
 * 선택된 사진에 KTX 프레임을 적용하는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameApplyScreen(
    photoId: Int,
    viewModel: FrameApplyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val photo = uiState.photo
    val selectedFrame = uiState.selectedFrame
    val frames = uiState.frames
    val previewBitmap = uiState.previewBitmap
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage
    val successMessage = uiState.successMessage
    
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프레임 적용") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    // 저장 버튼 (프레임이 선택된 경우에만 활성화)
                    if (selectedFrame != null) {
                        IconButton(
                            onClick = { viewModel.saveFrameAppliedPhoto() },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "저장")
                        }
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
            if (successMessage != null) {
                MessageCard(
                    message = successMessage,
                    isError = false,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            if (errorMessage != null) {
                MessageCard(
                    message = errorMessage,
                    isError = true,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // 1. 선택된 사진 표시
            PhotoPreviewSection(photo = photo)
            
            // 2. 프레임 선택 UI
            FrameSelectionSection(
                frames = frames,
                selectedFrame = selectedFrame,
                onFrameSelected = { frame -> viewModel.selectFrame(frame) }
            )
            
            // 3. 미리보기 섹션 (선택된 프레임이 있을 때만 표시)
            if (selectedFrame != null) {
                FramePreviewSection(
                    photo = photo,
                    frame = selectedFrame,
                    previewBitmap = previewBitmap,
                    isLoading = isLoading
                )
            }
        }
    }
    
    // 메시지 자동 제거
    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            if (successMessage != null) {
                viewModel.clearSuccessMessage()
            }
            if (errorMessage != null) {
                viewModel.clearErrorMessage()
            }
        }
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
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) 
                MaterialTheme.colorScheme.onErrorContainer 
            else 
                MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * 사진 미리보기 섹션
 */
@Composable
private fun PhotoPreviewSection(
    photo: PhotoEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Text(
                text = "원본 사진",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AsyncImage(
                model = photo.imagePath,
                contentDescription = "원본 KTX 네컷 사진",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * 프레임 선택 섹션
 */
@Composable
private fun FrameSelectionSection(
    frames: List<Frame>,
    selectedFrame: Frame?,
    onFrameSelected: (Frame) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(frames) { frame ->
                FrameSelectionCard(
                    frame = frame,
                    isSelected = selectedFrame?.id == frame.id,
                    onClick = { onFrameSelected(frame) }
                )
            }
        }
    }
}

/**
 * 프레임 선택 카드
 */
@Composable
private fun FrameSelectionCard(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(80.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 12.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = frame.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * 프레임 미리보기 섹션
 */
@Composable
private fun FramePreviewSection(
    photo: PhotoEntity,
    frame: Frame,
    previewBitmap: Bitmap?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column {
            Text(
                text = "미리보기",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    previewBitmap != null -> {
                        Image(
                            bitmap = previewBitmap.asImageBitmap(),
                            contentDescription = "프레임 적용 미리보기",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Text(
                            text = "미리보기를 생성할 수 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
