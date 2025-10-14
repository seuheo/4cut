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
    
    // 디버그 로그
    LaunchedEffect(photos) {
        println("PhotoSelectionScreen: 사진 상태 업데이트 - ${photos.map { it != null }}")
    }
    
    // Context 설정
    LaunchedEffect(Unit) {
        frameViewModel.setContext(context)
    }
    
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
            
            // 테스트용 사진 선택 버튼들 (에뮬레이터용)
            item {
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
            }
            
            // 다음 단계 버튼
            item {
                val photoCount = photos.count { it != null }
                println("PhotoSelectionScreen: photoCount = $photoCount, photos = ${photos.map { it != null }}")
                IosStyleButton(
                    text = "프레임 선택하기 (${photoCount}장 선택됨)",
                    onClick = onNext,
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
 * 테스트용 사진 선택 버튼 섹션
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
            color = IosColors.label,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "마우스 클릭으로 쉽게 사진을 선택하고 테스트할 수 있습니다.",
            style = MaterialTheme.typography.bodySmall,
            color = IosColors.secondaryLabel,
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
                    containerColor = IosColors.SystemBlue
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
                    containerColor = IosColors.SystemBlue.copy(alpha = 0.8f)
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
                    containerColor = IosColors.SystemBlue.copy(alpha = 0.8f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "사진2",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onSelectTestPhoto3,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosColors.SystemBlue.copy(alpha = 0.8f)
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
                    containerColor = IosColors.SystemBlue.copy(alpha = 0.8f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "사진4",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Button(
                onClick = onClearAllPhotos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosColors.SystemRed
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "전체삭제",
                    style = MaterialTheme.typography.labelSmall
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

