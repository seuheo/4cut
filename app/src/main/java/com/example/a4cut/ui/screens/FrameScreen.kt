package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.components.PhotoGrid
import com.example.a4cut.ui.components.ImagePreviewDialog
import com.example.a4cut.ui.components.FrameCarousel
import com.example.a4cut.ui.components.FramePreview
import com.example.a4cut.ui.viewmodel.FrameViewModel

/**
 * 프레임 화면
 * 4컷 사진 선택 + 프레임 적용 + 미리보기
 * Phase 4.3.2: 실시간 프레임 미리보기 및 직관적인 프레임 선택 UI 구현
 */
@Composable
fun FrameScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel = viewModel()
) {
    // FrameViewModel에서 상태 수집
    val frames by frameViewModel.frames.collectAsState()
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val photos by frameViewModel.photos.collectAsState()
    val isLoading by frameViewModel.isLoading.collectAsState()
    val isProcessing by frameViewModel.isProcessing.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    val successMessage by frameViewModel.successMessage.collectAsState()
    val composedImage by frameViewModel.composedImage.collectAsState()
    
    // 미리보기 다이얼로그 상태
    var showPreviewDialog by remember { mutableStateOf(false) }
    
    // 이미지 합성 완료 시 자동으로 미리보기 표시
    LaunchedEffect(composedImage) {
        if (composedImage != null && !isProcessing) {
            showPreviewDialog = true
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "KTX 프레임 적용",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 성공 메시지 표시
        successMessage?.let { success ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = success,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // 에러 메시지 표시
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // 4컷 사진 선택 그리드 (PhotoGrid 컴포넌트 사용)
        PhotoGrid(
            photos = photos,
            onPhotoClick = { index -> 
                // 사진 클릭 시 토글 동작: 있으면 제거, 없으면 추가 준비
                frameViewModel.togglePhotoSelection(index)
            },
            onAddPhotoClick = { frameViewModel.openImagePicker() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Phase 4.3.2: 개선된 프레임 선택 UI (FrameCarousel 사용)
        FrameCarousel(
            frames = frames,
            isLoading = isLoading,
            selectedFrameId = selectedFrame?.id,
            onFrameSelected = { frame -> frameViewModel.selectFrame(frame) },
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Phase 4.3.2: 실시간 프레임 미리보기 (FramePreview 사용)
        FramePreview(
            frame = selectedFrame,
            photos = photos.filterNotNull(),
            isLoading = isProcessing,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 액션 버튼들
        Button(
            onClick = { 
                frameViewModel.startImageComposition()
            },
            enabled = selectedFrame != null && photos.any { it != null } && !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("합성 중...")
            } else {
                Text("이미지 합성")
            }
        }
        
        // 합성된 이미지가 있으면 미리보기 버튼 표시
        if (composedImage != null) {
            Button(
                onClick = { showPreviewDialog = true },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("결과물 미리보기")
            }
        }
        
        // 이미지 미리보기 다이얼로그
        if (showPreviewDialog && composedImage != null) {
            ImagePreviewDialog(
                bitmap = composedImage,
                onDismiss = { showPreviewDialog = false },
                onSave = { 
                    frameViewModel.saveImage()
                    showPreviewDialog = false
                },
                onShare = { 
                    frameViewModel.shareToInstagram()
                    showPreviewDialog = false
                },
                isProcessing = isProcessing
            )
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
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = frame.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = frame.station,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            if (frame.isPremium) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PREMIUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
