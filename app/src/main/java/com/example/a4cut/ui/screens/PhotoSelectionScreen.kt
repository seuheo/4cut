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
 * iOS 미니멀 스타일 사진 선택 화면
 * 20대 사용자들이 선호하는 세련되고 깔끔한 디자인
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(IosColors.systemBackground)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 메인 타이틀
            Text(
                text = "네컷 사진 만들기",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = IosColors.label
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 서브타이틀
            Text(
                text = "갤러리에서 최고의 사진 4장을 선택하거나\n테스트용 사진으로 바로 시작해보세요.",
                fontSize = 16.sp,
                color = IosColors.secondaryLabel,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // iOS 스타일 버튼들
            IosStyleButton(
                text = "갤러리에서 사진 선택",
                onClick = { openGallery?.invoke() },
                icon = Icons.Default.Home
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            IosStyleButton(
                text = "테스트용 사진으로 시작",
                onClick = { 
                    // 테스트용 사진 선택 로직
                    onNext()
                },
                icon = Icons.Default.Star,
                isOutlined = true
            )
        }
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

