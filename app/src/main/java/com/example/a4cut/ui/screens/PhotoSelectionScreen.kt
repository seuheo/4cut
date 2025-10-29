package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.components.TossPrimaryButton
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 사진 4장 선택 화면 - iOS 미니멀 스타일 적용
 * 기존 워크플로우 유지하면서 세련된 디자인 적용
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSelectionScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel,
    onNext: () -> Unit,
    openGallery: (() -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // FrameViewModel에서 상태 수집
    val photos by frameViewModel.photos.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    val successMessage by frameViewModel.successMessage.collectAsState()
    val uiUpdateTrigger by frameViewModel.uiUpdateTrigger.collectAsState()
    
    // 디버그 로그
    LaunchedEffect(photos, uiUpdateTrigger) {
        val photoCount = photos.count { it != null }
        println("=== PhotoSelectionScreen 디버그 ===")
        println("PhotoSelectionScreen: 사진 상태 업데이트 - ${photos.map { it != null }}")
        println("PhotoSelectionScreen: 선택된 사진 개수: $photoCount")
        println("PhotoSelectionScreen: 사진 리스트 크기: ${photos.size}")
        println("PhotoSelectionScreen: UI 업데이트 트리거: $uiUpdateTrigger")
        photos.forEachIndexed { index, bitmap ->
            println("PhotoSelectionScreen: 사진[$index] = ${if (bitmap != null) "있음 (${bitmap.width}x${bitmap.height})" else "없음"}")
        }
        println("=== 디버그 끝 ===")
    }
    
    // Context 설정
    LaunchedEffect(Unit) {
        frameViewModel.setContext(context)
    }
    
    // 화면이 사라질 때 상태 초기화 제거
    // DisposableEffect에서 clearAllSelections() 호출 시,
    // FramePickerScreen으로 이동할 때도 사진 데이터가 사라지는 문제 발생
    // 해결: PhotoSelectionScreen의 onDispose에서는 상태를 초기화하지 않음
    // 대신 뒤로 가기 버튼에서만 초기화하도록 변경 필요
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "사진 선택", 
                        fontWeight = FontWeight.SemiBold,
                        color = IosColors.label
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = IosColors.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(IosColors.systemBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 헤더 섹션
            item {
                HeaderSection(
                    successMessage = successMessage,
                    errorMessage = errorMessage
                )
            }
            
            // 4컷 사진 선택 그리드
            item {
                PhotoGridSection(
                    photos = photos,
                    onPhotoClick = { index -> 
                        frameViewModel.togglePhotoSelection(index)
                    }
                )
            }
            
            // 갤러리에서 사진 선택 버튼
            if (openGallery != null) {
                item {
                    IosStyleButton(
                        text = "갤러리에서 사진 선택하기",
                        onClick = openGallery,
                        icon = Icons.Default.Home
                    )
                }
            }
            
            // 모든 사진 삭제 버튼
            item {
                Button(
                    onClick = { 
                        repeat(4) { index ->
                            frameViewModel.removePhoto(index)
                        }
                    },
                    enabled = photos.any { it != null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosColors.SystemRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "모든 사진 삭제",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
            
            // 다음 단계 버튼
            item {
                val photoCount = photos.count { it != null }
                println("PhotoSelectionScreen: photoCount = $photoCount, photos = ${photos.map { it != null }}")
                IosStyleButton(
                    text = "프레임 선택하기 (${photoCount}장 선택됨)",
                    onClick = {
                        println("=== PhotoSelectionScreen: 프레임 선택 화면으로 이동 ===")
                        println("PhotoSelectionScreen: 현재 사진 상태 = ${photos.map { it != null }}")
                        println("PhotoSelectionScreen: 선택된 사진 개수 = $photoCount")
                        println("PhotoSelectionScreen: FrameViewModel 상태 확인")
                        println("  - _photos: ${frameViewModel.photos.value.map { it != null }}")
                        println("  - _photoStates: ${frameViewModel.photoStates.map { it.bitmap != null }}")
                        onNext()
                    },
                    enabled = photoCount > 0,
                    icon = Icons.Default.Star
                )
            }
            
            // 안내 텍스트
            item {
                val photoCount = photos.count { it != null }
                Text(
                    text = if (photoCount == 0) "최소 1장의 사진을 선택해주세요" else "선택된 사진: ${photoCount}장",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (photoCount > 0) IosColors.SystemBlue else IosColors.secondaryLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 헤더 섹션 - iOS 스타일
 */
@Composable
private fun HeaderSection(
    successMessage: String?,
    errorMessage: String?
) {
    Column {
        Text(
            text = "사진 선택",
            style = MaterialTheme.typography.headlineLarge,
            color = IosColors.label,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "4컷 사진을 만들기 위해 사진을 선택해주세요",
            style = MaterialTheme.typography.bodyLarge,
            color = IosColors.secondaryLabel,
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
 * 사진 그리드 섹션
 */
@Composable
private fun PhotoGridSection(
    photos: List<Bitmap?>,
    onPhotoClick: (Int) -> Unit
) {
    Column {
        Text(
            text = "4컷 사진 선택",
            style = MaterialTheme.typography.titleLarge,
            color = IosColors.label,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(
                count = 4,
                key = { index -> 
                    // photo의 존재 여부와 크기를 key로 사용하여 재구성 강제
                    val photo = photos.getOrNull(index)
                    "photo_${index}_${photo != null}_${photo?.width ?: 0}_${photo?.height ?: 0}"
                }
            ) { index ->
                val photo = photos.getOrNull(index)
                PhotoGridItem(
                    index = index,
                    photo = photo,
                    onPhotoClick = onPhotoClick
                )
            }
        }
    }
}


/**
 * 사진 그리드 아이템
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
                coroutineScope.launch {
                    delay(100)
                    isPressed = false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) IosColors.SystemBlue.copy(alpha = 0.1f) else IosColors.systemBackground
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
                // 디버그 로그 추가
                LaunchedEffect(photo) {
                    println("PhotoGridItem[$index]: Bitmap 렌더링 시도 - 크기: ${photo.width}x${photo.height}, isRecycled: ${photo.isRecycled}")
                }
                
                // Bitmap 유효성 검사
                if (photo.isRecycled) {
                    println("PhotoGridItem[$index]: Bitmap이 이미 재활용됨")
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Red.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bitmap\n재활용됨",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // 사진이 있을 때 - 디버깅을 위해 배경색과 투명도 확인
                    val imageBitmap = remember(photo) { 
                        println("PhotoGridItem[$index]: asImageBitmap() 호출 - 원본 Bitmap 크기: ${photo.width}x${photo.height}")
                        photo.asImageBitmap()
                    }
                    
                    // 디버깅: 이미지가 실제로 렌더링되는지 확인하기 위한 명확한 배경
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                // 이미지가 투명하거나 안 보일 경우를 위한 파란색 배경
                                Color.Blue.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        // 실제 이미지 렌더링
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "사진 ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        
                        // 디버깅용: 이미지가 렌더링되었는지 확인하기 위한 인디케이터
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                                .background(
                                    Color.Green.copy(alpha = 0.8f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${photo.width}x${photo.height}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // 추가 디버깅: 이미지가 실제로 보이는지 확인하기 위한 테두리
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 2.dp,
                                    color = Color.Red.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                    println("PhotoGridItem[$index]: Image 컴포넌트 렌더링 완료 - 파란색 배경과 빨간색 테두리 추가됨")
                }
                
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
                        tint = if (isPressed) IosColors.SystemBlue else IosColors.secondaryLabel,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "사진 추가",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPressed) IosColors.SystemBlue else IosColors.secondaryLabel
                    )
                }
            }
        }
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
            containerColor = IosColors.SystemBlue.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = IosColors.SystemBlue,
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
            containerColor = IosColors.SystemRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = IosColors.SystemRed,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 재사용 가능한 iOS 스타일 버튼 컴포넌트
 */
@Composable
fun IosStyleButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector,
    isOutlined: Boolean = false,
    enabled: Boolean = true
) {
    val backgroundColor = when {
        !enabled -> IosColors.systemGray3
        isOutlined -> IosColors.White
        else -> IosColors.SystemBlue
    }
    val contentColor = when {
        !enabled -> IosColors.systemGray
        isOutlined -> IosColors.SystemBlue
        else -> IosColors.White
    }
    val borderColor = if (isOutlined) IosColors.SystemBlue else Color.Transparent

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = IosColors.systemGray3,
            disabledContentColor = IosColors.systemGray
        ),
        border = BorderStroke(if (isOutlined) 1.5.dp else 0.dp, borderColor)
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = text,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text, 
            fontSize = 17.sp, 
            fontWeight = FontWeight.SemiBold
        )
    }
}


