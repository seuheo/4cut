package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.a4cut.ui.viewmodel.PhotoState

/**
 * Phase 4: 세련된 프레임 선택 화면 - 예시 이미지와 동일한 현대적 UI
 * 상단 미리보기 + 하단 프레임 캐러셀의 직관적인 단일 화면
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
    val photoStates = frameViewModel.photoStates
    val isLoading by frameViewModel.isLoading.collectAsState()
    val isProcessing by frameViewModel.isProcessing.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    val successMessage by frameViewModel.successMessage.collectAsState()
    val composedImage by frameViewModel.composedImage.collectAsState()

    // 미리보기 다이얼로그 상태
    var showPreviewDialog by remember { mutableStateOf(false) }

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

    // Phase 4: 예시 이미지와 동일한 세련된 레이아웃 구성
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)), // 예시 이미지와 유사한 배경색
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 여백
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. 상단 미리보기 영역 (예시 이미지의 상단 영역)
        ModernPhotoPreviewSection(
            photoStates = photoStates,
            selectedFrame = selectedFrame,
            onPhotoClick = { index -> 
                frameViewModel.togglePhotoSelection(index)
            },
            onTransform = { index, scale, offsetX, offsetY ->
                frameViewModel.updatePhotoState(index, scale, offsetX, offsetY)
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 2. 하단 프레임 캐러셀 (예시 이미지의 하단 영역)
        ModernFrameCarouselSection(
            frames = frames,
            selectedFrame = selectedFrame,
            isLoading = isLoading,
            onFrameSelect = { frame ->
                println("FrameScreen: 프레임 선택됨 - ${frame.name} (ID: ${frame.id})")
                frameViewModel.selectFrame(frame)
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 3. 액션 버튼 (예시 이미지의 "확인" 버튼과 유사)
        ModernActionButtonSection(
            selectedFrame = selectedFrame,
            photoStates = photoStates,
            isProcessing = isProcessing,
            onCompose = {
                println("FrameScreen: 이미지 합성 버튼 클릭됨")
                frameViewModel.startImageComposition()
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }

    // 메시지 표시 (성공/에러)
    if (successMessage != null) {
        ModernMessageCard(
            message = successMessage!!,
            isError = false,
            modifier = Modifier.padding(16.dp)
        )
    }

    if (errorMessage != null) {
        ModernMessageCard(
            message = errorMessage!!,
            isError = true,
            modifier = Modifier.padding(16.dp)
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
 * Phase 4: 현대적인 상단 미리보기 섹션 - 예시 이미지와 동일한 세련된 디자인
 * 선택된 사진 4장을 2x2 그리드로 표시하고 프레임을 오버레이
 */
@Composable
private fun ModernPhotoPreviewSection(
    photoStates: List<PhotoState>,
    selectedFrame: Frame?,
    onPhotoClick: (Int) -> Unit,
    onTransform: (Int, Float, Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(0.6f) // 예시 이미지와 유사한 비율
            .background(
                Color.White, 
                RoundedCornerShape(20.dp) // 더 둥근 모서리
            )
            .padding(12.dp), // 내부 여백 증가
        contentAlignment = Alignment.Center
    ) {
        // 사진 4장을 2x2 그리드로 배치 (제스처 편집 가능)
        if (photoStates.any { it.bitmap != null }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp) // 간격 증가
            ) {
                // 상단 행
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModernEditablePhotoItem(
                        photoState = photoStates.getOrNull(0) ?: PhotoState(null),
                        index = 0,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                    ModernEditablePhotoItem(
                        photoState = photoStates.getOrNull(1) ?: PhotoState(null),
                        index = 1,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 하단 행
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ModernEditablePhotoItem(
                        photoState = photoStates.getOrNull(2) ?: PhotoState(null),
                        index = 2,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                    ModernEditablePhotoItem(
                        photoState = photoStates.getOrNull(3) ?: PhotoState(null),
                        index = 3,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // 사진이 없을 때 안내 텍스트
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Photos",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "사진을 추가하고\n프레임을 선택하세요",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 선택된 프레임 오버레이
        selectedFrame?.let { frame ->
            Image(
                painter = painterResource(id = frame.drawableId),
                contentDescription = "Frame Overlay",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

/**
 * Phase 4: 현대적인 제스처 편집 가능한 개별 사진 아이템
 */
@Composable
private fun ModernEditablePhotoItem(
    photoState: PhotoState,
    index: Int,
    onPhotoClick: (Int) -> Unit,
    onTransform: (Int, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                Color(0xFFF3F4F6), // 더 부드러운 회색
                RoundedCornerShape(12.dp) // 더 둥근 모서리
            )
            .clickable { onPhotoClick(index) }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(index, zoom, pan.x, pan.y)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (photoState.bitmap != null) {
            Image(
                bitmap = photoState.bitmap.asImageBitmap(),
                contentDescription = "Photo ${index + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .graphicsLayer(
                        scaleX = photoState.scale,
                        scaleY = photoState.scale,
                        translationX = photoState.offsetX,
                        translationY = photoState.offsetY
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Photo",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "사진 추가",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

/**
 * Phase 4: 현대적인 하단 프레임 캐러셀 섹션 - 예시 이미지와 동일한 세련된 디자인
 * 프레임들을 가로 스크롤로 선택할 수 있는 캐러셀
 */
@Composable
private fun ModernFrameCarouselSection(
    frames: List<Frame>,
    selectedFrame: Frame?,
    isLoading: Boolean,
    onFrameSelect: (Frame) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 프레임 선택 안내 텍스트
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = KTXBlue,
                modifier = Modifier.size(32.dp)
            )
        } else {
            // 프레임 캐러셀 (가로 스크롤)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(frames) { frame ->
                    ModernFrameCarouselItem(
                        frame = frame,
                        isSelected = selectedFrame?.id == frame.id,
                        onClick = { onFrameSelect(frame) }
                    )
                }
            }
        }
    }
}

/**
 * Phase 4: 현대적인 프레임 캐러셀 개별 아이템
 */
@Composable
private fun ModernFrameCarouselItem(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 90.dp else 80.dp) // 선택 시 크기 변화
            .background(
                color = if (isSelected) KTXBlue.copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(16.dp) // 더 둥근 모서리
            )
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
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

        // 선택 상태 표시 (더 세련된 디자인)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        KTXBlue.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
            )
            
            // 선택 표시 아이콘
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        KTXBlue,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Phase 4: 현대적인 액션 버튼 섹션 - 예시 이미지의 "확인" 버튼과 유사
 */
@Composable
private fun ModernActionButtonSection(
    selectedFrame: Frame?,
    photoStates: List<PhotoState>,
    isProcessing: Boolean,
    onCompose: () -> Unit
) {
    val photoCount = photoStates.count { it.bitmap != null }
    val isEnabled = selectedFrame != null && photoCount > 0 && !isProcessing

    Button(
        onClick = onCompose,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) KTXBlue else Color(0xFFD1D5DB)
        ),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp) // 더 둥근 모서리
    ) {
        if (isProcessing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "합성 중...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        } else {
            Text(
                text = "완성하기",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Phase 4: 현대적인 메시지 표시 카드
 */
@Composable
private fun ModernMessageCard(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) Color(0xFFFEF2F2) else Color(0xFFF0F9FF)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Close else Icons.Default.Check,
                contentDescription = if (isError) "Error" else "Success",
                tint = if (isError) Color(0xFFDC2626) else Color(0xFF059669),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (isError) Color(0xFFDC2626) else Color(0xFF059669)
            )
        }
    }
}

