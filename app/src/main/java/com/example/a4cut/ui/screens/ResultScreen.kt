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
import com.example.a4cut.ui.theme.*
import com.example.a4cut.ui.viewmodel.FrameViewModel

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
    onRestartWithPhotos: () -> Unit = onRestart // 기존 사진 유지하고 프레임만 변경
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
    
    // 로컬 상태
    var showPreviewDialog by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var showSaveSnackbar by remember { mutableStateOf(false) }
    var showShareSnackbar by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    
    // 디버그 로그
    LaunchedEffect(selectedFrame, photos) {
        println("ResultScreen: selectedFrame = $selectedFrame, photos = ${photos.map { it != null }}, composedImage = ${composedImage != null}")
        if (selectedFrame != null && photos.any { it != null } && composedImage == null) {
            println("ResultScreen: 이미지 합성 시작")
            frameViewModel.startImageComposition()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // 인스타그램 스타일 상단 바
        TopAppBar(
            title = {
                Text(
                    text = "완성된 사진",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = TextPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = { showRestartDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "다시 만들기",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = SurfaceLight,
                titleContentColor = TextPrimary
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

            // 액션 버튼들
            if (composedImage != null) {
                ActionButtons(
                    isSaved = false, // 임시로 false
                    isShared = false, // 임시로 false
                    onSave = { 
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
                    containerColor = SuccessGreen
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
                    containerColor = InstagramBlue
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
            containerColor = SurfaceLight
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
                color = TextPrimary
            )
            Text(
                text = frame?.name ?: "프레임",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
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
        val heartColor = if (isLiked) LikeRed else TextSecondary

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
                tint = TextSecondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 저장 버튼
        IconButton(onClick = { /* TODO: 저장 기능 */ }) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "저장",
                modifier = Modifier.size(24.dp),
                tint = TextSecondary
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
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "#KTX #네컷 #여행 #추억",
            style = MaterialTheme.typography.bodySmall,
            color = InstagramBlue
        )

        if (frame != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "프레임: ${frame.name}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
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
            containerColor = SurfaceLight
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
                    color = InstagramBlue,
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
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = loadingSubTexts[animationPhase],
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
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
            containerColor = ErrorRed.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = ErrorRed.copy(alpha = 0.3f)
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
                color = ErrorRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed,
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
                    containerColor = if (isSaved) SuccessGreen else InstagramBlue,
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
                    containerColor = if (isShared) SuccessGreen else KTXBlue,
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
                contentColor = TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = BorderLight
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
                    containerColor = InstagramBlue,
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
                    contentColor = TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = BorderLight
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