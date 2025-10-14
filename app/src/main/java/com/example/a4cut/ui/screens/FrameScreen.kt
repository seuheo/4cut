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
 * Phase 1: 통합 프레임 적용 화면 - 예시 이미지와 동일한 레이아웃
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
    val photos by frameViewModel.photos.collectAsState()
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
    
    // Phase 1: 예시 이미지와 동일한 레이아웃 구성
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // 예시 이미지와 유사한 배경색
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 상단 미리보기 영역 (예시 이미지의 상단 2/3 영역)
        Spacer(modifier = Modifier.height(32.dp))
        
        PhotoPreviewSection(
            photos = photos,
            selectedFrame = selectedFrame,
            onPhotoClick = { index -> 
                frameViewModel.togglePhotoSelection(index)
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 2. 하단 프레임 캐러셀 (예시 이미지의 하단 1/3 영역)
        FrameCarouselSection(
            frames = frames,
            selectedFrame = selectedFrame,
            isLoading = isLoading,
            onFrameSelect = { frame ->
                println("FrameScreen: 프레임 선택됨 - ${frame.name} (ID: ${frame.id})")
                frameViewModel.selectFrame(frame)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 3. 액션 버튼 (예시 이미지의 "확인" 버튼과 유사)
        ActionButtonSection(
            selectedFrame = selectedFrame,
            photos = photos,
            isProcessing = isProcessing,
            onCompose = { 
                println("FrameScreen: 이미지 합성 버튼 클릭됨")
                frameViewModel.startImageComposition() 
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
    
    // 메시지 표시 (성공/에러)
    if (successMessage != null) {
        MessageCard(
            message = successMessage!!,
            isError = false,
            modifier = Modifier.padding(16.dp)
        )
    }
    
    if (errorMessage != null) {
        MessageCard(
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
 * Phase 1: 상단 미리보기 섹션 - 예시 이미지와 동일한 레이아웃
 * 선택된 사진 4장을 2x2 그리드로 표시하고 프레임을 오버레이
 */
@Composable
private fun PhotoPreviewSection(
    photos: List<Bitmap?>,
    selectedFrame: Frame?,
    onPhotoClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(0.56f) // 인생네컷 비율 (9:16)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 사진 4장을 2x2 그리드로 배치
        if (photos.any { it != null }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 상단 행
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PhotoPreviewItem(
                        photo = photos.getOrNull(0),
                        index = 0,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f)
                    )
                    PhotoPreviewItem(
                        photo = photos.getOrNull(1),
                        index = 1,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 하단 행
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PhotoPreviewItem(
                        photo = photos.getOrNull(2),
                        index = 2,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f)
                    )
                    PhotoPreviewItem(
                        photo = photos.getOrNull(3),
                        index = 3,
                        onPhotoClick = onPhotoClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // 사진이 없을 때 안내 텍스트
            Text(
                text = "프레임을 선택하세요",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
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
 * 개별 사진 미리보기 아이템
 */
@Composable
private fun PhotoPreviewItem(
    photo: Bitmap?,
    index: Int,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { onPhotoClick(index) },
        contentAlignment = Alignment.Center
    ) {
        if (photo != null) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = "Photo ${index + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Photo",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Phase 1: 하단 프레임 캐러셀 섹션 - 예시 이미지와 동일한 레이아웃
 * 프레임들을 가로 스크롤로 선택할 수 있는 캐러셀
 */
@Composable
private fun FrameCarouselSection(
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
            text = "프레임 만들기",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (isLoading) {
            CircularProgressIndicator(color = KTXBlue)
        } else {
            // 프레임 캐러셀 (가로 스크롤)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(frames) { frame ->
                    FrameCarouselItem(
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
 * 프레임 캐러셀 개별 아이템
 */
@Composable
private fun FrameCarouselItem(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(
                color = if (isSelected) KTXBlue.copy(alpha = 0.2f) else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // 프레임 미리보기 이미지
        Image(
            painter = painterResource(id = frame.drawableId),
            contentDescription = frame.name,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        
        // 선택 상태 표시
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        KTXBlue.copy(alpha = 0.3f),
                        RoundedCornerShape(6.dp)
                    )
            )
        }
    }
}

/**
 * Phase 1: 액션 버튼 섹션 - 예시 이미지의 "확인" 버튼과 유사
 */
@Composable
private fun ActionButtonSection(
    selectedFrame: Frame?,
    photos: List<Bitmap?>,
    isProcessing: Boolean,
    onCompose: () -> Unit
) {
    val photoCount = photos.count { it != null }
    val isEnabled = selectedFrame != null && photoCount > 0 && !isProcessing
    
    Button(
        onClick = onCompose,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) KTXBlue else Color.Gray
        ),
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = if (isProcessing) "합성 중..." else "확인",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
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
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else KTXBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) MaterialTheme.colorScheme.onErrorContainer else KTXBlue,
            modifier = Modifier.padding(16.dp)
        )
    }
}

