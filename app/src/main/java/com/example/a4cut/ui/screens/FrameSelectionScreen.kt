package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel

/**
 * 인스타그램 스타일 프레임 선택 화면
 * 시각적이고 직관적인 프레임 선택 경험을 제공합니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrameSelectionScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // ViewModel 상태 수집
    val photos by frameViewModel.photos.collectAsState()
    val frames by frameViewModel.frames.collectAsState()
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val isLoading by frameViewModel.isLoading.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    
    // 디버그 로그
    LaunchedEffect(photos) {
        val photoCount = photos.count { it != null }
        println("=== FrameSelectionScreen 디버그 ===")
        println("FrameSelectionScreen: 사진 상태 업데이트 - ${photos.map { it != null }}")
        println("FrameSelectionScreen: 선택된 사진 개수: $photoCount")
        println("FrameSelectionScreen: 사진 크기들: ${photos.map { "${it?.width ?: 0}x${it?.height ?: 0}" }}")
        println("FrameSelectionScreen: 사진 리스트 크기: ${photos.size}")
        photos.forEachIndexed { index, bitmap ->
            println("FrameSelectionScreen: 사진[$index] = ${if (bitmap != null) "있음 (${bitmap.width}x${bitmap.height})" else "없음"}")
        }
        println("=== 디버그 끝 ===")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosColors.systemBackground)
    ) {
        // 인스타그램 스타일 상단 바
        TopAppBar(
            title = {
                Text(
                    text = "프레임 선택",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = IosColors.label
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 선택된 사진 미리보기 섹션
            item {
                SelectedPhotosPreview(
                    photos = photos,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 프레임 선택 섹션
            item {
                FrameSelectionSection(
                    frames = frames,
                    selectedFrame = selectedFrame,
                    onFrameSelect = { frame ->
                        println("FrameSelectionScreen: 프레임 클릭됨 - ${frame.name}")
                        frameViewModel.selectFrame(frame)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 선택된 프레임 정보
            selectedFrame?.let { frame ->
                item {
                    SelectedFrameInfo(
                        frame = frame,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 다음 단계 버튼
            item {
                val photoCount = photos.count { it != null }
                val hasValidPhotos = photoCount > 0
                val hasSelectedFrame = selectedFrame != null
                
                NextStepButton(
                    isEnabled = hasValidPhotos && hasSelectedFrame,
                    isLoading = isLoading,
                    onNext = {
                        println("=== FrameSelectionScreen: 다음 단계 버튼 클릭 ===")
                        println("FrameSelectionScreen: 사진 상태 = ${photos.map { it != null }}")
                        println("FrameSelectionScreen: 선택된 프레임 = ${selectedFrame?.name}")
                        println("FrameSelectionScreen: ResultScreen으로 이동")
                        onNext()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 사진 선택 상태 안내
                if (!hasValidPhotos) {
                    Text(
                        text = "최소 한 장의 사진을 선택해주세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IosColors.SystemRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                } else if (!hasSelectedFrame) {
                    Text(
                        text = "프레임을 선택해주세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IosColors.SystemRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                } else {
                    Text(
                        text = "선택된 사진: ${photoCount}장, 프레임: ${selectedFrame?.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IosColors.SystemBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }

            // 에러 메시지
            errorMessage?.let { message ->
                item {
                    ErrorMessage(
                        message = message,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 선택된 사진 미리보기 섹션
 */
@Composable
private fun SelectedPhotosPreview(
    photos: List<Bitmap?>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "선택된 사진",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
                color = IosColors.label,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            photos.forEachIndexed { index, bitmap ->
                PhotoPreviewItem(
                    bitmap = bitmap,
                    index = index,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

/**
 * 개별 사진 미리보기 아이템
 */
@Composable
private fun PhotoPreviewItem(
    bitmap: Bitmap?,
    index: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IosColors.secondarySystemBackground)
            .border(
                width = 2.dp,
                color = if (bitmap != null) IosColors.SystemBlue else IosColors.systemGray4,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "사진 ${index + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                    color = IosColors.tertiaryLabel
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
    onFrameSelect: (Frame) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
                color = IosColors.label,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(frames) { frame ->
                FrameSelectionItem(
                    frame = frame,
                    isSelected = selectedFrame?.id == frame.id,
                    onClick = { onFrameSelect(frame) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 개별 프레임 선택 아이템
 */
@Composable
private fun FrameSelectionItem(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "frame_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelected) IosColors.SystemBlue.copy(alpha = 0.1f) else IosColors.systemBackground
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) IosColors.SystemBlue else IosColors.systemGray4,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프레임 이미지
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IosColors.secondarySystemBackground),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = frame.drawableId),
                    contentDescription = frame.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 프레임 이름
            Text(
                text = frame.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) IosColors.SystemBlue else IosColors.label,
                textAlign = TextAlign.Center
            )

            // 선택 표시
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = IosColors.SystemBlue,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "선택됨",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 선택된 프레임 정보
 */
@Composable
private fun SelectedFrameInfo(
    frame: Frame,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = IosColors.SystemBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = IosColors.SystemBlue.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = IosColors.SystemBlue
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "선택된 프레임",
                    style = MaterialTheme.typography.bodySmall,
                    color = IosColors.secondaryLabel
                )
                Text(
                    text = frame.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IosColors.SystemBlue
                )
            }
        }
    }
}

/**
 * 다음 단계 버튼
 */
@Composable
private fun NextStepButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onNext,
        enabled = isEnabled && !isLoading,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) IosColors.SystemBlue else IosColors.systemGray3,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "다음 단계",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 에러 메시지
 */
@Composable
private fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = IosColors.SystemRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = IosColors.SystemRed.copy(alpha = 0.3f)
        )
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = IosColors.SystemRed,
            modifier = Modifier.padding(16.dp)
        )
    }
}