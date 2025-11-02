package com.example.a4cut.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 이미지 크롭 화면
 * frames.json에 정의된 프레임 슬롯의 실제 픽셀 크기에 맞춰 사진을 자르는 화면
 * 예: long_form_black 첫 번째 칸의 비율은 "497:336" (슬롯의 실제 픽셀 크기)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    navController: NavController,
    imageUriString: String,
    aspectRatioString: String, // "3:4", "1:1" 등
    slotIndex: Int = -1 // 몇 번째 칸인지 (옵션)
) {
    val context = LocalContext.current
    val imageUri = imageUriString.toUri()
    
    // 비율 문자열을 파싱하여 너비와 높이 추출 (예: "497:336")
    val (slotWidth, slotHeight) = remember(aspectRatioString) {
        val parts = aspectRatioString.split(":")
        if (parts.size == 2) {
            val width = parts[0].toFloatOrNull() ?: 497f
            val height = parts[1].toFloatOrNull() ?: 336f
            Pair(width, height)
        } else {
            Pair(497f, 336f) // 기본값: long_form_black 첫 번째 슬롯
        }
    }
    
    // 비율 계산 (너비:높이)
    val aspectRatio = slotWidth / slotHeight
    
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var imageSize by remember { mutableStateOf<Size?>(null) }
    
    // 이미지 로드
    LaunchedEffect(imageUri) {
        if (imageUri.toString().isEmpty()) {
            navController.popBackStack()
            return@LaunchedEffect
        }
        
        imageBitmap = withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(imageUri)
                    .size(2048, 2048) // 최대 크기 제한
                    .build()
                val imageLoader = ImageLoader(context)
                val result = imageLoader.execute(request)
                when (result) {
                    is coil.request.SuccessResult -> {
                        result.drawable?.toBitmap()
                    }
                    else -> {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        
        imageBitmap?.let { bitmap ->
            val size = Size(bitmap.width.toFloat(), bitmap.height.toFloat())
            imageSize = size
            
            // 초기 크롭 영역 계산 (슬롯 비율 유지)
            val cropWidth = minOf(size.width, size.height * aspectRatio)
            val cropHeight = cropWidth / aspectRatio
            val cropX = (size.width - cropWidth) / 2
            val cropY = (size.height - cropHeight) / 2
            
            cropRect = Rect(
                offset = Offset(cropX, cropY),
                size = Size(cropWidth, cropHeight)
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사진 자르기") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            imageBitmap?.let { bitmap ->
                                cropRect?.let { rect ->
                                    // 크롭된 비트맵 생성
                                    val croppedBitmap = Bitmap.createBitmap(
                                        bitmap,
                                        rect.left.toInt(),
                                        rect.top.toInt(),
                                        rect.width.toInt(),
                                        rect.height.toInt()
                                    )
                                    
                                    // 크롭된 이미지를 임시 파일로 저장하고 Uri 반환
                                    val resultUri = saveBitmapToCache(context, croppedBitmap)
                                    
                                    // 이전 화면으로 잘린 이미지 Uri 전달
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("croppedImageUri", resultUri.toString())
                                    
                                    navController.popBackStack()
                                }
                            }
                        },
                        enabled = imageBitmap != null && cropRect != null
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "완료",
                            tint = if (imageBitmap != null && cropRect != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Gray
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            imageBitmap?.let { bitmap ->
                imageSize?.let { size ->
                    cropRect?.let { rect ->
                        ImageCropperContent(
                            imageBitmap = bitmap,
                            imageSize = size,
                            cropRect = rect,
                            aspectRatio = aspectRatio,
                            onCropRectChanged = { newRect ->
                                cropRect = newRect
                            }
                        )
                    }
                }
            } ?: run {
                // 이미지 로딩 중
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

/**
 * 이미지 크롭 컴포넌트
 */
@Composable
private fun ImageCropperContent(
    imageBitmap: Bitmap,
    imageSize: Size,
    cropRect: Rect,
    aspectRatio: Float,
    onCropRectChanged: (Rect) -> Unit
) {
    var imageOffsetX by remember { mutableStateOf(0f) }
    var imageOffsetY by remember { mutableStateOf(0f) }
    var imageScale by remember { mutableStateOf(1f) }
    
    // 이미지 스케일은 Canvas 내부에서 계산
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    // 드래그로 크롭 영역 이동
                    val scaleX = size.width / imageSize.width
                    val scaleY = size.height / imageSize.height
                    val actualScale = minOf(scaleX, scaleY, 1f)
                    
                    // 드래그 좌표를 이미지 좌표계로 변환
                    val dragX = dragAmount.x / actualScale
                    val dragY = dragAmount.y / actualScale
                    
                    val newRect = Rect(
                        offset = Offset(
                            (cropRect.left + dragX).coerceIn(
                                0f,
                                imageSize.width - cropRect.width
                            ),
                            (cropRect.top + dragY).coerceIn(
                                0f,
                                imageSize.height - cropRect.height
                            )
                        ),
                        size = cropRect.size
                    )
                    onCropRectChanged(newRect)
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val scaleX = size.width / imageSize.width
            val scaleY = size.height / imageSize.height
            val actualScale = minOf(scaleX, scaleY, 1f)
            
            val scaledImageWidth = imageSize.width * actualScale
            val scaledImageHeight = imageSize.height * actualScale
            val imageOffsetX = (size.width - scaledImageWidth) / 2
            val imageOffsetY = (size.height - scaledImageHeight) / 2
            
            // 크롭 영역을 화면 좌표계로 변환
            val cropRectScreen = Rect(
                offset = Offset(
                    imageOffsetX + cropRect.left * actualScale,
                    imageOffsetY + cropRect.top * actualScale
                ),
                size = Size(cropRect.width * actualScale, cropRect.height * actualScale)
            )
            // 이미지 그리기
            drawImage(
                image = imageBitmap.asImageBitmap(),
                dstOffset = Offset(imageOffsetX, imageOffsetY),
                dstSize = Size(scaledImageWidth, scaledImageHeight)
            )
            
            // 크롭 영역 외부를 어둡게 처리
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset.Zero,
                size = Size(cropRectScreen.left, size.height)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(cropRectScreen.right, 0f),
                size = Size(size.width - cropRectScreen.right, size.height)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(cropRectScreen.left, 0f),
                size = Size(cropRectScreen.width, cropRectScreen.top)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(cropRectScreen.left, cropRectScreen.bottom),
                size = Size(cropRectScreen.width, size.height - cropRectScreen.bottom)
            )
            
            // 크롭 영역 테두리
            drawRect(
                color = Color.White,
                topLeft = cropRectScreen.topLeft,
                size = cropRectScreen.size,
                style = Stroke(width = 3.dp.toPx())
            )
            
            // 크롭 영역 모서리 표시
            val cornerSize = 20.dp.toPx()
            // 좌측 상단
            drawLine(
                color = Color.White,
                start = cropRectScreen.topLeft,
                end = Offset(cropRectScreen.left + cornerSize, cropRectScreen.top),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.White,
                start = cropRectScreen.topLeft,
                end = Offset(cropRectScreen.left, cropRectScreen.top + cornerSize),
                strokeWidth = 4f
            )
            // 우측 상단
            drawLine(
                color = Color.White,
                start = Offset(cropRectScreen.right, cropRectScreen.top),
                end = Offset(cropRectScreen.right - cornerSize, cropRectScreen.top),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.White,
                start = Offset(cropRectScreen.right, cropRectScreen.top),
                end = Offset(cropRectScreen.right, cropRectScreen.top + cornerSize),
                strokeWidth = 4f
            )
            // 좌측 하단
            drawLine(
                color = Color.White,
                start = Offset(cropRectScreen.left, cropRectScreen.bottom),
                end = Offset(cropRectScreen.left + cornerSize, cropRectScreen.bottom),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.White,
                start = Offset(cropRectScreen.left, cropRectScreen.bottom),
                end = Offset(cropRectScreen.left, cropRectScreen.bottom - cornerSize),
                strokeWidth = 4f
            )
            // 우측 하단
            drawLine(
                color = Color.White,
                start = Offset(cropRectScreen.right, cropRectScreen.bottom),
                end = Offset(cropRectScreen.right - cornerSize, cropRectScreen.bottom),
                strokeWidth = 4f
            )
            drawLine(
                color = Color.White,
                start = Offset(cropRectScreen.right, cropRectScreen.bottom),
                end = Offset(cropRectScreen.right, cropRectScreen.bottom - cornerSize),
                strokeWidth = 4f
            )
        }
    }
}

/**
 * 비트맵을 임시 파일로 저장하고 Uri를 반환하는 헬퍼 함수
 */
private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val cacheDir = context.cacheDir
    val file = File(cacheDir, "cropped_image_${UUID.randomUUID()}.jpg")
    try {
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Uri.fromFile(file)
}

/**
 * Drawable을 Bitmap으로 변환하는 확장 함수
 */
private fun android.graphics.drawable.Drawable.toBitmap(): Bitmap? {
    return try {
        if (this is android.graphics.drawable.BitmapDrawable) {
            return this.bitmap
        }
        
        val width = if (intrinsicWidth > 0) intrinsicWidth else 1080
        val height = if (intrinsicHeight > 0) intrinsicHeight else 1920
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        setBounds(0, 0, width, height)
        draw(canvas)
        
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

