package com.example.a4cut.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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

/**
 * 프레임 화면의 ViewModel
 * 사진 선택, 프레임 적용, 이미지 합성 등 핵심 로직을 담당
 * Phase 3: Bitmap 기반 이미지 처리 및 권한 관리 기능 추가
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
     * Context 설정 (권한 및 이미지 처리에 필요)
     */
    fun setContext(context: Context) {
        this.context = context
        imagePicker = ImagePicker(context)
        permissionHelper = PermissionHelper(context)
        imageComposer = ImageComposer(context) // ImageComposer 초기화
        
        // PhotoRepository 초기화
        val database = AppDatabase.getDatabase(context)
        photoRepository = PhotoRepository(database.photoDao())
        
        checkImagePermission()
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
                frameRepository.frames.collect { frameList ->
                    _frames.value = frameList
                }
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
        _selectedFrame.value = frame
        clearError()
    }
    
    /**
     * 사진 선택 (Bitmap 기반)
     */
    fun selectPhoto(index: Int, bitmap: Bitmap?) {
        if (index in 0..3) {
            val currentPhotos = _photos.value.toMutableList()
            currentPhotos[index] = bitmap
            _photos.value = currentPhotos
            clearError()
        } else {
            _errorMessage.value = "잘못된 사진 인덱스입니다: $index"
        }
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
     * 사진 선택 토글 (있으면 제거, 없으면 추가 준비)
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
            }
            // 사진이 없으면 추가 준비 (openImagePicker 호출 필요)
            
            _photos.value = currentPhotos
            clearError()
        } else {
            _errorMessage.value = "잘못된 사진 인덱스입니다: $index"
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
        if (_selectedFrame.value == null) {
            _errorMessage.value = "프레임을 선택해주세요"
            return
        }
        
        val hasPhotos = _photos.value.any { it != null }
        if (!hasPhotos) {
            _errorMessage.value = "최소 한 장의 사진을 선택해주세요"
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
                    val result = composer.composeImage(
                        photos = _photos.value,
                        frameBitmap = frameBitmap
                    )
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
                            location = "KTX 역",
                            tags = "ktx,4cut,여행"
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
     * KTX 시그니처 프레임 리소스를 고품질 Bitmap으로 로드
     */
    private fun loadKtxFrameBitmap(): Bitmap {
        return try {
            context?.let { ctx ->
                // ImageComposer의 고품질 벡터 드로어블 로딩 함수 사용
                imageComposer?.loadVectorDrawableAsBitmap(
                    context = ctx,
                    drawableId = R.drawable.ktx_frame_signature,
                    width = ImageComposer.OUTPUT_WIDTH,
                    height = ImageComposer.OUTPUT_HEIGHT
                ) ?: createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
            } ?: createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
        } catch (e: Exception) {
            // 리소스 로드 실패 시 기본 프레임 생성
            createDefaultFrameBitmap(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
        }
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

