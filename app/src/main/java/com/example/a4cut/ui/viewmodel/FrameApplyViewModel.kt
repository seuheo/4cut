package com.example.a4cut.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.repository.FrameRepository
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.ui.utils.ImageComposer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 프레임 적용 화면의 ViewModel
 * 사진 데이터 로드, 프레임 선택, 미리보기 관리
 */
class FrameApplyViewModel(
    private val photoRepository: PhotoRepository,
    private val frameRepository: FrameRepository,
    private val context: Context
) : ViewModel() {

    private val imageComposer = ImageComposer(context)
    private val imageLoader = ImageLoader(context)

    private val _uiState = MutableStateFlow(FrameApplyUiState())
    val uiState: StateFlow<FrameApplyUiState> = _uiState.asStateFlow()

    /**
     * 사진 ID로 사진 데이터 로드
     */
    fun loadPhoto(photoId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // 실제 PhotoRepository에서 사진 데이터 가져오기
                val photo = photoRepository.getPhotoById(photoId)
                
                if (photo != null) {
                    _uiState.update { it.copy(photo = photo) }
                    // 프레임 목록도 함께 로드
                    loadFrames()
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "해당 사진을 찾을 수 없습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "사진을 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 사용 가능한 프레임 목록 로드
     */
    private fun loadFrames() {
        viewModelScope.launch {
            try {
                val frames = frameRepository.getFrames()
                _uiState.update { it.copy(frames = frames) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "프레임 목록을 불러오는데 실패했습니다: ${e.message}") }
            }
        }
    }

    /**
     * 프레임 선택
     */
    fun selectFrame(frame: Frame) {
        _uiState.update { it.copy(selectedFrame = frame) }
        // 프레임 선택 시 미리보기 생성
        generatePreview()
    }

    /**
     * 프레임 미리보기 생성
     */
    private fun generatePreview() {
        val photo = _uiState.value.photo
        val selectedFrame = _uiState.value.selectedFrame
        
        if (photo != null && selectedFrame != null) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    // 사진 Bitmap 로드
                    val photoBitmap = loadBitmapFromPath(photo.imagePath)
                    if (photoBitmap == null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "사진을 불러올 수 없습니다."
                            )
                        }
                        return@launch
                    }
                    
                    // 프레임 Bitmap 로드
                    val frameBitmap = imageComposer.loadVectorDrawableAsBitmap(
                        context,
                        selectedFrame.drawableId,
                        ImageComposer.OUTPUT_WIDTH / 2, // 미리보기용 저해상도
                        ImageComposer.OUTPUT_HEIGHT / 2
                    )
                    
                    // 프레임 적용하여 미리보기 생성
                    val previewBitmap = imageComposer.applyFrameToPhoto(
                        photoBitmap,
                        frameBitmap,
                        isPreview = true
                    )
                    
                    _uiState.update { 
                        it.copy(
                            previewBitmap = previewBitmap,
                            isLoading = false
                        )
                    }
                    
                    // 메모리 정리
                    imageComposer.recycleBitmap(photoBitmap)
                    imageComposer.recycleBitmap(frameBitmap)
                    
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "미리보기 생성에 실패했습니다: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    /**
     * 이미지 경로에서 Bitmap 로드
     * Coil을 사용한 실제 이미지 로딩 구현
     */
    private suspend fun loadBitmapFromPath(imagePath: String): Bitmap? {
        return try {
            // 이미지 경로가 유효한지 확인
            if (imagePath.isBlank() || imagePath == "dummy_path") {
                return null
            }

            // Coil을 사용한 이미지 로딩
            val request = ImageRequest.Builder(context)
                .data(imagePath)
                .build()

            val result = imageLoader.execute(request)
            
            when (result) {
                is coil.request.SuccessResult -> {
                    val drawable = result.drawable
                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                        val bitmap = drawable.bitmap
                        if (bitmap != null && !bitmap.isRecycled) {
                            // 메모리 최적화: 필요한 경우 리사이징
                            val maxSize = 1024 // 최대 1024px로 제한
                            if (bitmap.width > maxSize || bitmap.height > maxSize) {
                                val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
                                val newWidth = (bitmap.width * scale).toInt()
                                val newHeight = (bitmap.height * scale).toInt()
                                
                                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
                                // 원본 비트맵은 Coil이 관리하므로 여기서는 해제하지 않음
                                return resizedBitmap
                            }
                            return bitmap
                        } else {
                            null
                        }
                    } else {
                        // BitmapDrawable이 아닌 경우 null 반환
                        null
                    }
                }
                is coil.request.ErrorResult -> {
                    // 에러 로깅 (디버그용)
                    android.util.Log.e("FrameApplyViewModel", "이미지 로딩 실패: ${result.throwable.message}")
                    null
                }
            }
        } catch (e: Exception) {
            // 예외 로깅 (디버그용)
            android.util.Log.e("FrameApplyViewModel", "이미지 로딩 중 예외 발생: ${e.message}")
            null
        }
    }

    /**
     * 선택된 프레임 해제
     */
    fun clearSelectedFrame() {
        _uiState.update { it.copy(selectedFrame = null) }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 성공 메시지 초기화
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    /**
     * 프레임 적용 결과물 저장
     */
    fun saveFrameAppliedPhoto() {
        val photo = _uiState.value.photo
        val selectedFrame = _uiState.value.selectedFrame
        
        if (photo != null && selectedFrame != null) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    // 사진 Bitmap 로드
                    val photoBitmap = loadBitmapFromPath(photo.imagePath)
                    if (photoBitmap == null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "사진을 불러올 수 없습니다."
                            )
                        }
                        return@launch
                    }
                    
                    // 프레임 Bitmap 로드 (저장용 고해상도)
                    val frameBitmap = imageComposer.loadVectorDrawableAsBitmap(
                        context,
                        selectedFrame.drawableId,
                        ImageComposer.OUTPUT_WIDTH,
                        ImageComposer.OUTPUT_HEIGHT
                    )
                    
                    // 프레임 적용하여 최종 결과물 생성
                    val resultBitmap = imageComposer.applyFrameToPhoto(
                        photoBitmap,
                        frameBitmap,
                        isPreview = false
                    )
                    
                    // 갤러리에 저장
                    val fileName = "KTX_Frame_${System.currentTimeMillis()}.jpg"
                    val savedUri = imageComposer.saveBitmapToGallery(resultBitmap, fileName)
                    
                    if (savedUri != null) {
                        // 새로운 PhotoEntity 생성하여 데이터베이스에 저장
                        val newPhoto = photo.copy(
                            id = 0, // 새로운 ID 생성
                            imagePath = savedUri.toString(),
                            title = "${photo.title} (${selectedFrame.name} 프레임)",
                            frameType = selectedFrame.name,
                            createdAt = System.currentTimeMillis()
                        )
                        
                        // PhotoRepository를 통해 새로운 사진 저장
                        photoRepository.insertPhoto(newPhoto)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "프레임이 적용된 사진이 저장되었습니다."
                            )
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "사진 저장에 실패했습니다."
                            )
                        }
                    }
                    
                    // 메모리 정리
                    imageComposer.recycleBitmap(photoBitmap)
                    imageComposer.recycleBitmap(frameBitmap)
                    imageComposer.recycleBitmap(resultBitmap)
                    
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "저장 중 오류가 발생했습니다: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}

/**
 * 프레임 적용 화면의 UI 상태
 */
data class FrameApplyUiState(
    val photo: PhotoEntity? = null,
    val frames: List<Frame> = emptyList(),
    val selectedFrame: Frame? = null,
    val previewBitmap: Bitmap? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
