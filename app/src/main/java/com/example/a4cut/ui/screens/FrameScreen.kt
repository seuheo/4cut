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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.model.FrameFormat
import com.example.a4cut.ui.components.PhotoGrid
import com.example.a4cut.ui.components.ImagePreviewDialog
import com.example.a4cut.ui.components.TossPrimaryButton
import com.example.a4cut.ui.components.TossSecondaryButton
import com.example.a4cut.ui.components.TossTextButton
import com.example.a4cut.ui.components.KtxStationSelector
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel
import com.example.a4cut.ui.viewmodel.PhotoState
import com.example.a4cut.data.model.KtxStation
import com.example.a4cut.ui.navigation.Screen

/**
 * iOS 스타일 프레임 선택 화면 - 예시 이미지와 동일한 깔끔한 디자인
 * 상단 미리보기 + 하단 프레임 캐러셀의 직관적인 단일 화면
 */
@Composable
fun FrameScreen(
    navController: NavController,
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
    val selectedKtxStation by frameViewModel.selectedKtxStation.collectAsState()
    
    // 포맷 관련 상태
    val selectedFormat by frameViewModel.selectedFormat.collectAsState()
    val framesByFormat by frameViewModel.framesByFormat.collectAsState()
    val formats by frameViewModel.formats.collectAsState()

    // DisposableEffect 제거: FrameScreen에서는 사진 데이터를 초기화하지 않음
    // 사진 선택 데이터는 PhotoSelectionScreen에서 관리되며,
    // FrameScreen에서 뒤로 가면 PhotoSelectionScreen으로 돌아가므로
    // PhotoSelectionScreen이 여전히 생존한 상태임
    // 따라서 FrameScreen의 onDispose에서는 사진 데이터를 초기화하지 않아야 함

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

    // iOS 스타일 레이아웃 구성
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)), // iOS 스타일 순백색 배경
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // iOS 스타일 상단 여백
        Spacer(modifier = Modifier.height(20.dp))
        
        // 1. iOS 스타일 상단 미리보기 영역
        FramePreviewSection(
            photoStates = photoStates,
            selectedFrame = selectedFrame,
            onPhotoClick = { index -> 
                frameViewModel.togglePhotoSelection(index)
            },
            onTransform = { index, scale, offsetX, offsetY ->
                frameViewModel.updatePhotoState(index, scale, offsetX, offsetY)
            },
            frameViewModel = frameViewModel
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // 2. 선택된 프레임 정보 표시
        SelectedFrameInfoSection(
            selectedFrame = selectedFrame
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 3. 프레임 선택 버튼
        Button(
            onClick = {
                navController.navigate(Screen.FramePicker.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "프레임 선택 / 변경",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 3. KTX역 선택 섹션 (임시 비활성화)
        // KtxStationSelector(
        //     selectedStation = selectedKtxStation,
        //     onStationSelected = { station ->
        //         frameViewModel.selectKtxStation(station)
        //     },
        //     modifier = Modifier.padding(horizontal = 16.dp)
        // )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 4. iOS 스타일 액션 버튼
        IOSActionButtonSection(
            selectedFrame = selectedFrame,
            photoStates = photoStates,
            isProcessing = isProcessing,
            onCompose = {
                println("FrameScreen: 이미지 합성 버튼 클릭됨")
                frameViewModel.startImageComposition()
            }
        )
        
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    // iOS 스타일 메시지 표시 (성공/에러)
    if (successMessage != null) {
        IOSMessageCard(
            message = successMessage!!,
            isError = false,
            modifier = Modifier.padding(16.dp)
        )
    }

    if (errorMessage != null) {
        IOSMessageCard(
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
                // Bug #6 수정: 범용 공유로 변경
                frameViewModel.shareImage()
                showPreviewDialog = false
            },
            isProcessing = isProcessing
        )
    }
}

/**
 * iOS 스타일 상단 미리보기 섹션 - 예시 이미지와 동일한 깔끔한 디자인
 * 선택된 사진 4장을 2x2 그리드로 표시하고 프레임을 오버레이
 */
@Composable
private fun IOSPhotoPreviewSection(
    photoStates: List<PhotoState>,
    selectedFrame: Frame?,
    onPhotoClick: (Int) -> Unit,
    onTransform: (Int, Float, Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(0.75f) // iOS 스타일 비율
            .background(
                Color(0xFFF8F9FA), // iOS 스타일 연한 회색 배경
                RoundedCornerShape(24.dp) // iOS 스타일 둥근 모서리
            )
            .padding(16.dp), // iOS 스타일 내부 여백
        contentAlignment = Alignment.Center
    ) {
        // 사진 4장을 2x2 그리드로 배치 (제스처 편집 가능)
        if (photoStates.any { it.bitmap != null }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp) // iOS 스타일 간격
            ) {
                // 상단 행
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IOSEditablePhotoItem(
                        photoState = photoStates.getOrNull(0) ?: PhotoState(null),
                        index = 0,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                    IOSEditablePhotoItem(
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IOSEditablePhotoItem(
                        photoState = photoStates.getOrNull(2) ?: PhotoState(null),
                        index = 2,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                    IOSEditablePhotoItem(
                        photoState = photoStates.getOrNull(3) ?: PhotoState(null),
                        index = 3,
                        onPhotoClick = onPhotoClick,
                        onTransform = onTransform,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // iOS 스타일 안내 텍스트
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Photos",
                    tint = Color(0xFF8E8E93), // iOS 스타일 회색
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "사진을 추가하고\n프레임을 선택하세요",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF8E8E93), // iOS 스타일 회색
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 선택된 프레임 오버레이 (Box 내부로 이동)
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
 * iOS 스타일 제스처 편집 가능한 개별 사진 아이템
 */
@Composable
private fun IOSEditablePhotoItem(
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
                Color(0xFFE5E5EA), // iOS 스타일 연한 회색
                RoundedCornerShape(16.dp) // iOS 스타일 둥근 모서리
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
                    .clip(RoundedCornerShape(16.dp))
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
                    tint = Color(0xFF8E8E93), // iOS 스타일 회색
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "사진 추가",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF8E8E93) // iOS 스타일 회색
                )
            }
        }
    }
}

/**
 * iOS 스타일 하단 프레임 캐러셀 섹션 - 예시 이미지와 동일한 깔끔한 디자인
 * 프레임들을 가로 스크롤로 선택할 수 있는 캐러셀
 */
@Composable
private fun IOSFrameCarouselSection(
    frames: List<Frame>,
    selectedFrame: Frame?,
    isLoading: Boolean,
    onFrameSelect: (Frame) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // iOS 스타일 프레임 선택 안내 텍스트
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF000000), // iOS 스타일 검은색
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = Color(0xFF007AFF), // iOS 스타일 파란색
                modifier = Modifier.size(28.dp)
            )
        } else {
            // iOS 스타일 프레임 캐러셀 (가로 스크롤)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(frames) { frame ->
                    IOSFrameCarouselItem(
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
 * iOS 스타일 프레임 캐러셀 개별 아이템
 */
@Composable
private fun IOSFrameCarouselItem(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 88.dp else 76.dp) // iOS 스타일 크기 변화
            .background(
                color = if (isSelected) Color(0xFF007AFF).copy(alpha = 0.1f) else Color.White,
                shape = RoundedCornerShape(20.dp) // iOS 스타일 둥근 모서리
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // 프레임 미리보기 이미지
        Image(
            painter = painterResource(id = frame.drawableId),
            contentDescription = frame.name,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        // iOS 스타일 선택 상태 표시
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFF007AFF).copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    )
            )
            
            // iOS 스타일 선택 표시 아이콘
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        Color(0xFF007AFF), // iOS 스타일 파란색
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * iOS 스타일 액션 버튼 섹션 - 예시 이미지의 "확인" 버튼과 유사
 */
@Composable
private fun IOSActionButtonSection(
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
            containerColor = if (isEnabled) Color(0xFF007AFF) else Color(0xFFC7C7CC)
        ),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp) // iOS 스타일 둥근 모서리
    ) {
        if (isProcessing) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "합성 중...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        } else {
            Text(
                text = "완성하기",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

/**
 * iOS 스타일 메시지 표시 카드
 */
@Composable
private fun IOSMessageCard(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
        ),
        shape = RoundedCornerShape(20.dp), // iOS 스타일 둥근 모서리
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Close else Icons.Default.Check,
                contentDescription = if (isError) "Error" else "Success",
                tint = if (isError) Color(0xFFFF3B30) else Color(0xFF34C759), // iOS 스타일 색상
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isError) Color(0xFFFF3B30) else Color(0xFF34C759) // iOS 스타일 색상
            )
        }
    }
}

/**
 * 포맷 선택 섹션 - iOS 스타일 세그먼트 컨트롤
 */
@Composable
fun FormatSelectionSection(
    modifier: Modifier = Modifier,
    selectedFormat: FrameFormat,
    formats: List<FrameFormat>,
    onFormatSelect: (FrameFormat) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "프레임 포맷 선택",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF1C1C1E), // iOS 스타일 다크 그레이
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // iOS 스타일 세그먼트 컨트롤
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .background(
                    color = Color(0xFFF2F2F7), // iOS 스타일 라이트 그레이
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            formats.forEach { format ->
                val isSelected = format == selectedFormat
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onFormatSelect(format) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (format) {
                            FrameFormat.STANDARD -> "Standard"
                            FrameFormat.LONG_FORM -> "Long Form"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color(0xFF007AFF) else Color(0xFF8E8E93), // iOS 스타일 색상
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 프레임별 슬롯 위치에 맞는 사진 미리보기 섹션
 */
@Composable
fun FramePreviewSection(
    photoStates: List<PhotoState>,
    selectedFrame: Frame?,
    onPhotoClick: (Int) -> Unit,
    onTransform: (Int, Float, Float, Float) -> Unit,
    frameViewModel: FrameViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(0.75f) // iOS 스타일 비율
            .background(
                Color(0xFFF8F9FA), // iOS 스타일 연한 회색 배경
                RoundedCornerShape(24.dp) // iOS 스타일 둥근 모서리
            )
            .padding(16.dp), // iOS 스타일 내부 여백
        contentAlignment = Alignment.Center
    ) {
        if (selectedFrame != null && photoStates.any { it.bitmap != null }) {
            // 프레임별 슬롯 위치에 맞는 미리보기
            FramePreviewWithSlots(
                frame = selectedFrame,
                photoStates = photoStates,
                onPhotoClick = onPhotoClick,
                onTransform = onTransform,
                frameViewModel = frameViewModel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // 프레임이 선택되지 않았거나 사진이 없을 때 기본 표시
            Text(
                text = "프레임을 선택하고 사진을 추가하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF8E8E93), // iOS 스타일 회색
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 프레임별 슬롯 위치에 맞는 사진 배치 미리보기
 */
@Composable
fun FramePreviewWithSlots(
    frame: Frame,
    photoStates: List<PhotoState>,
    onPhotoClick: (Int) -> Unit,
    onTransform: (Int, Float, Float, Float) -> Unit,
    frameViewModel: FrameViewModel,
    modifier: Modifier = Modifier
) {
    val slotRects = frameViewModel.getSlotRectsForFrame(frame)
    
    Box(modifier = modifier.clipToBounds()) {
        // 1. 프레임 이미지 (배경)
        Image(
            painter = painterResource(id = frame.drawableId),
            contentDescription = frame.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit // 프레임 전체가 보이도록
        )

        // 2. 사진들을 슬롯 위치에 배치
        if (slotRects != null) {
            Layout(
                modifier = Modifier.fillMaxSize(),
                content = {
                    photoStates.forEachIndexed { index, photoState ->
                        photoState.bitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Photo ${index + 1}",
                                contentScale = ContentScale.Crop, // 칸에 맞게 크롭
                                modifier = Modifier
                                    .clipToBounds()
                                    .pointerInput(index) {
                                        detectTapGestures {
                                            onPhotoClick(index)
                                        }
                                    }
                            )
                        }
                    }
                }
            ) { measurables, constraints ->
                val placeables = mutableListOf<Placeable>()
                val parentWidth = constraints.maxWidth
                val parentHeight = constraints.maxHeight

                measurables.forEachIndexed { index, measurable ->
                    if (index < slotRects.size) {
                        val rectF = slotRects[index]
                        // RectF 비율 좌표를 절대 픽셀 좌표/크기로 변환
                        val slotLeft = (rectF.left * parentWidth).toInt()
                        val slotTop = (rectF.top * parentHeight).toInt()
                        val slotRight = (rectF.right * parentWidth).toInt()
                        val slotBottom = (rectF.bottom * parentHeight).toInt()
                        val slotWidth = slotRight - slotLeft
                        val slotHeight = slotBottom - slotTop

                        // 해당 크기로 자식 측정
                        val placeable = measurable.measure(
                            androidx.compose.ui.unit.Constraints.fixed(slotWidth, slotHeight)
                        )
                        placeables.add(placeable)
                    }
                }

                // 부모 레이아웃 크기 설정 및 자식 배치
                layout(parentWidth, parentHeight) {
                    placeables.forEachIndexed { index, placeable ->
                        if (index < slotRects.size) {
                            val rectF = slotRects[index]
                            val slotLeft = (rectF.left * parentWidth).toInt()
                            val slotTop = (rectF.top * parentHeight).toInt()
                            // 계산된 위치에 자식 배치
                            placeable.placeRelative(x = slotLeft, y = slotTop)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 선택된 프레임 정보 표시 섹션
 */
@Composable
private fun SelectedFrameInfoSection(
    selectedFrame: Frame?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "선택된 프레임",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = selectedFrame?.name ?: "프레임을 선택해주세요",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedFrame != null) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            
            if (selectedFrame != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedFrame.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

