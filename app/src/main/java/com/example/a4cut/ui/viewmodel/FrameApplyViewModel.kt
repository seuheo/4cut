package com.example.a4cut.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.repository.FrameRepository
import com.example.a4cut.data.repository.PhotoRepository
import com.example.a4cut.data.service.LocationTaggingService
import com.example.a4cut.ui.utils.ImageComposer
import com.example.a4cut.data.model.KtxStationData
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 프레임 적용 화면의 ViewModel
 * 사진 데이터 로드, 프레임 선택, 미리보기 관리
 */
class FrameApplyViewModel(
    private val photoRepository: PhotoRepository? = null,
    private val frameRepository: FrameRepository? = null,
    private val context: Context? = null,
    private val frameViewModel: com.example.a4cut.ui.viewmodel.FrameViewModel? = null
) : ViewModel() {

    private val imageComposer = context?.let { ImageComposer(it) }
    private val imageLoader = context?.let { 
        // AppApplication의 최적화된 ImageLoader 사용
        com.example.a4cut.AppApplication.imageLoader
    }
    private val locationTaggingService = context?.let { LocationTaggingService(it) }

    private val _uiState = MutableStateFlow(FrameApplyUiState())
    val uiState: StateFlow<FrameApplyUiState> = _uiState.asStateFlow()
    
    // 현재 선택된 프레임의 슬롯 정보 (frames.json에서 로드된 슬롯)
    val frameSlots: StateFlow<List<com.example.a4cut.data.model.Slot>> = 
        _uiState.map { it.selectedFrame?.slots ?: emptyList() }.stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // KTX 역 관련 상태
    private val _ktxLines = MutableStateFlow<List<String>>(emptyList())
    val ktxLines: StateFlow<List<String>> = _ktxLines.asStateFlow()
    
    private val _stationsByLine = MutableStateFlow<List<com.example.a4cut.data.model.KtxStation>>(emptyList())
    val stationsByLine: StateFlow<List<com.example.a4cut.data.model.KtxStation>> = _stationsByLine.asStateFlow()
    
    private val _selectedStationName = MutableStateFlow<String?>(null)
    
    init {
        loadKtxLines()
    }
    
    private fun loadKtxLines() {
        _ktxLines.value = listOf("Gyeongbu", "Honam")
        // 첫 노선 자동으로 선택
        _ktxLines.value.firstOrNull()?.let {
            loadStationsForLine(it)
        }
    }
    
    fun loadStationsForLine(line: String) {
        // 경부선과 호남선 역만 필터링하여 로드 (KtxStationData.availableStations 사용)
        val filteredStations = when (line) {
            "Gyeongbu" -> KtxStationData.gyeongbuLineStations
            "Honam" -> KtxStationData.honamLineStations
            else -> emptyList()
        }
        _stationsByLine.value = filteredStations
        Log.d("FrameApplyVM", "노선 [$line]의 역 로드: ${_stationsByLine.value.map { it.stationName }}")
    }
    
    fun updateSelectedStation(stationName: String?) {
        _selectedStationName.value = stationName
        Log.d("FrameApplyVM", "선택된 역 업데이트: $stationName")
    }

    /**
     * 사진 ID로 사진 데이터 로드
     */
    fun loadPhoto(photoId: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // 실제 PhotoRepository에서 사진 데이터 가져오기
                val photo = photoRepository?.getPhotoById(photoId)
                
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
                val frames = frameRepository?.getFrames() ?: emptyList()
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
        
        if (photo != null && selectedFrame != null && context != null && imageComposer != null) {
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
                    
                    // 프레임 미리보기 생성 완료 (DB 저장은 ResultScreen에서 처리)
                    // savePhotoMetadataToDatabase(photo, selectedFrame, previewBitmap) // 중복 저장 방지
                    
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
            if (imagePath.isBlank() || imagePath == "dummy_path" || context == null || imageLoader == null) {
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
     * ViewModel 정리 시 Bitmap 메모리 해제
     */
    override fun onCleared() {
        super.onCleared()
        // ViewModel이 정리될 때만 안전하게 메모리 해제
        // UI에서 사용 중인 bitmap은 가비지 컬렉터가 처리하도록 함
        _uiState.value.previewBitmap?.let { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * 사진 메타데이터를 DB에 자동 저장 (미리보기 생성 시)
     */
    private fun savePhotoMetadataToDatabase(photo: PhotoEntity, selectedFrame: Frame, previewBitmap: Bitmap?) {
        viewModelScope.launch {
            try {
                // 선택된 역 정보 가져오기
                val stationName = _selectedStationName.value
                val station = stationName?.let { KtxStationData.findStationByName(it) }
                
                // 임시 파일로 저장하여 URI 얻기 (DB 저장을 위해)
                val tempUri = saveBitmapToTempStorage(previewBitmap, "temp_${System.currentTimeMillis()}.jpg")
                
                val newPhoto = photo.copy(
                    id = 0, // 새로운 ID 생성
                    imagePath = tempUri?.toString() ?: "",
                    title = "${photo.title} (${selectedFrame.name} 프레임)",
                    frameType = selectedFrame.name,
                    location = station?.stationName ?: photo.location,
                    latitude = station?.latitude ?: photo.latitude,
                    longitude = station?.longitude ?: photo.longitude,
                    station = stationName,
                    createdAt = System.currentTimeMillis()
                )
                
                Log.d("FrameApplyVM", "DB에 저장할 PhotoEntity: $newPhoto")
                
                if (photoRepository != null) {
                    val photoId = photoRepository.insertPhoto(newPhoto)
                    Log.d("FrameApplyVM", "DB 저장 성공! Photo ID: $photoId")
                } else {
                    Log.e("FrameApplyVM", "PhotoRepository가 null입니다!")
                }
                
            } catch (e: Exception) {
                Log.e("FrameApplyVM", "DB 저장 실패", e)
            }
        }
    }
    
    /**
     * 비트맵을 임시 저장소에 저장
     */
    private suspend fun saveBitmapToTempStorage(bitmap: Bitmap?, filename: String): Uri? {
        if (bitmap == null || context == null) return null
        
        return try {
            val tempFile = java.io.File(context.cacheDir, filename)
            val outputStream = java.io.FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.close()
            Uri.fromFile(tempFile)
        } catch (e: Exception) {
            Log.e("FrameApplyVM", "임시 저장 실패", e)
            null
        }
    }
    
    /**
     * 갤러리에 최종 결과물 저장
     */
    fun saveToGallery() {
        val photo = _uiState.value.photo
        val selectedFrame = _uiState.value.selectedFrame
        val previewBitmap = _uiState.value.previewBitmap
        
        if (photo != null && selectedFrame != null && previewBitmap != null && context != null && imageComposer != null) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true) }
                    
                    // 사진 Bitmap 로드 (고해상도)
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
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                successMessage = "사진이 갤러리에 저장되었습니다."
                            )
                        }
                        Log.d("FrameApplyViewModel", "사진 저장 성공")
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                errorMessage = "사진 저장에 실패했습니다."
                            )
                        }
                    }
                    
                    // 메모리 정리 (중간 단계 Bitmap만 재활용, 최종 결과는 UI에서 사용 중이므로 재활용하지 않음)
                    imageComposer.recycleBitmap(photoBitmap)
                    imageComposer.recycleBitmap(frameBitmap)
                    // resultBitmap은 UI에서 사용 중이므로 재활용하지 않음
                    
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

    /**
     * KTX 역 이름을 좌표로 변환
     */
    private fun getStationCoordinates(stationName: String?): Pair<Double, Double>? {
        if (stationName == null) {
            Log.d("FrameApplyViewModel", "역 이름이 null입니다.")
            return null
        }
        
        // 모든 KTX 역 목록에서 해당 역 찾기
        val allStations = KtxStationData.allStations
        Log.d("FrameApplyViewModel", "전체 KTX 역 개수: ${allStations.size}")
        Log.d("FrameApplyViewModel", "찾는 역 이름: $stationName")
        Log.d("FrameApplyViewModel", "사용 가능한 역들: ${allStations.map { it.stationName }}")
        
        val station = allStations.find { it.stationName == stationName }
        
        return if (station != null) {
            Log.d("FrameApplyViewModel", "역 찾음: ${station.stationName} (${station.latitude}, ${station.longitude})")
            Pair(station.latitude, station.longitude)
        } else {
            Log.e("FrameApplyViewModel", "역을 찾을 수 없음: $stationName")
            Log.d("FrameApplyViewModel", "사용 가능한 역들: ${allStations.map { it.stationName }}")
            null
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
