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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.a4cut.ui.utils.BackgroundRemover
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
    var removeBackground by remember { mutableStateOf(false) } // 배경 제거 옵션
    var isProcessingBackground by remember { mutableStateOf(false) } // 배경 제거 처리 중
    
    // 이미지 로드
    LaunchedEffect(imageUri) {
        println("=== CropScreen: 이미지 로드 시작 ===")
        println("입력 URI 문자열: $imageUriString")
        println("비율 문자열: $aspectRatioString")
        println("슬롯 인덱스: $slotIndex")
        
        if (imageUriString.isEmpty()) {
            println("❌ URI 문자열이 비어있음 - 화면 종료")
            navController.popBackStack()
            return@LaunchedEffect
        }
        
        val uri = imageUriString.toUri()
        println("변환된 URI: $uri")
        
        imageBitmap = withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(2048, 2048) // 최대 크기 제한
                    .build()
                val imageLoader = ImageLoader(context)
                val result = imageLoader.execute(request)
                when (result) {
                    is coil.request.SuccessResult -> {
                        val bitmap = result.drawable?.toBitmap()
                        println("이미지 로드 성공: ${bitmap?.width}x${bitmap?.height}")
                        bitmap
                    }
                    else -> {
                        println("❌ 이미지 로드 실패: ${(result as? coil.request.ErrorResult)?.throwable?.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("❌ 이미지 로드 중 예외 발생: ${e.message}")
                e.printStackTrace()
                null
            }
        }
        
        imageBitmap?.let { bitmap ->
            val size = Size(bitmap.width.toFloat(), bitmap.height.toFloat())
            imageSize = size
            println("이미지 크기 설정: ${size.width}x${size.height}")
            println("목표 비율: $aspectRatio (${slotWidth}:${slotHeight})")
            
            // 초기 크롭 영역 계산 (슬롯 비율 유지)
            val cropWidth = minOf(size.width, size.height * aspectRatio)
            val cropHeight = cropWidth / aspectRatio
            val cropX = (size.width - cropWidth) / 2
            val cropY = (size.height - cropHeight) / 2
            
            println("초기 크롭 영역 계산:")
            println("  크기: ${cropWidth}x${cropHeight}")
            println("  위치: ($cropX, $cropY)")
            
            cropRect = Rect(
                left = cropX,
                top = cropY,
                right = cropX + cropWidth,
                bottom = cropY + cropHeight
            )
            println("✅ 이미지 로드 및 초기 크롭 영역 설정 완료")
        } ?: run {
            println("❌ imageBitmap이 null - 화면 종료")
            navController.popBackStack()
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
                    // 배경 제거 토글 버튼 (옵션)
                    IconButton(
                        onClick = { 
                            removeBackground = !removeBackground
                            println("배경 제거 옵션: $removeBackground")
                        },
                        enabled = !isProcessingBackground && imageBitmap != null
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = if (removeBackground) "배경 제거 해제" else "배경 제거",
                            tint = if (removeBackground) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            println("=== CropScreen: 완료 버튼 클릭 ===")
                            println("배경 제거 옵션: $removeBackground")
                            imageBitmap?.let { bitmap ->
                                cropRect?.let { rect ->
                                    println("원본 비트맵 크기: ${bitmap.width}x${bitmap.height}")
                                    println("크롭 영역: left=${rect.left}, top=${rect.top}, width=${rect.width}, height=${rect.height}")
                                    
                                    // 크롭 영역이 비트맵 범위를 벗어나지 않도록 제한
                                    val cropLeft = rect.left.toInt().coerceIn(0, bitmap.width - 1)
                                    val cropTop = rect.top.toInt().coerceIn(0, bitmap.height - 1)
                                    val cropWidth = rect.width.toInt().coerceIn(1, bitmap.width - cropLeft)
                                    val cropHeight = rect.height.toInt().coerceIn(1, bitmap.height - cropTop)
                                    
                                    println("조정된 크롭 영역: left=$cropLeft, top=$cropTop, width=$cropWidth, height=$cropHeight")
                                    
                                    // 크롭된 비트맵 생성
                                    val croppedBitmap = try {
                                        Bitmap.createBitmap(
                                            bitmap,
                                            cropLeft,
                                            cropTop,
                                            cropWidth,
                                            cropHeight
                                        )
                                    } catch (e: Exception) {
                                        println("❌ 크롭된 비트맵 생성 실패: ${e.message}")
                                        e.printStackTrace()
                                        null
                                    }
                                    
                                    croppedBitmap?.let { cropped ->
                                        println("크롭된 비트맵 생성 성공: ${cropped.width}x${cropped.height}")
                                        
                                        // 배경 제거 옵션이 활성화된 경우 배경 제거 수행
                                        var processedBitmap = cropped
                                        if (removeBackground && !isProcessingBackground) {
                                            isProcessingBackground = true
                                            try {
                                                println("배경 제거 시작...")
                                                processedBitmap = BackgroundRemover.removeBackground(cropped, context)
                                                println("배경 제거 완료: ${processedBitmap.width}x${processedBitmap.height}")
                                            } catch (e: Exception) {
                                                println("배경 제거 실패: ${e.message}")
                                                e.printStackTrace()
                                                // 배경 제거 실패 시 원본 크롭된 비트맵 사용
                                                processedBitmap = cropped
                                            } finally {
                                                isProcessingBackground = false
                                            }
                                        }
                                        
                                        // 크롭된 이미지를 임시 파일로 저장하고 Uri 반환
                                        val resultUri = saveBitmapToCache(context, processedBitmap)
                                        println("저장된 파일 URI: $resultUri")
                                        
                                        // 이전 화면으로 잘린 이미지 Uri 및 slotIndex 전달
                                        val previousEntry = navController.previousBackStackEntry
                                        if (previousEntry != null) {
                                            previousEntry.savedStateHandle.set("croppedImageUri", resultUri.toString())
                                            previousEntry.savedStateHandle.set("croppedImageSlotIndex", slotIndex)
                                            println("✅ savedStateHandle에 URI 및 slotIndex 저장 완료: URI=$resultUri, slotIndex=$slotIndex")
                                        } else {
                                            println("❌ previousBackStackEntry가 null")
                                        }
                                        
                                        // 메모리 정리
                                        if (cropped != bitmap && !cropped.isRecycled) {
                                            cropped.recycle()
                                        }
                                        
                                        navController.popBackStack()
                                    }
                                } ?: run {
                                    println("❌ cropRect가 null")
                                }
                            } ?: run {
                                println("❌ imageBitmap이 null")
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
    // 화면 크기를 State로 추적 (Canvas 내부에서 사용하기 위해)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    
    // 이미지를 화면 크기에 맞게 스케일링된 비트맵으로 변환 (Canvas 밖에서 계산)
    val scaledBitmap = remember(imageBitmap, canvasSize, imageSize) {
        if (imageBitmap != null && canvasSize.width > 0 && canvasSize.height > 0 && imageSize.width > 0 && imageSize.height > 0) {
            val scaleX = canvasSize.width.toFloat() / imageSize.width
            val scaleY = canvasSize.height.toFloat() / imageSize.height
            val actualScale = minOf(scaleX, scaleY)
            
            // 이미지가 화면보다 큰 경우에만 스케일링
            if (actualScale < 1f) {
                val scaledWidth = (imageBitmap.width * actualScale).toInt()
                val scaledHeight = (imageBitmap.height * actualScale).toInt()
                Bitmap.createScaledBitmap(imageBitmap, scaledWidth, scaledHeight, true)
            } else {
                imageBitmap
            }
        } else {
            imageBitmap
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(cropRect, imageSize) {
                // 드래그 시작 시 초기 크롭 영역 저장
                var startCropRect: Rect? = null
                
                detectDragGestures(
                    onDragStart = { offset ->
                        // 드래그 시작 시 현재 크롭 영역 저장
                        startCropRect = cropRect
                    }
                ) { change, dragAmount ->
                    // 드래그로 크롭 영역 이동
                    val scaleX = size.width / imageSize.width
                    val scaleY = size.height / imageSize.height
                    val actualScale = minOf(scaleX, scaleY)
                    
                    // 드래그 좌표를 이미지 좌표계로 변환
                    val scaledImageWidth = imageSize.width * actualScale
                    val scaledImageHeight = imageSize.height * actualScale
                    val imageOffsetX = (size.width - scaledImageWidth) / 2
                    val imageOffsetY = (size.height - scaledImageHeight) / 2
                    
                    // 화면 좌표를 이미지 좌표로 변환
                    val screenX = change.position.x - imageOffsetX
                    val screenY = change.position.y - imageOffsetY
                    
                    // 드래그 시작 시 저장된 크롭 영역 사용 (없으면 현재 크롭 영역 사용)
                    val currentCropRect = startCropRect ?: cropRect
                    
                    // 크롭 영역의 화면 좌표 계산
                    val cropScreenLeft = imageOffsetX + currentCropRect.left * actualScale
                    val cropScreenTop = imageOffsetY + currentCropRect.top * actualScale
                    val cropScreenRight = cropScreenLeft + currentCropRect.width * actualScale
                    val cropScreenBottom = cropScreenTop + currentCropRect.height * actualScale
                    
                    // 크롭 영역 내부 또는 테두리 근처에서 드래그 가능하도록 확장
                    // pointerInput 블록 내에서는 toPx()를 사용할 수 없으므로 화면 크기를 기준으로 계산
                    // 30dp를 픽셀로 변환 (대략 1dp = 1/160 인치, 일반 화면에서는 약 3-4픽셀)
                    val touchTolerance = size.width * 0.03f // 화면 너비의 3% (약 30dp)
                    val isInsideCropArea = 
                        change.position.x >= cropScreenLeft - touchTolerance &&
                        change.position.x <= cropScreenRight + touchTolerance &&
                        change.position.y >= cropScreenTop - touchTolerance &&
                        change.position.y <= cropScreenBottom + touchTolerance
                    
                    // 이미지 영역 내에서만 드래그 가능
                    val isInsideImageArea =
                        screenX >= -touchTolerance &&
                        screenX <= scaledImageWidth + touchTolerance &&
                        screenY >= -touchTolerance &&
                        screenY <= scaledImageHeight + touchTolerance
                    
                    if (isInsideCropArea && isInsideImageArea) {
                        // 드래그 양을 이미지 좌표계로 변환 (감도 향상을 위해 가중치 적용)
                        // actualScale이 작을수록 화면이 작게 보이므로, 드래그 감도를 높여야 함
                        val dragSensitivity = 2.0f // 드래그 감도 가중치 (2.0배)
                        val dragX = (dragAmount.x / actualScale) * dragSensitivity
                        val dragY = (dragAmount.y / actualScale) * dragSensitivity
                        
                        // 크롭 영역을 이동 (경계까지 정확하게 이동 가능하도록)
                        // 최대값: 이미지 크기 - 크롭 영역 크기 (끝까지 정확하게 이동 가능)
                        val maxLeft = imageSize.width - currentCropRect.width
                        val maxTop = imageSize.height - currentCropRect.height
                        
                        val newLeft = (currentCropRect.left + dragX).coerceIn(
                            0f,
                            maxLeft
                        )
                        val newTop = (currentCropRect.top + dragY).coerceIn(
                            0f,
                            maxTop
                        )
                        
                        val newRect = Rect(
                            left = newLeft,
                            top = newTop,
                            right = newLeft + currentCropRect.width,
                            bottom = newTop + currentCropRect.height
                        )
                        onCropRectChanged(newRect)
                        
                        // 드래그 중 크롭 영역 업데이트
                        startCropRect = newRect
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // 전체 이미지를 보여주기 위해 fit 방식 사용
            val scaleX = size.width / imageSize.width
            val scaleY = size.height / imageSize.height
            val actualScale = minOf(scaleX, scaleY) // 1f 제한 제거: 이미지 전체를 보여주기
            
            val scaledImageWidth = imageSize.width * actualScale
            val scaledImageHeight = imageSize.height * actualScale
            val imageOffsetX = (size.width - scaledImageWidth) / 2
            val imageOffsetY = (size.height - scaledImageHeight) / 2
            
            // 검은 배경 전체 그리기 (비율이 맞지 않을 때 보이도록)
            drawRect(
                color = Color.Black,
                size = size
            )
            
            // 크롭 영역을 화면 좌표계로 변환
            val cropScreenLeft = imageOffsetX + cropRect.left * actualScale
            val cropScreenTop = imageOffsetY + cropRect.top * actualScale
            val cropScreenWidth = cropRect.width * actualScale
            val cropScreenHeight = cropRect.height * actualScale
            val cropRectScreen = Rect(
                left = cropScreenLeft,
                top = cropScreenTop,
                right = cropScreenLeft + cropScreenWidth,
                bottom = cropScreenTop + cropScreenHeight
            )
            
            // 이미지 그리기 - 전체 이미지를 화면에 맞춰 표시 (fit 방식)
            // Canvas 밖에서 계산된 스케일링된 비트맵을 사용하여 그리기
            val displayBitmap = scaledBitmap ?: imageBitmap
            displayBitmap?.let { bitmap ->
                val imageBitmapCompose = bitmap.asImageBitmap()
                // 스케일링된 이미지를 중앙에 그리기
                drawImage(
                    image = imageBitmapCompose,
                    topLeft = Offset(imageOffsetX, imageOffsetY)
                )
            }
            
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

