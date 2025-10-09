package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.components.TossPrimaryButton
import com.example.a4cut.ui.components.TossSecondaryButton
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 2단계: 프레임 선택 전용 화면
 * 사용자가 원하는 프레임을 선택하는 두 번째 단계
 */
@Composable
fun FrameSelectionScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // FrameViewModel에서 상태 수집
    val frames by frameViewModel.frames.collectAsState()
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val photos by frameViewModel.photos.collectAsState()
    val isLoading by frameViewModel.isLoading.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    val successMessage by frameViewModel.successMessage.collectAsState()
    
    // 디버그 로그
    LaunchedEffect(photos) {
        println("FrameSelectionScreen: 사진 상태 업데이트 - ${photos.map { it != null }}")
    }
    
    // Context 설정
    LaunchedEffect(Unit) {
        frameViewModel.setContext(context)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 헤더 섹션
        HeaderSection(
            successMessage = successMessage,
            errorMessage = errorMessage,
            photoCount = photos.count { it != null }
        )
        
        // 선택된 사진 미리보기
        SelectedPhotosPreviewSection(photos = photos)
        
        // 디버그 정보 표시
        Text(
            text = "디버그: 사진 개수 = ${photos.count { it != null }}/4, 선택된 프레임 = ${selectedFrame?.name ?: "없음"}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        // 프레임 선택 섹션
        FrameSelectionSection(
            frames = frames,
            selectedFrame = selectedFrame,
            isLoading = isLoading,
            onFrameSelect = { frame ->
                println("FrameSelectionScreen: 프레임 클릭됨 - ${frame.name}")
                frameViewModel.selectFrame(frame)
            }
        )
        
        // 액션 버튼들
        ActionButtonsSection(
            selectedFrame = selectedFrame,
            onNext = onNext,
            onBack = onBack
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
    photoCount: Int
) {
    Column {
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "선택된 사진 ${photoCount}장에 적용할 프레임을 선택해주세요",
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
 * 선택된 사진 미리보기 섹션
 */
@Composable
private fun SelectedPhotosPreviewSection(photos: List<Bitmap?>) {
    Column {
        Text(
            text = "선택된 사진",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(4) { index ->
                val photo = photos.getOrNull(index)
                if (photo != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Image(
                            bitmap = photo.asImageBitmap(),
                            contentDescription = "사진 ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = TextTertiary.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "빈칸",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }
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
    isLoading: Boolean,
    onFrameSelect: (Frame) -> Unit
) {
    Column {
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "마음에 드는 프레임을 선택하세요. 각 프레임은 다른 역의 특색을 담고 있습니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "↓ 아래로 스크롤하여 더 많은 프레임을 확인하세요",
            style = MaterialTheme.typography.bodySmall,
            color = KTXBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KTXBlue)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(frames.size) { index ->
                    val frame = frames[index]
                    FrameCard(
                        frame = frame,
                        isSelected = selectedFrame?.id == frame.id,
                        onClick = { onFrameSelect(frame) },
                        modifier = Modifier.height(120.dp)
                    )
                }
            }
        }
    }
}

/**
 * 프레임 카드
 */
@Composable
private fun FrameCard(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, radius = 200.dp)
            ) { 
                isPressed = true
                onClick()
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> KTXBlue.copy(alpha = 0.2f)
                isPressed -> KTXBlue.copy(alpha = 0.1f)
                else -> SurfaceLight
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isSelected -> 8.dp
                isPressed -> 6.dp
                else -> 2.dp
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // 프레임 미리보기 이미지
            Image(
                painter = painterResource(id = frame.drawableId),
                contentDescription = frame.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            
            // 선택 상태 표시
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            KTXBlue.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                )
            }
            
            // 프레임 정보
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = frame.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) KTXBlue else TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = frame.station,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) KTXBlue else TextSecondary
                )
                Text(
                    text = frame.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) KTXBlue else TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Premium 배지
            if (frame.isPremium) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            KTXBlue,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PREMIUM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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
    selectedFrame: Frame?,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 뒤로가기 버튼
        TossSecondaryButton(
            text = "뒤로가기",
            onClick = onBack,
            modifier = Modifier.weight(1f)
        )
        
        // 다음 단계 버튼
        TossPrimaryButton(
            text = "다음 단계",
            onClick = onNext,
            enabled = selectedFrame != null,
            modifier = Modifier.weight(1f)
        )
    }
    
    // 상태 안내
    Text(
        text = if (selectedFrame == null) "프레임을 선택해주세요" else "선택된 프레임: ${selectedFrame.name}",
        style = MaterialTheme.typography.bodyMedium,
        color = if (selectedFrame != null) KTXBlue else TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    
    // 버튼 상태 디버그
    Text(
        text = "버튼 상태: ${if (selectedFrame != null) "활성화됨" else "비활성화됨"}",
        style = MaterialTheme.typography.bodySmall,
        color = if (selectedFrame != null) KTXBlue else TextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
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
