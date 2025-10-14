package com.example.a4cut.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.repository.FrameRepository
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.database.AppDatabase
import com.example.a4cut.ui.utils.ImagePicker
import com.example.a4cut.ui.utils.PermissionHelper
import com.example.a4cut.ui.utils.ImageComposer
import com.example.a4cut.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Phase 2: 각 사진의 편집 상태(URI, 크기, 위치)를 관리하는 데이터 클래스
 */
data class PhotoState(
    val bitmap: Bitmap?,
    var scale: Float = 1f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)

/**
 * 프레임 화면의 ViewModel
 * 사진 선택, 프레임 적용, 이미지 합성 등 핵심 로직을 담당
 * Phase 2: 제스처 기반 사진 편집 기능 추가
 */
class FrameViewModel : ViewModel() {
    
    private val frameRepository = FrameRepository()
    private var imagePicker: ImagePicker? = null
    private var permissionHelper: PermissionHelper? = null
    private var imageComposer: ImageComposer? = null // ImageComposer 추가
    private var context: Context? = null // Context 저장
    private var photoRepository: PhotoRepository? = null // PhotoRepository 추가
    
    // 프레임 관련 상태
    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()
    
    private val _selectedFrame = MutableStateFlow<Frame?>(null)
    val selectedFrame: StateFlow<Frame?> = _selectedFrame.asStateFlow()
    
    // 사진 관련 상태 (Bitmap 기반)
    private val _photos = MutableStateFlow<List<Bitmap?>>(List(4) { null })
    val photos: StateFlow<List<Bitmap?>> = _photos.asStateFlow()
    
    // Phase 2: PhotoState 리스트로 사진 편집 상태 관리
    private val _photoStates = mutableStateListOf<PhotoState>()
    val photoStates: SnapshotStateList<PhotoState> = _photoStates
    
    // 선택된 이미지 URI를 저장할 StateFlow 추가
    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris: StateFlow<List<Uri>> = _selectedImageUris.asStateFlow()
    
    // 테스트용 사진 목록
    private val _testPhotos = MutableStateFlow<List<Bitmap>>(emptyList())
    val testPhotos: StateFlow<List<Bitmap>> = _testPhotos.asStateFlow()
    
    // 고품질 예시 사진 목록
    private val _examplePhotos = MutableStateFlow<List<Bitmap>>(emptyList())
    val examplePhotos: StateFlow<List<Bitmap>> = _examplePhotos.asStateFlow()
    
    // 권한 관련 상태
    private val _hasImagePermission = MutableStateFlow(false)
    val hasImagePermission: StateFlow<Boolean> = _hasImagePermission.asStateFlow()
    
    // UI 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // 합성된 최종 이미지를 저장할 상태
    private val _composedImage = MutableStateFlow<Bitmap?>(null)
    val composedImage: StateFlow<Bitmap?> = _composedImage.asStateFlow()
    
    // 인생네컷 예시 이미지 상태
    private val _life4CutExample = MutableStateFlow<Bitmap?>(null)
    val life4CutExample: StateFlow<Bitmap?> = _life4CutExample.asStateFlow()
    
    // 인스타그램 공유 Intent 상태
    private val _instagramShareIntent = MutableStateFlow<android.content.Intent?>(null)
    val instagramShareIntent: StateFlow<android.content.Intent?> = _instagramShareIntent.asStateFlow()
    
    // 이미지 선택 결과
    private val _imagePickerResult = MutableStateFlow<List<Uri>?>(null)
    val imagePickerResult: StateFlow<List<Uri>?> = _imagePickerResult.asStateFlow()
    
    init {
        loadFrames()
    }
    
    /**
     * 테스트용 사진들 로드
     */
    private fun loadTestPhotos() {
        viewModelScope.launch {
            try {
                println("테스트 사진 로드 시작")
                val testPhotoIds = listOf(
                    R.drawable.test_photo_1,
                    R.drawable.test_photo_2,
                    R.drawable.test_photo_3,
                    R.drawable.test_photo_4,
                    R.drawable.test_photo_5,
                    R.drawable.test_photo_6,
                    R.drawable.test_photo_7,
                    R.drawable.test_photo_8
                )
                
                val bitmaps = testPhotoIds.mapNotNull { drawableId: Int ->
                    try {
                        context?.let { ctx ->
                            BitmapFactory.decodeResource(ctx.resources, drawableId)?.let { bitmap ->
                                // 512x512 크기로 리사이즈
                                Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                            }
                        }
                    } catch (e: Exception) {
                        println("테스트 사진 로드 실패: $drawableId - ${e.message}")
                        null
                    }
                }
                
                println("테스트 사진 로드 완료: ${bitmaps.size}개")
                _testPhotos.value = bitmaps
            } catch (e: Exception) {
                println("테스트 사진 로드 전체 실패: ${e.message}")
                _errorMessage.value = "테스트 사진 로드 실패: ${e.message}"
            }
        }
    }
    
    /**
     * 고품질 예시 사진들 로드
     */
    private fun loadExamplePhotos() {
        viewModelScope.launch {
            try {
                println("고품질 예시 사진 로드 시작")
                val examplePhotoIds = listOf(
                    R.drawable.example_photo_train_window,
                    R.drawable.example_photo_station_platform,
                    R.drawable.example_photo_travel_destination,
                    R.drawable.example_photo_friends_together,
                    R.drawable.example_photo_sunset_view,
                    R.drawable.example_photo_city_skyline,
                    R.drawable.example_photo_food_memory,
                    R.drawable.example_photo_adventure_moment
                )
                
                val bitmaps = examplePhotoIds.mapNotNull { drawableId: Int ->
                    try {
                        context?.let { ctx ->
                            BitmapFactory.decodeResource(ctx.resources, drawableId)?.let { bitmap ->
                                // 512x512 크기로 리사이즈
                                Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                            }
                        }
                    } catch (e: Exception) {
                        println("예시 사진 로드 실패: $drawableId - ${e.message}")
                        null
                    }
                }
                
                println("고품질 예시 사진 로드 완료: ${bitmaps.size}개")
                _examplePhotos.value = bitmaps
            } catch (e: Exception) {
                println("고품질 예시 사진 로드 전체 실패: ${e.message}")
                _errorMessage.value = "예시 사진 로드 실패: ${e.message}"
            }
        }
    }
    
    /**
     * 인생네컷 예시 이미지 생성 (기본)
     */
    private fun generateLife4CutExample() {
        viewModelScope.launch {
            try {
                context?.let { ctx ->
                    // 인생네컷 예시 이미지 생성 (1080x1920)
                    val exampleBitmap = createLife4CutExampleBitmap(ctx)
                    _life4CutExample.value = exampleBitmap
                }
            } catch (e: Exception) {
                _errorMessage.value = "인생네컷 예시 생성 실패: ${e.message}"
            }
        }
    }
    
    /**
     * 동적 인생네컷 예시 생성 (랜덤 조합)
     */
    fun generateRandomLife4CutExample() {
        viewModelScope.launch {
            try {
                context?.let { ctx ->
                    val examplePhotos = _examplePhotos.value
                    val frames = _frames.value
                    
                    if (examplePhotos.isNotEmpty() && frames.isNotEmpty()) {
                        // 랜덤하게 4장의 예시 사진 선택
                        val selectedPhotos = examplePhotos.shuffled().take(4)
                        
                        // 랜덤하게 프레임 선택
                        val selectedFrame = frames.random()
                        
                        // 동적 예시 이미지 생성
                        val exampleBitmap = createDynamicLife4CutExample(
                            ctx, 
                            selectedPhotos, 
                            selectedFrame
                        )
                        _life4CutExample.value = exampleBitmap
                        
                        println("동적 인생네컷 예시 생성 완료: 프레임=${selectedFrame.name}")
                    } else {
                        // 기본 예시 생성
                        generateLife4CutExample()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "동적 예시 생성 실패: ${e.message}"
            }
        }
    }
    
    /**
     * 인생네컷 예시 Bitmap 생성 (세련된 디자인)
     */
    private fun createLife4CutExampleBitmap(context: Context): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // 배경 그라데이션 (KTX 브랜드 컬러)
        val backgroundPaint = android.graphics.Paint()
        val backgroundGradient = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(0xFF1E3A8A.toInt(), 0xFF3B82F6.toInt(), 0xFF60A5FA.toInt()),
            floatArrayOf(0f, 0.5f, 1f),
            android.graphics.Shader.TileMode.CLAMP
        )
        backgroundPaint.shader = backgroundGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // 상단 장식 바
        val topBarPaint = android.graphics.Paint().apply {
            color = 0xFFF59E0B.toInt()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), 100f, topBarPaint)
        
        // 하단 장식 바
        canvas.drawRect(0f, height - 100f, width.toFloat(), height.toFloat(), topBarPaint)
        
        // 좌측 장식 바
        canvas.drawRect(0f, 0f, 100f, height.toFloat(), topBarPaint)
        
        // 우측 장식 바
        canvas.drawRect(width - 100f, 0f, width.toFloat(), height.toFloat(), topBarPaint)
        
        // 4컷 영역 그리기 (더 세련된 스타일)
        val margin = 140f
        val spacing = 20f
        val photoWidth = (width - 2 * margin - spacing) / 2
        val photoHeight = (height - 2 * margin - spacing) / 2
        
        val framePaint = android.graphics.Paint().apply {
            color = 0xFFFFFFFF.toInt()
            strokeWidth = 6f
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }
        
        val fillPaint = android.graphics.Paint().apply {
            color = 0xFFFFFFFF.toInt()
            alpha = 20
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }
        
        // 4컷 영역 배경
        canvas.drawRoundRect(margin, margin, width - margin, height - margin, 20f, 20f, fillPaint)
        
        // 4컷 영역 테두리
        canvas.drawRoundRect(margin, margin, width - margin, height - margin, 20f, 20f, framePaint)
        
        // 상단 좌측
        canvas.drawRoundRect(margin, margin, margin + photoWidth, margin + photoHeight, 12f, 12f, framePaint)
        // 상단 우측
        canvas.drawRoundRect(margin + photoWidth + spacing, margin, width - margin, margin + photoHeight, 12f, 12f, framePaint)
        // 하단 좌측
        canvas.drawRoundRect(margin, margin + photoHeight + spacing, margin + photoWidth, height - margin, 12f, 12f, framePaint)
        // 하단 우측
        canvas.drawRoundRect(margin + photoWidth + spacing, margin + photoHeight + spacing, width - margin, height - margin, 12f, 12f, framePaint)
        
        // 4컷 사진 영역에 귀여운 사람 모습 그리기
        drawCutePeopleInFrames(canvas, margin, photoWidth, photoHeight, spacing, width, height)
        
        // 구분선 그리기
        val linePaint = android.graphics.Paint().apply {
            color = 0xFFFFFFFF.toInt()
            strokeWidth = 3f
            alpha = 100
            isAntiAlias = true
        }
        
        // 가로 구분선
        canvas.drawLine(margin, height / 2f, width - margin, height / 2f, linePaint)
        // 세로 구분선
        canvas.drawLine(width / 2f, margin, width / 2f, height - margin, linePaint)
        
        // 상단 KTX 로고 영역
        val logoPaint = android.graphics.Paint().apply {
            color = 0xFFFFFFFF.toInt()
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }
        
        canvas.drawRoundRect(400f, 140f, 680f, 200f, 20f, 20f, logoPaint)
        
        // KTX 로고 텍스트
        val textPaint = android.graphics.Paint().apply {
            color = 0xFF1E3A8A.toInt()
            textSize = 36f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        canvas.drawText("KTX", width / 2f, 175f, textPaint)
        
        // 하단 텍스트
        val bottomTextPaint = android.graphics.Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 32f
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        canvas.drawText("인생네컷 예시", width / 2f, height - 150f, bottomTextPaint)
        
        // 장식 패턴 (점선 효과)
        val dotPaint = android.graphics.Paint().apply {
            color = 0xFFF59E0B.toInt()
            alpha = 150
            style = android.graphics.Paint.Style.FILL
            isAntiAlias = true
        }
        
        // 상단 장식 점들
        for (i in 0..7) {
            val x = 200f + i * 100f
            canvas.drawCircle(x, 50f, 8f, dotPaint)
        }
        
        // 하단 장식 점들
        for (i in 0..7) {
            val x = 200f + i * 100f
            canvas.drawCircle(x, height - 50f, 8f, dotPaint)
        }
        
        return bitmap
    }
    
    /**
     * 동적 인생네컷 예시 Bitmap 생성 (실제 사진과 프레임 조합)
     */
    private fun createDynamicLife4CutExample(
        context: Context,
        selectedPhotos: List<Bitmap>,
        selectedFrame: Frame
    ): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // 선택된 프레임을 배경으로 사용
        try {
            val frameDrawable = context.getDrawable(selectedFrame.drawableId)
            frameDrawable?.let { drawable ->
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
            }
        } catch (e: Exception) {
            println("프레임 로드 실패, 기본 배경 사용: ${e.message}")
            // 기본 배경 그라데이션
            val backgroundPaint = android.graphics.Paint()
            val backgroundGradient = android.graphics.LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF1E3A8A.toInt(), 0xFF3B82F6.toInt(), 0xFF60A5FA.toInt()),
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            backgroundPaint.shader = backgroundGradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        }
        
        // 4컷 사진 영역에 실제 예시 사진들 배치
        val margin = 140f
        val spacing = 20f
        val photoWidth = (width - 2 * margin - spacing) / 2
        val photoHeight = (height - 2 * margin - spacing) / 2
        
        // 4개 위치에 사진 배치
        val positions = listOf(
            android.graphics.RectF(margin, margin, margin + photoWidth, margin + photoHeight), // 상단 좌측
            android.graphics.RectF(margin + photoWidth + spacing, margin, width - margin, margin + photoHeight), // 상단 우측
            android.graphics.RectF(margin, margin + photoHeight + spacing, margin + photoWidth, height - margin), // 하단 좌측
            android.graphics.RectF(margin + photoWidth + spacing, margin + photoHeight + spacing, width - margin, height - margin) // 하단 우측
        )
        
        selectedPhotos.forEachIndexed { index, photo ->
            if (index < 4) {
                val destRect = positions[index]
                canvas.drawBitmap(photo, null, destRect, null)
                
                // 사진 테두리 추가
                val borderPaint = android.graphics.Paint().apply {
                    color = 0xFFFFFFFF.toInt()
                    strokeWidth = 4f
                    style = android.graphics.Paint.Style.STROKE
                    isAntiAlias = true
                }
                canvas.drawRect(destRect, borderPaint)
            }
        }
        
        // 프레임 정보 텍스트 추가
        val textPaint = android.graphics.Paint().apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 48f
            isAntiAlias = true
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        val frameInfoText = "${selectedFrame.name} 프레임"
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(frameInfoText, 0, frameInfoText.length, textBounds)
        val textX = (width - textBounds.width()) / 2f
        val textY = height - 80f
        
        // 텍스트 배경
        val textBgPaint = android.graphics.Paint().apply {
            color = 0x80000000.toInt() // 반투명 검은색
        }
        val textBgRect = android.graphics.RectF(
            textX - 20f, textY - textBounds.height() - 20f,
            textX + textBounds.width() + 20f, textY + 20f
        )
        canvas.drawRoundRect(textBgRect, 20f, 20f, textBgPaint)
        
        // 텍스트 그리기
        canvas.drawText(frameInfoText, textX, textY, textPaint)
        
        return bitmap
    }
    
    /**
     * 4컷 사진 영역에 귀여운 사람 모습 그리기
     */
    private fun drawCutePeopleInFrames(
        canvas: android.graphics.Canvas,
        margin: Float,
        photoWidth: Float,
        photoHeight: Float,
        spacing: Float,
        width: Int,
        height: Int
    ) {
        val personPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        
        val strokePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            color = 0xFFFFFFFF.toInt()
        }
        
        // 상단 좌측 - 웃는 얼굴
        drawCuteFace(
            canvas, 
            margin + photoWidth/2, 
            margin + photoHeight/2, 
            photoWidth * 0.3f, 
            personPaint, 
            strokePaint,
            0xFFFBBF24.toInt() // 노란색
        )
        
        // 상단 우측 - 윙크하는 얼굴
        drawWinkingFace(
            canvas, 
            margin + photoWidth + spacing + photoWidth/2, 
            margin + photoHeight/2, 
            photoWidth * 0.3f, 
            personPaint, 
            strokePaint,
            0xFFEC4899.toInt() // 핑크색
        )
        
        // 하단 좌측 - 놀란 얼굴
        drawSurprisedFace(
            canvas, 
            margin + photoWidth/2, 
            margin + photoHeight + spacing + photoHeight/2, 
            photoWidth * 0.3f, 
            personPaint, 
            strokePaint,
            0xFF4ECDC4.toInt() // 청록색
        )
        
        // 하단 우측 - 사랑스러운 얼굴
        drawLovingFace(
            canvas, 
            margin + photoWidth + spacing + photoWidth/2, 
            margin + photoHeight + spacing + photoHeight/2, 
            photoWidth * 0.3f, 
            personPaint, 
            strokePaint,
            0xFF8B5CF6.toInt() // 보라색
        )
    }
    
    /**
     * 웃는 얼굴 그리기
     */
    private fun drawCuteFace(
        canvas: android.graphics.Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        fillPaint: android.graphics.Paint,
        strokePaint: android.graphics.Paint,
        color: Int
    ) {
        fillPaint.color = color
        strokePaint.color = 0xFFFFFFFF.toInt()
        
        // 얼굴
        canvas.drawCircle(centerX, centerY, size, fillPaint)
        canvas.drawCircle(centerX, centerY, size, strokePaint)
        
        // 눈
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.1f, fillPaint)
        canvas.drawCircle(centerX + size * 0.3f, centerY - size * 0.2f, size * 0.1f, fillPaint)
        
        // 눈동자
        fillPaint.color = 0xFF000000.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.05f, fillPaint)
        canvas.drawCircle(centerX + size * 0.3f, centerY - size * 0.2f, size * 0.05f, fillPaint)
        
        // 입 (웃는 모습)
        strokePaint.color = 0xFF000000.toInt()
        strokePaint.strokeWidth = 4f
        canvas.drawArc(
            centerX - size * 0.3f, centerY - size * 0.1f, 
            centerX + size * 0.3f, centerY + size * 0.2f,
            0f, 180f, false, strokePaint
        )
    }
    
    /**
     * 윙크하는 얼굴 그리기
     */
    private fun drawWinkingFace(
        canvas: android.graphics.Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        fillPaint: android.graphics.Paint,
        strokePaint: android.graphics.Paint,
        color: Int
    ) {
        fillPaint.color = color
        strokePaint.color = 0xFFFFFFFF.toInt()
        
        // 얼굴
        canvas.drawCircle(centerX, centerY, size, fillPaint)
        canvas.drawCircle(centerX, centerY, size, strokePaint)
        
        // 왼쪽 눈 (일반)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.1f, fillPaint)
        fillPaint.color = 0xFF000000.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.05f, fillPaint)
        
        // 오른쪽 눈 (윙크)
        strokePaint.color = 0xFF000000.toInt()
        strokePaint.strokeWidth = 4f
        canvas.drawLine(
            centerX + size * 0.2f, centerY - size * 0.2f,
            centerX + size * 0.4f, centerY - size * 0.2f,
            strokePaint
        )
        
        // 입 (웃는 모습)
        canvas.drawArc(
            centerX - size * 0.3f, centerY - size * 0.1f, 
            centerX + size * 0.3f, centerY + size * 0.2f,
            0f, 180f, false, strokePaint
        )
    }
    
    /**
     * 놀란 얼굴 그리기
     */
    private fun drawSurprisedFace(
        canvas: android.graphics.Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        fillPaint: android.graphics.Paint,
        strokePaint: android.graphics.Paint,
        color: Int
    ) {
        fillPaint.color = color
        strokePaint.color = 0xFFFFFFFF.toInt()
        
        // 얼굴
        canvas.drawCircle(centerX, centerY, size, fillPaint)
        canvas.drawCircle(centerX, centerY, size, strokePaint)
        
        // 눈 (크게 뜬 모습)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.15f, fillPaint)
        canvas.drawCircle(centerX + size * 0.3f, centerY - size * 0.2f, size * 0.15f, fillPaint)
        
        // 눈동자
        fillPaint.color = 0xFF000000.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.08f, fillPaint)
        canvas.drawCircle(centerX + size * 0.3f, centerY - size * 0.2f, size * 0.08f, fillPaint)
        
        // 입 (동그란 모습)
        canvas.drawCircle(centerX, centerY + size * 0.1f, size * 0.1f, fillPaint)
    }
    
    /**
     * 사랑스러운 얼굴 그리기
     */
    private fun drawLovingFace(
        canvas: android.graphics.Canvas,
        centerX: Float,
        centerY: Float,
        size: Float,
        fillPaint: android.graphics.Paint,
        strokePaint: android.graphics.Paint,
        color: Int
    ) {
        fillPaint.color = color
        strokePaint.color = 0xFFFFFFFF.toInt()
        
        // 얼굴
        canvas.drawCircle(centerX, centerY, size, fillPaint)
        canvas.drawCircle(centerX, centerY, size, strokePaint)
        
        // 눈 (하트 모양)
        fillPaint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.1f, fillPaint)
        canvas.drawCircle(centerX + size * 0.3f, centerY - size * 0.2f, size * 0.1f, fillPaint)
        
        // 눈동자
        fillPaint.color = 0xFF000000.toInt()
        canvas.drawCircle(centerX - size * 0.3f, centerY - size * 0.2f, size * 0.05f, fillPaint)
        canvas.drawCircle(centerX + size * 0.3f, centerY - size * 0.2f, size * 0.05f, fillPaint)
        
        // 볼 (빨간 볼)
        fillPaint.color = 0xFFFF6B6B.toInt()
        canvas.drawCircle(centerX - size * 0.4f, centerY, size * 0.08f, fillPaint)
        canvas.drawCircle(centerX + size * 0.4f, centerY, size * 0.08f, fillPaint)
        
        // 입 (사랑스러운 웃음)
        strokePaint.color = 0xFF000000.toInt()
        strokePaint.strokeWidth = 3f
        canvas.drawArc(
            centerX - size * 0.2f, centerY - size * 0.05f, 
            centerX + size * 0.2f, centerY + size * 0.15f,
            0f, 180f, false, strokePaint
        )
    }
    
    /**
     * Context 설정 (권한 및 이미지 처리에 필요)
     */
    fun setContext(context: Context) {
        println("setContext 호출됨")
        this.context = context
        imagePicker = ImagePicker(context)
        permissionHelper = PermissionHelper(context)
        imageComposer = ImageComposer(context) // ImageComposer 초기화
        
        // PhotoRepository 초기화
        val database = AppDatabase.getDatabase(context)
        photoRepository = PhotoRepository(database.photoDao())
        
        checkImagePermission()
        
        // 기존 사진 데이터가 없을 때만 테스트 사진들 로드
        if (_photos.value.all { it == null }) {
            loadTestPhotos() // 테스트용 사진들 로드
            loadExamplePhotos() // 고품질 예시 사진들 로드
            generateLife4CutExample() // 인생네컷 예시 생성
        } else {
            println("기존 사진 데이터가 있으므로 테스트 사진 로드 건너뜀")
        }
        println("setContext 완료")
    }
    
    /**
     * 이미지 권한 확인
     */
    private fun checkImagePermission() {
        permissionHelper?.let { helper ->
            _hasImagePermission.value = helper.isImagePermissionGranted()
        }
    }
    
    /**
     * 프레임 목록 로드
     */
    private fun loadFrames() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // StateFlow에서 한 번만 값을 가져오기
                val frameList = frameRepository.getFrames()
                _frames.value = frameList
            } catch (e: Exception) {
                _errorMessage.value = "프레임 로드 실패: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 프레임 선택
     */
    fun selectFrame(frame: Frame) {
        println("프레임 선택됨: ${frame.name} (ID: ${frame.id}, DrawableID: ${frame.drawableId})")
        _selectedFrame.value = frame
        clearError()
        // 햅틱 피드백 추가
        triggerHapticFeedback()
    }
    
    /**
     * 사진 선택 (Bitmap 기반)
     */
    fun selectPhoto(index: Int, bitmap: Bitmap?) {
        println("=== selectPhoto 호출됨 ===")
        println("selectPhoto: index=$index, bitmap=${bitmap != null}")
        if (bitmap != null) {
            println("selectPhoto: bitmap 크기 = ${bitmap.width}x${bitmap.height}")
        }
        println("selectPhoto: 현재 사진 상태 = ${_photos.value.map { it != null }}")
        
        if (index in 0..3) {
            val currentPhotos = _photos.value.toMutableList()
            currentPhotos[index] = bitmap
            _photos.value = currentPhotos
            
            // Phase 2: PhotoState도 함께 업데이트
            updatePhotoStateFromBitmap(index, bitmap)
            
            println("selectPhoto: 사진 선택 완료")
            println("selectPhoto: 새로운 그리드 상태=${_photos.value.map { it != null }}")
            println("selectPhoto: 선택된 사진 개수 = ${_photos.value.count { it != null }}")
            clearError()
        } else {
            println("selectPhoto: 잘못된 사진 인덱스: $index")
            _errorMessage.value = "잘못된 사진 인덱스입니다: $index"
        }
        println("=== selectPhoto 완료 ===")
    }
    
    /**
     * 사진 제거
     */
    fun removePhoto(index: Int) {
        if (index in 0..3) {
            val currentPhotos = _photos.value.toMutableList()
            // 기존 Bitmap 메모리 해제
            currentPhotos[index]?.let { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            currentPhotos[index] = null
            _photos.value = currentPhotos
            clearError()
        }
    }
    
    
    /**
     * 테스트용 사진 선택
     */
    fun selectTestPhoto(gridIndex: Int, testPhotoIndex: Int) {
        if (gridIndex in 0..3 && testPhotoIndex in 0 until _testPhotos.value.size) {
            val testPhoto = _testPhotos.value[testPhotoIndex]
            selectPhoto(gridIndex, testPhoto)
            clearError()
        } else {
            _errorMessage.value = "잘못된 사진 인덱스입니다"
        }
    }
    
    /**
     * Phase 1: 사진 선택 토글 (미리보기에서 사용)
     */
    fun togglePhotoSelection(index: Int) {
        if (index in 0..3) {
            val currentPhotos = _photos.value.toMutableList()
            val currentPhoto = currentPhotos[index]
            
            if (currentPhoto != null) {
                // 사진이 있으면 제거
                if (!currentPhoto.isRecycled) {
                    currentPhoto.recycle()
                }
                currentPhotos[index] = null
                // PhotoState도 업데이트
                updatePhotoStateFromBitmap(index, null)
            } else {
                // 사진이 없으면 랜덤 테스트 사진 추가
                selectRandomTestPhoto()
            }
            
            _photos.value = currentPhotos
            clearError()
        } else {
            _errorMessage.value = "잘못된 사진 인덱스입니다: $index"
        }
    }
    
    /**
     * Phase 2: PhotoState를 Bitmap으로부터 초기화/업데이트
     */
    private fun updatePhotoStateFromBitmap(index: Int, bitmap: Bitmap?) {
        if (index in 0..3) {
            // PhotoState 리스트가 비어있으면 4개로 초기화
            while (_photoStates.size < 4) {
                _photoStates.add(PhotoState(null))
            }
            
            // 해당 인덱스의 PhotoState 업데이트
            _photoStates[index] = PhotoState(
                bitmap = bitmap,
                scale = 1f,
                offsetX = 0f,
                offsetY = 0f
            )
        }
    }
    
    /**
     * Phase 2: 특정 사진의 편집 상태(크기, 위치)를 업데이트하는 함수
     */
    fun updatePhotoState(index: Int, scale: Float, offsetX: Float, offsetY: Float) {
        if (index in _photoStates.indices) {
            val currentState = _photoStates[index]
            _photoStates[index] = currentState.copy(
                scale = (currentState.scale * scale).coerceIn(0.5f, 3f), // 확대/축소 범위 제한
                offsetX = (currentState.offsetX + offsetX).coerceIn(-200f, 200f), // 이동 범위 제한
                offsetY = (currentState.offsetY + offsetY).coerceIn(-200f, 200f)
            )
        }
    }
    
    /**
     * Phase 2: 사진 편집 상태 초기화
     */
    fun resetPhotoState(index: Int) {
        if (index in _photoStates.indices) {
            val currentState = _photoStates[index]
            _photoStates[index] = currentState.copy(
                scale = 1f,
                offsetX = 0f,
                offsetY = 0f
            )
        }
    }
    
    /**
     * 이미지 선택기 열기
     */
    fun openImagePicker() {
        if (!_hasImagePermission.value) {
            _errorMessage.value = "갤러리 접근 권한이 필요합니다"
            return
        }
        
        // 이미지 선택 결과를 처리할 준비
        _imagePickerResult.value = emptyList()
    }
    
    /**
     * 랜덤 테스트 사진 선택 (빈 그리드 위치에)
     */
    fun selectRandomTestPhoto() {
        println("selectRandomTestPhoto 호출됨")
        // 빈 그리드 위치 찾기
        val emptyIndex = _photos.value.indexOfFirst { it == null }
        println("빈 그리드 위치: $emptyIndex, 테스트 사진 개수: ${_testPhotos.value.size}")
        println("현재 그리드 상태: ${_photos.value.map { it != null }}")
        
        if (emptyIndex != -1) {
            val randomPhoto = if (_testPhotos.value.isNotEmpty()) {
                val randomIndex = (0 until _testPhotos.value.size).random()
                _testPhotos.value[randomIndex]
            } else {
                // 테스트 사진이 없으면 더미 사진 생성
                createDummyPhoto()
            }
            
            if (randomPhoto != null) {
                println("랜덤 사진 선택: 위치 $emptyIndex")
                println("선택된 사진 크기: ${randomPhoto.width}x${randomPhoto.height}")
                selectPhoto(emptyIndex, randomPhoto)
                clearError()
            } else {
                println("사진 생성 실패")
                _errorMessage.value = "사진 생성에 실패했습니다"
            }
        } else {
            println("모든 그리드가 채워져 있음")
            _errorMessage.value = "모든 그리드가 채워져 있습니다"
        }
    }
    
    /**
     * 더미 사진 생성 (세련된 디자인)
     */
    private fun createDummyPhoto(): Bitmap? {
        return try {
            val width = 512
            val height = 512
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            
            // 테마별 색상 조합
            val themes = listOf(
                // KTX 블루 테마
                Triple(
                    intArrayOf(0xFF1E3A8A.toInt(), 0xFF3B82F6.toInt(), 0xFF60A5FA.toInt()),
                    "KTX",
                    0xFFFFFFFF.toInt()
                ),
                // 선셋 테마
                Triple(
                    intArrayOf(0xFFFF6B6B.toInt(), 0xFFFF8E53.toInt(), 0xFFFFB347.toInt()),
                    "SUNSET",
                    0xFFFFFFFF.toInt()
                ),
                // 오션 테마
                Triple(
                    intArrayOf(0xFF4ECDC4.toInt(), 0xFF45B7D1.toInt(), 0xFF87CEEB.toInt()),
                    "OCEAN",
                    0xFFFFFFFF.toInt()
                ),
                // 포레스트 테마
                Triple(
                    intArrayOf(0xFF96CEB4.toInt(), 0xFF85C1A3.toInt(), 0xFF74B896.toInt()),
                    "FOREST",
                    0xFFFFFFFF.toInt()
                ),
                // 퍼플 테마
                Triple(
                    intArrayOf(0xFF8B5CF6.toInt(), 0xFFA78BFA.toInt(), 0xFFC4B5FD.toInt()),
                    "PURPLE",
                    0xFFFFFFFF.toInt()
                ),
                // 골드 테마
                Triple(
                    intArrayOf(0xFFF59E0B.toInt(), 0xFFFBBF24.toInt(), 0xFFFCD34D.toInt()),
                    "GOLD",
                    0xFFFFFFFF.toInt()
                ),
                // 로즈 테마
                Triple(
                    intArrayOf(0xFFEC4899.toInt(), 0xFFF472B6.toInt(), 0xFFF9A8D4.toInt()),
                    "ROSE",
                    0xFFFFFFFF.toInt()
                ),
                // 그레이 테마
                Triple(
                    intArrayOf(0xFF6B7280.toInt(), 0xFF9CA3AF.toInt(), 0xFFD1D5DB.toInt()),
                    "GRAY",
                    0xFFFFFFFF.toInt()
                )
            )
            
            val selectedTheme = themes.random()
            val colors = selectedTheme.first
            val themeName = selectedTheme.second
            val textColor = selectedTheme.third
            
            // 그라데이션 배경
            val gradient = android.graphics.LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                colors,
                floatArrayOf(0f, 0.5f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            
            val backgroundPaint = android.graphics.Paint().apply {
                shader = gradient
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
            
            // 패턴 추가 (원형)
            val patternPaint = android.graphics.Paint().apply {
                color = textColor
                alpha = 30
                isAntiAlias = true
            }
            
            for (i in 0..20) {
                val x = (0..width).random().toFloat()
                val y = (0..height).random().toFloat()
                val radius = (10..50).random().toFloat()
                canvas.drawCircle(x, y, radius, patternPaint)
            }
            
            // 중앙에 귀여운 사람 모습 그리기
            drawCutePersonInTestPhoto(canvas, width, height, textColor)
            
            // 중앙에 테마명 표시
            val textPaint = android.graphics.Paint().apply {
                color = textColor
                textSize = 28f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            canvas.drawText(themeName, width / 2f, height - 80f, textPaint)
            
            // 하단에 "KTX 네컷" 텍스트
            val subtitlePaint = android.graphics.Paint().apply {
                color = textColor
                textSize = 20f
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
                alpha = 180
            }
            
            canvas.drawText("KTX 네컷", width / 2f, height - 40f, subtitlePaint)
            
            // 테두리 추가
            val borderPaint = android.graphics.Paint().apply {
                color = textColor
                strokeWidth = 4f
                style = android.graphics.Paint.Style.STROKE
                isAntiAlias = true
                alpha = 100
            }
            
            canvas.drawRoundRect(8f, 8f, width - 8f, height - 8f, 16f, 16f, borderPaint)
            
            bitmap
        } catch (e: Exception) {
            println("더미 사진 생성 실패: ${e.message}")
            null
        }
    }
    
    /**
     * 테스트 사진에 귀여운 사람 모습 그리기
     */
    private fun drawCutePersonInTestPhoto(
        canvas: android.graphics.Canvas,
        width: Int,
        height: Int,
        textColor: Int
    ) {
        val centerX = width / 2f
        val centerY = height / 2f
        val size = minOf(width, height) * 0.3f
        
        val personPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        
        val strokePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 3f
            color = textColor
        }
        
        // 랜덤한 표정 선택
        val expressions = listOf("smile", "wink", "surprised", "loving")
        val expression = expressions.random()
        
        when (expression) {
            "smile" -> drawCuteFace(canvas, centerX, centerY, size, personPaint, strokePaint, 0xFFFBBF24.toInt())
            "wink" -> drawWinkingFace(canvas, centerX, centerY, size, personPaint, strokePaint, 0xFFEC4899.toInt())
            "surprised" -> drawSurprisedFace(canvas, centerX, centerY, size, personPaint, strokePaint, 0xFF4ECDC4.toInt())
            "loving" -> drawLovingFace(canvas, centerX, centerY, size, personPaint, strokePaint, 0xFF8B5CF6.toInt())
        }
        
        // 귀여운 포즈 추가 (손 흔들기)
        val handPaint = android.graphics.Paint().apply {
            color = 0xFFFFB347.toInt()
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
        }
        
        // 왼쪽 손
        canvas.drawCircle(centerX - size * 0.8f, centerY + size * 0.3f, size * 0.15f, handPaint)
        // 오른쪽 손
        canvas.drawCircle(centerX + size * 0.8f, centerY + size * 0.3f, size * 0.15f, handPaint)
        
        // 손가락 (V자 모양)
        strokePaint.color = 0xFFFFB347.toInt()
        strokePaint.strokeWidth = 4f
        canvas.drawLine(
            centerX - size * 0.8f, centerY + size * 0.3f,
            centerX - size * 0.9f, centerY + size * 0.1f,
            strokePaint
        )
        canvas.drawLine(
            centerX - size * 0.8f, centerY + size * 0.3f,
            centerX - size * 0.7f, centerY + size * 0.1f,
            strokePaint
        )
        
        canvas.drawLine(
            centerX + size * 0.8f, centerY + size * 0.3f,
            centerX + size * 0.9f, centerY + size * 0.1f,
            strokePaint
        )
        canvas.drawLine(
            centerX + size * 0.8f, centerY + size * 0.3f,
            centerX + size * 0.7f, centerY + size * 0.1f,
            strokePaint
        )
    }
    
    /**
     * 이미지 선택 결과 처리
     */
    fun processSelectedImages(uris: List<Uri>) {
        if (uris.isEmpty()) return
        
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                imagePicker?.let { picker ->
                    // 선택된 이미지들을 4컷 그리드에 맞게 처리
                    val processedBitmaps = picker.processImagesForGrid(uris, 512)
                    
                    // 기존 Bitmap들 메모리 해제
                    _photos.value.forEach { bitmap ->
                        bitmap?.let { 
                            if (!it.isRecycled) {
                                it.recycle()
                            }
                        }
                    }
                    
                    // 새로운 Bitmap들로 교체
                    _photos.value = processedBitmaps
                    clearError()
                } ?: run {
                    _errorMessage.value = "이미지 처리기를 초기화할 수 없습니다"
                }
            } catch (e: Exception) {
                _errorMessage.value = "이미지 처리 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 이미지 합성 시작
     */
    fun startImageComposition() {
        println("=== startImageComposition 시작 ===")
        println("selectedFrame: ${_selectedFrame.value}")
        println("photos: ${_photos.value.map { it != null }}")
        println("photoStates: ${_photoStates.map { it.bitmap != null }}")
        
        if (_selectedFrame.value == null) {
            _errorMessage.value = "프레임을 선택해주세요"
            println("startImageComposition: 프레임이 선택되지 않음")
            return
        }

        // 두 가지 방식으로 사진 확인
        val hasPhotosInPhotos = _photos.value.any { it != null }
        val hasPhotosInStates = _photoStates.any { it.bitmap != null }
        
        println("startImageComposition: hasPhotosInPhotos = $hasPhotosInPhotos")
        println("startImageComposition: hasPhotosInStates = $hasPhotosInStates")
        
        if (!hasPhotosInPhotos && !hasPhotosInStates) {
            _errorMessage.value = "최소 한 장의 사진을 선택해주세요"
            println("startImageComposition: 사진이 선택되지 않음")
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // 기존 합성된 이미지 메모리 해제
                _composedImage.value?.let { bitmap ->
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                }

                // 실제 KTX 시그니처 프레임 리소스 로드
                val frameBitmap = loadKtxFrameBitmap()

                imageComposer?.let { composer ->
                    // 선택된 프레임 ID에 따른 분기 처리
                    val selectedFrame = _selectedFrame.value
                    val result = when (selectedFrame?.id) {
                        1 -> {
                            // 인생네컷 프레임 전용 합성 함수 사용
                            val photos = _photoStates.map { it.bitmap }
                            composer.composeLife4CutFrame(frameBitmap, photos)
                        }
                        else -> {
                            // 기존 합성 로직 사용
                            composer.composeImageWithPhotoStates(
                                photoStates = _photoStates.toList(),
                                frameBitmap = frameBitmap
                            )
                        }
                    }
                    _composedImage.value = result // 합성 결과 저장
                    clearError()
                } ?: run {
                    _errorMessage.value = "이미지 합성기를 초기화할 수 없습니다"
                }

                // 프레임 메모리 해제
                frameBitmap.recycle()

            } catch (e: Exception) {
                _errorMessage.value = "이미지 합성 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 이미지 저장
     */
    fun saveImage() {
        val imageToSave = _composedImage.value
        if (imageToSave == null) {
            _errorMessage.value = "합성된 이미지가 없습니다. 먼저 이미지 합성을 해주세요."
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val fileName = "KTX_4cut_${System.currentTimeMillis()}.jpg"
                val savedUri = imageComposer?.saveBitmapToGallery(imageToSave, fileName)
                
                if (savedUri != null) {
                    // 갤러리 저장 성공 시 데이터베이스에도 저장
                    try {
                        photoRepository?.createKTXPhoto(
                            imagePath = savedUri.toString(),
                            title = "KTX 네컷 사진",
                            location = "KTX 역"
                        )
                        _successMessage.value = "이미지가 갤러리와 앱에 성공적으로 저장되었습니다!"
                    } catch (dbException: Exception) {
                        // 데이터베이스 저장 실패해도 갤러리 저장은 성공했으므로 부분 성공 메시지
                        _successMessage.value = "이미지는 갤러리에 저장되었지만 앱 저장에 실패했습니다."
                    }
                    clearError()
                } else {
                    _errorMessage.value = "이미지 저장에 실패했습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "이미지 저장 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * 인스타그램 공유
     */
    fun shareToInstagram() {
        val imageToShare = _composedImage.value
        if (imageToShare == null) {
            _errorMessage.value = "합성된 이미지가 없습니다. 먼저 이미지 합성을 해주세요."
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // 1. 합성된 이미지를 임시 파일로 저장
                val sharedImageFile = saveImageToCache(imageToShare)
                
                // 2. Instagram Story Intent 생성 및 실행
                context?.let { ctx ->
                    val intent = createInstagramStoryIntent(ctx, sharedImageFile)
                    // Intent 실행은 UI에서 처리해야 하므로 콜백으로 전달
                    _instagramShareIntent.value = intent
                } ?: run {
                    _errorMessage.value = "공유를 위한 컨텍스트를 찾을 수 없습니다."
                }
                
                clearError()
            } catch (e: Exception) {
                _errorMessage.value = "공유 실패: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    // 성공 메시지 상태
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * 에러 메시지 초기화
     */
    private fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * 햅틱 피드백 트리거
     */
    private fun triggerHapticFeedback() {
        context?.let { ctx ->
            try {
                val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            } catch (e: Exception) {
                println("햅틱 피드백 실패: ${e.message}")
            }
        }
    }
    
    /**
     * 성공 메시지 초기화
     */
    private fun clearSuccess() {
        _successMessage.value = null
    }
    
    /**
     * 프리미엄 프레임만 가져오기
     */
    fun getPremiumFrames(): List<Frame> {
        return frameRepository.getPremiumFrames()
    }
    
    /**
     * 특정 ID의 프레임 가져오기
     */
    fun getFrameById(id: Int): Frame? {
        return frameRepository.getFrameById(id)
    }
    
    /**
     * 권한 상태 업데이트
     */
    fun updatePermissionStatus(hasPermission: Boolean) {
        _hasImagePermission.value = hasPermission
    }
    
    /**
     * 선택된 프레임을 고품질 Bitmap으로 로드
     */
    private fun loadSelectedFrameBitmap(): Bitmap {
        return try {
            context?.let { ctx ->
                val selectedFrame = _selectedFrame.value
                if (selectedFrame != null) {
                    println("선택된 프레임 로딩: ${selectedFrame.name} (DrawableID: ${selectedFrame.drawableId})")
                    // 선택된 프레임 사용 (Vector Drawable + PNG 모두 지원)
                    imageComposer?.loadDrawableAsBitmap(
                        context = ctx,
                        drawableId = selectedFrame.drawableId,
                        width = ImageComposer.OUTPUT_WIDTH,
                        height = ImageComposer.OUTPUT_HEIGHT
                    ) ?: run {
                        println("프레임 로딩 실패, 기본 프레임 사용")
                        createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
                    }
                } else {
                    println("선택된 프레임 없음, 기본 프레임 사용")
                    // 기본 프레임 사용
                    imageComposer?.loadDrawableAsBitmap(
                        context = ctx,
                        drawableId = R.drawable.ktx_frame_signature,
                        width = ImageComposer.OUTPUT_WIDTH,
                        height = ImageComposer.OUTPUT_HEIGHT
                    ) ?: createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
                }
            } ?: run {
                println("Context 없음, 기본 프레임 생성")
                createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
            }
        } catch (e: Exception) {
            println("프레임 로딩 오류: ${e.message}")
            // 리소스 로드 실패 시 기본 프레임 생성
            createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
        }
    }
    
    /**
     * KTX 시그니처 프레임 리소스를 고품질 Bitmap으로 로드 (하위 호환성)
     */
    private fun loadKtxFrameBitmap(): Bitmap {
        return loadSelectedFrameBitmap()
    }
    
    /**
     * 임시 프레임 생성 함수 (fallback용)
     */
    private fun createDefaultFrameBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(0xFF1E3A8A.toInt()) // KTX 블루
        }
    }
    
    /**
     * Bitmap을 캐시 파일로 저장
     */
    private fun saveImageToCache(bitmap: Bitmap): File {
        val cacheDir = context?.cacheDir ?: throw IllegalStateException("Context not available")
        val imagesDir = File(cacheDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        
        val imageFile = File(imagesDir, "ktx_4cut_${System.currentTimeMillis()}.jpg")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        return imageFile
    }
    
    /**
     * 인스타그램 스토리 공유 Intent 생성
     */
    private fun createInstagramStoryIntent(context: Context, imageFile: File): Intent {
        val imageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        
        return Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(imageUri, "image/jpeg")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("interactive_asset_uri", imageUri)
        }
    }
    
    /**
     * ActivityResultLauncher에서 호출할 함수
     * 갤러리에서 선택된 이미지 URI들을 처리하여 Bitmap으로 변환
     */
    fun onImagesSelected(uris: List<Uri>) {
        println("onImagesSelected 호출됨: ${uris.size}개의 URI")
        if (uris.isEmpty()) {
            println("선택된 URI가 없습니다")
            return
        }
        
        // 최대 4개의 이미지만 선택하여 상태 업데이트
        _selectedImageUris.value = uris.take(4)
        println("선택된 URI 저장 완료: ${_selectedImageUris.value.size}개")
        
        // 선택된 URI들을 Bitmap으로 변환하여 그리드에 배치
        viewModelScope.launch {
            try {
                val imagePicker = imagePicker
                if (imagePicker == null) {
                    println("ImagePicker가 null입니다. Context를 다시 설정해주세요.")
                    _errorMessage.value = "이미지 처리기가 초기화되지 않았습니다. 앱을 다시 시작해주세요."
                    return@launch
                }
                
                println("ImagePicker로 이미지 처리 시작")
                val processedImages = imagePicker.processImagesForGrid(uris, 512)
                println("이미지 처리 완료: ${processedImages.size}개의 Bitmap 생성")
                
                // 변환된 Bitmap들을 그리드에 배치
                val currentPhotos = _photos.value.toMutableList()
                processedImages.forEachIndexed { index, bitmap ->
                    if (index < 4) {
                        // 기존 Bitmap 메모리 해제
                        currentPhotos[index]?.let { oldBitmap ->
                            if (!oldBitmap.isRecycled) {
                                oldBitmap.recycle()
                            }
                        }
                        currentPhotos[index] = bitmap
                        println("그리드 위치 ${index}에 이미지 배치 완료")
                    }
                }
                _photos.value = currentPhotos
                println("사진 그리드 업데이트 완료: ${_photos.value.map { it != null }}")
                
                _successMessage.value = "갤러리에서 ${uris.size}장의 사진을 선택했습니다"
                clearError()
            } catch (e: Exception) {
                println("이미지 처리 중 오류 발생: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "이미지 처리 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }
    
    /**
     * ViewModel 정리 시 Bitmap 메모리 해제
     */
    override fun onCleared() {
        super.onCleared()
        // 모든 Bitmap 메모리 해제
        _photos.value.forEach { bitmap ->
            bitmap?.let { 
                if (!it.isRecycled) {
                    it.recycle()
                }
            }
        }
        // 합성된 이미지도 메모리 해제
        _composedImage.value?.let { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }
}

