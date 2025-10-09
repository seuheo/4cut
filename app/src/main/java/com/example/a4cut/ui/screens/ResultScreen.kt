package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.components.ImagePreviewDialog
import com.example.a4cut.ui.components.TossPrimaryButton
import com.example.a4cut.ui.components.TossSecondaryButton
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel

/**
 * 3단계: 결과 확인 및 저장/공유 화면
 * 최종 4컷 사진을 확인하고 저장/공유하는 마지막 단계
 */
@Composable
fun ResultScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // FrameViewModel에서 상태 수집
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val photos by frameViewModel.photos.collectAsState()
    val composedImage by frameViewModel.composedImage.collectAsState()
    val isProcessing by frameViewModel.isProcessing.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    val successMessage by frameViewModel.successMessage.collectAsState()
    
    // 미리보기 다이얼로그 상태
    var showPreviewDialog by remember { mutableStateOf(false) }
    
    // Context 설정
    LaunchedEffect(Unit) {
        frameViewModel.setContext(context)
    }
    
    // 자동으로 이미지 합성 시작
    LaunchedEffect(selectedFrame, photos) {
        println("ResultScreen: selectedFrame = $selectedFrame, photos = ${photos.map { it != null }}, composedImage = ${composedImage != null}")
        if (selectedFrame != null && photos.any { it != null } && composedImage == null) {
            println("ResultScreen: 이미지 합성 시작")
            frameViewModel.startImageComposition()
        }
    }
    
    // 이미지 합성 완료 시 자동으로 미리보기 표시
    LaunchedEffect(composedImage) {
        if (composedImage != null && !isProcessing) {
            showPreviewDialog = true
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 헤더 섹션
        HeaderSection(
            successMessage = successMessage,
            errorMessage = errorMessage,
            selectedFrame = selectedFrame
        )
        
        // 결과 이미지 섹션
        ResultImageSection(
            composedImage = composedImage,
            isProcessing = isProcessing,
            onPreviewClick = { showPreviewDialog = true }
        )
        
        // 선택된 사진과 프레임 정보
        SelectionInfoSection(
            photos = photos,
            selectedFrame = selectedFrame
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 액션 버튼들
        ActionButtonsSection(
            composedImage = composedImage,
            isProcessing = isProcessing,
            onSave = { 
                frameViewModel.saveImage()
            },
            onShare = { 
                frameViewModel.shareToInstagram()
            },
            onPreview = { 
                showPreviewDialog = true 
            },
            onBack = onBack,
            onRestart = onRestart
        )
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

/**
 * 헤더 섹션
 */
@Composable
private fun HeaderSection(
    successMessage: String?,
    errorMessage: String?,
    selectedFrame: Frame?
) {
    Column {
        Text(
            text = "결과 확인",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "선택한 프레임과 사진으로 만든 4컷 사진을 확인해보세요",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 성공 메시지
        successMessage?.let { success ->
            SuccessMessageCard(message = success)
        }
        
        // 에러 메시지
        errorMessage?.let { error ->
            ErrorMessageCard(message = error)
        }
    }
}

/**
 * 결과 이미지 섹션
 */
@Composable
private fun ResultImageSection(
    composedImage: Bitmap?,
    isProcessing: Boolean,
    onPreviewClick: () -> Unit
) {
    Column {
        Text(
            text = "최종 결과",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    // 처리 중 상태
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = KTXBlue,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "이미지 합성 중...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                } else if (composedImage != null) {
                    // 합성된 이미지 표시
                    Image(
                        bitmap = composedImage.asImageBitmap(),
                        contentDescription = "합성된 4컷 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // 이미지 없음
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "이미지를 생성하는 중입니다...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        
        // 미리보기 버튼
        if (composedImage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            TossSecondaryButton(
                text = "크게 보기",
                onClick = onPreviewClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 선택된 사진과 프레임 정보 섹션
 */
@Composable
private fun SelectionInfoSection(
    photos: List<Bitmap?>,
    selectedFrame: Frame?
) {
    Column {
        Text(
            text = "선택 정보",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 선택된 사진 개수
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "선택된 사진",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${photos.count { it != null }}장",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // 선택된 프레임
                selectedFrame?.let { frame ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "선택된 프레임",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = frame.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "역",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = frame.station,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 액션 버튼 섹션
 */
@Composable
private fun ActionButtonsSection(
    composedImage: Bitmap?,
    isProcessing: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onPreview: () -> Unit,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 메인 액션 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 갤러리에 저장
            TossPrimaryButton(
                text = "갤러리에 저장",
                onClick = onSave,
                enabled = composedImage != null && !isProcessing,
                modifier = Modifier.weight(1f)
            )
            
            // 인스타그램 공유
            TossSecondaryButton(
                text = "인스타그램 공유",
                onClick = onShare,
                enabled = composedImage != null && !isProcessing,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 보조 액션 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 뒤로가기
            TossSecondaryButton(
                text = "뒤로가기",
                onClick = onBack,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f)
            )
            
            // 다시 만들기
            TossSecondaryButton(
                text = "다시 만들기",
                onClick = onRestart,
                enabled = !isProcessing,
                modifier = Modifier.weight(1f)
            )
        }
        
        // 상태 안내
        val statusText = when {
            isProcessing -> "이미지 합성 중..."
            composedImage == null -> "이미지를 생성하는 중입니다..."
            else -> "저장하거나 공유해보세요!"
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (composedImage != null) KTXBlue else TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 성공 메시지 카드
 */
@Composable
private fun SuccessMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = KTXBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = KTXBlue,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 에러 메시지 카드
 */
@Composable
private fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

