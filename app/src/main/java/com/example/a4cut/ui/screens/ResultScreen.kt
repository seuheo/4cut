package com.example.a4cut.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.a4cut.data.model.Frame
import com.example.a4cut.ui.components.ImagePreviewDialog
import com.example.a4cut.ui.components.KtxStationSelector
import com.example.a4cut.data.repository.KTXStationRepository
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 인스타그램 스타일 결과 화면
 * 완성된 4컷 사진을 인스타그램 포스트처럼 보여주고 공유할 수 있습니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    modifier: Modifier = Modifier,
    frameViewModel: FrameViewModel,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onRestartWithPhotos: () -> Unit = onRestart, // 기존 사진 유지하고 프레임만 변경
    photoRepository: PhotoRepository? = null // DB 저장을 위한 Repository 추가
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // FrameViewModel에서 상태 수집
    val selectedFrame by frameViewModel.selectedFrame.collectAsState()
    val photos by frameViewModel.photos.collectAsState()
    val composedImage by frameViewModel.composedImage.collectAsState()
    val isProcessing by frameViewModel.isProcessing.collectAsState()
    val errorMessage by frameViewModel.errorMessage.collectAsState()
    // val isSaved by frameViewModel.isSaved.collectAsState()
    // val isShared by frameViewModel.isShared.collectAsState()
    
    // KTX 역 선택을 위한 상태
    val ktxStationRepository = remember { KTXStationRepository() }
    val ktxLines by remember { MutableStateFlow(ktxStationRepository.getLines()) }.collectAsState()
    val _stationsByLine = remember { MutableStateFlow(ktxStationRepository.getStationsByLine("Gyeongbu")) }
    val stationsByLine by _stationsByLine.collectAsState()
    var selectedLine by remember { mutableStateOf("Gyeongbu") }
    var selectedStation by remember { mutableStateOf<String?>(null) }
    
    // 노선 변경 시 역 목록 업데이트
    LaunchedEffect(selectedLine) {
        val stations = ktxStationRepository.getStationsByLine(selectedLine)
        _stationsByLine.value = stations
        selectedStation = null // 노선 변경 시 역 선택 초기화
    }
    
    // 로컬 상태
    var showPreviewDialog by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var showSaveSnackbar by remember { mutableStateOf(false) }
    var showShareSnackbar by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    
    // 디버그 로그
    LaunchedEffect(selectedFrame, photos) {
        val photoCount = photos.count { it != null }
        println("=== ResultScreen 디버그 ===")
        println("ResultScreen: selectedFrame = $selectedFrame")
        println("ResultScreen: photos = ${photos.map { it != null }}")
        println("ResultScreen: photoCount = $photoCount")
        println("ResultScreen: composedImage = ${composedImage != null}")
        println("ResultScreen: isProcessing = $isProcessing")
        println("ResultScreen: errorMessage = $errorMessage")
        
        if (selectedFrame != null && photoCount > 0 && composedImage == null && !isProcessing) {
            println("ResultScreen: 이미지 합성 시작")
            frameViewModel.startImageComposition()
        } else {
            println("ResultScreen: 이미지 합성 조건 불만족")
            println("  - selectedFrame != null: ${selectedFrame != null}")
            println("  - photoCount > 0: ${photoCount > 0}")
            println("  - composedImage == null: ${composedImage == null}")
            println("  - !isProcessing: ${!isProcessing}")
        }
        println("=== ResultScreen 디버그 끝 ===")
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
                    text = "완성된 사진",
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
            actions = {
                IconButton(onClick = { showRestartDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "다시 만들기",
                        tint = IosColors.label
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = IosColors.systemBackground,
                titleContentColor = IosColors.label
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 인스타그램 스타일 포스트 카드
            when {
                composedImage != null -> {
                    InstagramPostCard(
                        image = composedImage!!,
                        frame = selectedFrame,
                        isLiked = isLiked,
                        onLikeToggle = { isLiked = !isLiked },
                        onImageClick = { showPreviewDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                isProcessing -> {
                    // 로딩 상태
                    ProcessingState(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    // 에러 상태
                    ErrorState(
                        message = errorMessage ?: "이미지 생성 중 오류가 발생했습니다.",
                        onRetry = { frameViewModel.startImageComposition() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // KTX 역 선택 섹션 (이미지가 완성된 후에만 표시)
            if (composedImage != null) {
                KtxStationSelectionSection(
                    selectedLine = selectedLine,
                    onLineSelected = { selectedLine = it },
                    stations = stationsByLine,
                    selectedStation = selectedStation,
                    onStationSelected = { selectedStation = it }
                )
            }

            // 액션 버튼들
            if (composedImage != null) {
                ActionButtons(
                    isSaved = false, // 임시로 false
                    isShared = false, // 임시로 false
                    onSave = { 
                        // KTX 역 정보와 함께 DB에 자동 저장
                        saveToDatabaseWithStation(selectedStation, photoRepository, context)
                        frameViewModel.saveImage()
                        showSaveSnackbar = true
                    },
                    onShare = { 
                        /* TODO: 공유 기능 */
                        showShareSnackbar = true
                    },
                    onRestart = { showRestartDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // 이미지 미리보기 다이얼로그
    if (showPreviewDialog && composedImage != null) {
        ImagePreviewDialog(
            bitmap = composedImage,
            onSave = { /* TODO: 저장 */ },
            onShare = { /* TODO: 공유 */ },
            onDismiss = { showPreviewDialog = false }
        )
    }
    
    // 다시 만들기 선택 다이얼로그
    if (showRestartDialog) {
        RestartDialog(
            onKeepPhotos = {
                // 기존 사진 유지하고 프레임 선택 화면으로
                showRestartDialog = false
                onRestartWithPhotos()
            },
            onNewPhotos = {
                // 완전히 새로 시작 (사진 선택 화면으로)
                showRestartDialog = false
                onRestart()
            },
            onDismiss = { showRestartDialog = false }
        )
    }
    
    // 저장 완료 Snackbar
    if (showSaveSnackbar) {
        LaunchedEffect(showSaveSnackbar) {
            kotlinx.coroutines.delay(2000) // 2초 후 자동 사라짐
            showSaveSnackbar = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = IosColors.SystemGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "갤러리에 저장되었어요! 📸",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // 공유 완료 Snackbar
    if (showShareSnackbar) {
        LaunchedEffect(showShareSnackbar) {
            kotlinx.coroutines.delay(2000) // 2초 후 자동 사라짐
            showShareSnackbar = false
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = IosColors.SystemBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "공유가 완료되었어요! 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 인스타그램 스타일 포스트 카드
 */
@Composable
private fun InstagramPostCard(
    image: Bitmap,
    frame: Frame?,
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = IosColors.systemBackground
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column {
            // 포스트 헤더
            PostHeader(
                frame = frame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // 포스트 이미지
            PostImage(
                image = image,
                onClick = onImageClick,
                modifier = Modifier.fillMaxWidth()
            )

            // 포스트 액션들
            PostActions(
                isLiked = isLiked,
                onLikeToggle = onLikeToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 포스트 정보
            PostInfo(
                frame = frame,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * 포스트 헤더
 */
@Composable
private fun PostHeader(
    frame: Frame?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지 (KTX 로고)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = KTXBlue,
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "KTX",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "KTX 네컷",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = IosColors.label
            )
            Text(
                text = frame?.name ?: "프레임",
                style = MaterialTheme.typography.bodySmall,
                    color = IosColors.secondaryLabel
            )
        }
    }
}

/**
 * 포스트 이미지
 */
@Composable
private fun PostImage(
    image: Bitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "image_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "완성된 4컷 사진",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * 포스트 액션들
 */
@Composable
private fun PostActions(
    isLiked: Boolean,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 좋아요 버튼 - 강화된 애니메이션
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        // 스케일 애니메이션 (스프링 효과)
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.7f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "like_scale"
        )
        
        // 색상 (간단한 조건부 색상)
        val heartColor = if (isLiked) IosColors.SystemRed else IosColors.secondaryLabel

        IconButton(
            onClick = onLikeToggle,
            modifier = Modifier.scale(scale),
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isLiked) "좋아요 취소" else "좋아요",
                modifier = Modifier.size(24.dp),
                tint = heartColor
            )
        }

        // 공유 버튼
        IconButton(onClick = { /* TODO: 공유 기능 */ }) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "공유",
                modifier = Modifier.size(24.dp),
                tint = IosColors.secondaryLabel
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 저장 버튼
        IconButton(onClick = { /* TODO: 저장 기능 */ }) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "저장",
                modifier = Modifier.size(24.dp),
                tint = IosColors.secondaryLabel
            )
        }
    }
}

/**
 * 포스트 정보
 */
@Composable
private fun PostInfo(
    frame: Frame?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "KTX와 함께한 특별한 순간 ✨",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = IosColors.label
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "#KTX #네컷 #여행 #추억",
            style = MaterialTheme.typography.bodySmall,
            color = IosColors.SystemBlue
        )

        if (frame != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "프레임: ${frame.name}",
                style = MaterialTheme.typography.bodySmall,
                    color = IosColors.secondaryLabel
            )
        }
    }
}

/**
 * 처리 중 상태 - 개선된 로딩 애니메이션
 */
@Composable
private fun ProcessingState(
    modifier: Modifier = Modifier
) {
    var animationPhase by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            animationPhase = (animationPhase + 1) % 3
            kotlinx.coroutines.delay(800)
        }
    }
    
    val loadingTexts = listOf(
        "사진을 만들고 있어요",
        "프레임을 적용하고 있어요",
        "거의 완성되었어요"
    )
    
    val loadingSubTexts = listOf(
        "잠시만 기다려주세요...",
        "KTX 프레임을 준비하고 있어요",
        "마지막 단계입니다"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = IosColors.systemBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 개선된 로딩 인디케이터
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = IosColors.SystemBlue,
                    strokeWidth = 4.dp
                )
                
                // 중앙에 KTX 로고
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = KTXBlue,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KTX",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = loadingTexts[animationPhase],
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = IosColors.label
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = loadingSubTexts[animationPhase],
                style = MaterialTheme.typography.bodyLarge,
                    color = IosColors.secondaryLabel,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 에러 상태
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = IosColors.SystemRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = IosColors.SystemRed.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "오류가 발생했습니다",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = IosColors.SystemRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                    color = IosColors.secondaryLabel,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosColors.SystemRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "다시 시도",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 액션 버튼들
 */
@Composable
private fun ActionButtons(
    isSaved: Boolean,
    isShared: Boolean,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 저장 및 공유 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = !isSaved,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSaved) IosColors.SystemGreen else IosColors.SystemBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSaved) "저장됨" else "저장하기",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onShare,
                enabled = !isShared,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isShared) IosColors.SystemGreen else IosColors.SystemBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isShared) "공유됨" else "공유하기",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 다시 만들기 버튼
        OutlinedButton(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = IosColors.label
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = IosColors.systemGray4
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "다시 만들기",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * 다시 만들기 선택 다이얼로그
 */
@Composable
private fun RestartDialog(
    onKeepPhotos: () -> Unit,
    onNewPhotos: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "다시 만들기",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "어떻게 다시 만들고 싶으신가요?",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            Button(
                onClick = onKeepPhotos,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IosColors.SystemBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "사진 유지",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onNewPhotos,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = IosColors.label
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = IosColors.systemGray4
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "새로운 사진",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * KTX 역 선택 섹션
 */
@Composable
private fun KtxStationSelectionSection(
    selectedLine: String,
    onLineSelected: (String) -> Unit,
    stations: List<com.example.a4cut.data.model.KtxStation>,
    selectedStation: String?,
    onStationSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("ResultScreen", "KtxStationSelectionSection 렌더링 - stations: ${stations.size}")
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "KTX 역 선택 (선택 사항)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 노선 선택 탭
            val lines = listOf("Gyeongbu", "Honam", "Gyeongjeon", "Jungang", "Jeolla", "Donghae")
            val lineNames = mapOf(
                "Gyeongbu" to "경부선",
                "Honam" to "호남선", 
                "Gyeongjeon" to "경전선",
                "Jungang" to "중앙선",
                "Jeolla" to "전라선",
                "Donghae" to "동해선"
            )
            
            TabRow(selectedTabIndex = lines.indexOf(selectedLine).coerceAtLeast(0)) {
                lines.forEach { line ->
                    Tab(
                        selected = selectedLine == line,
                        onClick = { onLineSelected(line) },
                        text = { Text(lineNames[line] ?: line) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 역 선택
            KtxStationSelector(
                stations = stations,
                selectedStation = selectedStation,
                onStationSelected = onStationSelected
            )
        }
    }
}

/**
 * 선택된 KTX 역 정보와 함께 DB에 저장
 */
private fun saveToDatabaseWithStation(
    selectedStation: String?,
    photoRepository: PhotoRepository?,
    context: android.content.Context
) {
    if (photoRepository == null) {
        Log.e("ResultScreen", "PhotoRepository가 null입니다!")
        return
    }
    
    if (selectedStation == null) {
        Log.d("ResultScreen", "KTX 역이 선택되지 않았습니다. 기본 정보로 저장합니다.")
        return
    }
    
    Log.d("ResultScreen", "선택된 KTX 역: $selectedStation")
    
    // KTX 역 정보 조회
    val ktxStationRepository = KTXStationRepository()
    val station = ktxStationRepository.findStationByName(selectedStation)
    
    if (station != null) {
        Log.d("ResultScreen", "KTX 역 정보: ${station.name} (${station.latitude}, ${station.longitude})")
        
        // PhotoEntity 생성 및 저장
        val photoEntity = PhotoEntity(
            id = 0, // 새로운 ID 생성
            imagePath = "temp_path_${System.currentTimeMillis()}", // 임시 경로
            title = "KTX 네컷 사진",
            location = station.name,
            latitude = station.latitude,
            longitude = station.longitude,
            station = station.name,
            createdAt = System.currentTimeMillis()
        )
        
        // 코루틴에서 DB 저장 실행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val photoId = photoRepository.insertPhoto(photoEntity)
                Log.d("ResultScreen", "DB 저장 성공! Photo ID: $photoId")
            } catch (e: Exception) {
                Log.e("ResultScreen", "DB 저장 실패", e)
            }
        }
    } else {
        Log.e("ResultScreen", "KTX 역을 찾을 수 없음: $selectedStation")
    }
}