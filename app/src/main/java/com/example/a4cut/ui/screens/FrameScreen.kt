package com.example.a4cut.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.components.FrameCarousel
import com.example.a4cut.ui.components.FramePreview
import com.example.a4cut.ui.components.ImagePreviewDialog
import com.example.a4cut.ui.viewmodel.FrameViewModel

/**
 * 프레임 화면
 * Phase 4.3.2: 이미지 URI 전달 및 개선된 UI 상태 관리 지원
 */
@Composable
fun FrameScreen(
    imageUris: List<String> = emptyList(),
    onNavigateBack: () -> Unit = {},
    frameViewModel: FrameViewModel = viewModel()
) {
    // Phase 4.3.2: 전달받은 이미지 URI를 ViewModel에 설정
    LaunchedEffect(imageUris) {
        if (imageUris.isNotEmpty()) {
            frameViewModel.onImagesSelected(imageUris)
        }
    }
    
    // UI 상태 수집
    val uiState by frameViewModel.uiState.collectAsState()
    
    // 기존 상태들 (하위 호환성 유지)
    val frames by frameViewModel.frames.collectAsState()
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val photos by frameViewModel.photos.collectAsState()
    val isProcessing by frameViewModel.isProcessing.collectAsState()
    val composedImage by frameViewModel.composedImage.collectAsState()
    
    // 이미지 미리보기 다이얼로그 상태
    var showPreviewDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 제목
        Text(
            text = "프레임 선택 및 이미지 합성",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Phase 4.3.2: 개선된 프레임 선택 UI (FrameCarousel 사용)
        FrameCarousel(
            frames = frames,
            isLoading = false,
            selectedFrameId = selectedFrame?.id,
            onFrameSelected = { frame -> frameViewModel.onFrameSelected(frame) },
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Phase 4.3.2: 실시간 프레임 미리보기 (FramePreview 사용)
        FramePreview(
            frame = selectedFrame,
            photos = photos,
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
