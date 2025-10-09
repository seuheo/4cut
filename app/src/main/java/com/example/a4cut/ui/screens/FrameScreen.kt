package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.components.PhotoGrid
import com.example.a4cut.ui.components.ImagePreviewDialog
import com.example.a4cut.ui.components.TossPrimaryButton
import com.example.a4cut.ui.components.TossSecondaryButton
import com.example.a4cut.ui.components.TossTextButton
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel

/**
 * 토스 스타일 프레임 화면 - 깔끔하고 직관적인 4컷 사진 제작
 * 4컷 사진 선택 + 프레임 적용 + 미리보기
 */
@Composable
fun FrameScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // FrameViewModel에서 상태 수집
    val frames by frameViewModel.frames.collectAsState()
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val photos by frameViewModel.photos.collectAsState()
    val isLoading by frameViewModel.isLoading.collectAsState()
    val isProcessing by frameViewModel.isProcessing.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    val successMessage by frameViewModel.successMessage.collectAsState()
    val composedImage by frameViewModel.composedImage.collectAsState()
    val life4CutExample by frameViewModel.life4CutExample.collectAsState()
    
    // 미리보기 다이얼로그 상태
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showLife4CutExample by remember { mutableStateOf(false) }
    
    // Context 설정
    LaunchedEffect(Unit) {
        frameViewModel.setContext(context)
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
            errorMessage = errorMessage
        )
        
        // 4컷 사진 선택 그리드 (크기 축소)
        PhotoSelectionSection(
            photos = photos,
            onPhotoClick = { index -> 
                frameViewModel.togglePhotoSelection(index)
            }
        )
        
        // 테스트용 사진 선택 버튼들 (에뮬레이터용)
        TestPhotoButtonsSection(
            onSelectRandomPhoto = { frameViewModel.selectRandomTestPhoto() },
            onSelectTestPhoto1 = { frameViewModel.selectTestPhoto(0, 0) },
            onSelectTestPhoto2 = { frameViewModel.selectTestPhoto(1, 1) },
            onSelectTestPhoto3 = { frameViewModel.selectTestPhoto(2, 2) },
            onSelectTestPhoto4 = { frameViewModel.selectTestPhoto(3, 3) },
            onClearAllPhotos = { 
                repeat(4) { index ->
                    frameViewModel.removePhoto(index)
                }
            }
        )
        
        // 프레임 선택 섹션
        FrameSelectionSection(
            frames = frames,
            selectedFrame = selectedFrame,
            isLoading = isLoading,
            onFrameSelect = { frame ->
                println("FrameScreen: 프레임 선택됨 - ${frame.name} (ID: ${frame.id})")
                frameViewModel.selectFrame(frame)
            }
        )
        
        // 액션 버튼 섹션
        ActionButtonsSection(
            selectedFrame = selectedFrame,
            photos = photos,
            isProcessing = isProcessing,
            composedImage = composedImage,
            life4CutExample = life4CutExample,
            onCompose = { 
                println("FrameScreen: 이미지 합성 버튼 클릭됨")
                frameViewModel.startImageComposition() 
            },
            onPreview = { 
                println("FrameScreen: 미리보기 버튼 클릭됨")
                showPreviewDialog = true 
            },
            onShowExample = { 
                println("FrameScreen: 예시 보기 버튼 클릭됨")
                showLife4CutExample = true 
            },
            onGenerateNewExample = {
                println("FrameScreen: 새로운 예시 생성 버튼 클릭됨")
                frameViewModel.generateRandomLife4CutExample()
            }
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
    
    // 인생네컷 예시 다이얼로그
    if (showLife4CutExample && life4CutExample != null) {
        ImagePreviewDialog(
            bitmap = life4CutExample,
            onDismiss = { showLife4CutExample = false },
            onSave = { showLife4CutExample = false },
            onShare = { showLife4CutExample = false },
            title = "인생네컷 예시"
        )
    }
}

/**
 * 헤더 섹션 - 토스 스타일
 */
@Composable
private fun HeaderSection(
    successMessage: String?,
    errorMessage: String?
) {
    Column {
        Text(
            text = "KTX 프레임 적용",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "4컷 사진을 선택하고 마음에 드는 프레임을 적용해보세요",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        
        // 성공 메시지
        successMessage?.let { success ->
            Spacer(modifier = Modifier.height(16.dp))
            SuccessMessageCard(message = success)
        }
        
        // 에러 메시지
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            ErrorMessageCard(message = error)
        }
    }
}

/**
 * 사진 선택 섹션 - 토스 스타일
 */
@Composable
private fun PhotoSelectionSection(
    photos: List<Bitmap?>,
    onPhotoClick: (Int) -> Unit
) {
    Column {
        Text(
            text = "4컷 사진 선택",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 4컷 사진 그리드 직접 구현 (크기 축소)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp) // 400dp → 200dp로 축소
        ) {
            items(4) { index ->
                PhotoGridItem(
                    index = index,
                    photo = photos.getOrNull(index),
                    onPhotoClick = onPhotoClick
                )
            }
        }
    }
}

/**
 * 프레임 선택 섹션 - 토스 스타일
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
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KTXBlue)
            }
        } else {
            // 프레임을 2열 그리드로 표시하여 스크롤 영역 최소화
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp) // 고정 높이로 스크롤 가능하게
            ) {
                items(frames.size) { index ->
                    val frame = frames[index]
                    TossStyleFrameCard(
                        frame = frame,
                        isSelected = selectedFrame?.id == frame.id,
                        onClick = { onFrameSelect(frame) },
                        modifier = Modifier.height(90.dp) // 카드 높이 고정
                    )
                }
            }
        }
    }
}

/**
 * 액션 버튼 섹션 - 토스 스타일
 */
@Composable
private fun ActionButtonsSection(
    selectedFrame: Frame?,
    photos: List<Bitmap?>,
    isProcessing: Boolean,
    composedImage: Bitmap?,
    life4CutExample: Bitmap?,
    onCompose: () -> Unit,
    onPreview: () -> Unit,
    onShowExample: () -> Unit,
    onGenerateNewExample: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 상태 안내
        val photoCount = photos.count { it != null }
        val statusText = when {
            selectedFrame == null -> "프레임을 선택해주세요"
            photoCount == 0 -> "사진을 선택해주세요"
            isProcessing -> "이미지 합성 중..."
            else -> "선택된 프레임: ${selectedFrame.name} (사진 ${photoCount}장)"
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selectedFrame != null && photoCount > 0) KTXBlue else TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 메인 액션 버튼
        TossPrimaryButton(
            text = if (isProcessing) "합성 중..." else "이미지 합성하기",
            onClick = onCompose,
            enabled = selectedFrame != null && photos.any { it != null } && !isProcessing,
            modifier = Modifier.fillMaxWidth()
        )
        
        // 보조 액션 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 인생네컷 예시 버튼
            if (life4CutExample != null) {
                TossSecondaryButton(
                    text = "예시 보기",
                    onClick = onShowExample,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 미리보기 버튼
            if (composedImage != null) {
                TossSecondaryButton(
                    text = "미리보기",
                    onClick = onPreview,
                    enabled = !isProcessing,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 새로운 예시 생성 버튼
        if (life4CutExample != null) {
            TossTextButton(
                text = "🔄 새로운 예시 보기",
                onClick = onGenerateNewExample,
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 토스 스타일 프레임 카드
 */
@Composable
private fun TossStyleFrameCard(
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
            .height(90.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, radius = 200.dp)
            ) { 
                isPressed = true
                onClick()
                // 클릭 피드백을 위한 짧은 지연
                coroutineScope.launch {
                    kotlinx.coroutines.delay(100)
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
            // 프레임 미리보기 이미지 (실제 프레임 이미지 사용)
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
 * 성공 메시지 카드 - 토스 스타일
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
 * 에러 메시지 카드 - 토스 스타일
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

/**
 * 테스트용 사진 선택 버튼 섹션 - 에뮬레이터용
 */
@Composable
private fun TestPhotoButtonsSection(
    onSelectRandomPhoto: () -> Unit,
    onSelectTestPhoto1: () -> Unit,
    onSelectTestPhoto2: () -> Unit,
    onSelectTestPhoto3: () -> Unit,
    onSelectTestPhoto4: () -> Unit,
    onClearAllPhotos: () -> Unit
) {
    Column {
        Text(
            text = "테스트용 사진 선택 (에뮬레이터용)",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "마우스 클릭으로 쉽게 사진을 선택하고 테스트할 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 랜덤 사진 선택 버튼
            Button(
                onClick = onSelectRandomPhoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KTXBlue
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "랜덤",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            // 테스트 사진 1-4 선택 버튼들
            Button(
                onClick = onSelectTestPhoto1,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KTXBlue.copy(alpha = 0.8f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "사진1",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Button(
                onClick = onSelectTestPhoto2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KTXBlue.copy(alpha = 0.8f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "사진2",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Button(
                onClick = onSelectTestPhoto3,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KTXBlue.copy(alpha = 0.8f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "사진3",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Button(
                onClick = onSelectTestPhoto4,
                colors = ButtonDefaults.buttonColors(
                    containerColor = KTXBlue.copy(alpha = 0.8f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "사진4",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        
        // 모든 사진 제거 버튼
        Button(
            onClick = onClearAllPhotos,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "모든 사진 제거",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * 사진 그리드 아이템 - 토스 스타일 (에뮬레이터 최적화)
 */
@Composable
private fun PhotoGridItem(
    index: Int,
    photo: Bitmap?,
    onPhotoClick: (Int) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, radius = 200.dp)
            ) { 
                isPressed = true
                onPhotoClick(index)
                // 클릭 피드백을 위한 짧은 지연
                coroutineScope.launch {
                    kotlinx.coroutines.delay(100)
                    isPressed = false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) KTXBlue.copy(alpha = 0.1f) else SurfaceLight
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (photo != null) {
                // 사진이 있을 때
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = "사진 ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // 사진 위에 제거 버튼 오버레이
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .size(24.dp)
                        .clickable { onPhotoClick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "사진 제거",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // 사진이 없을 때 추가 버튼
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "사진 추가",
                        tint = if (isPressed) KTXBlue else TextSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "사진 추가",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPressed) KTXBlue else TextSecondary
                    )
                }
            }
        }
    }
}
