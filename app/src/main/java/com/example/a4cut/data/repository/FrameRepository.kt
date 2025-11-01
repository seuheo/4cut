package com.example.a4cut.data.repository

import android.content.Context
import com.example.a4cut.R
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.model.FrameFormat
import com.example.a4cut.data.model.Slot
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream

/**
 * 프레임 데이터를 관리하는 Repository
 * 현재는 로컬 더미 데이터를 제공하지만, 추후 서버나 데이터베이스 연동 가능
 */
class FrameRepository {
    
    private val _frames = MutableStateFlow<List<Frame>>(emptyList())
    val frames: StateFlow<List<Frame>> = _frames.asStateFlow()
    
    init {
        // 초기 프레임 데이터 로드
        loadInitialFrames()
    }
    
    /**
     * 초기 프레임 데이터 로드
     */
    private fun loadInitialFrames() {
        val initialFrames = listOf(
            Frame(
                id = "single_frame",
                name = "기본 프레임",
                date = "25.01.13",
                station = "KTX",
                title = "기본 프레임",
                isPremium = false,
                drawableId = R.drawable.single_frame,
                category = "basic",
                format = FrameFormat.STANDARD
            ),
            Frame(
                id = "image_e15024",
                name = "인생 네컷 프레임",
                date = "25.01.13",
                station = "KTX",
                title = "인생 네컷 프레임",
                isPremium = false,
                drawableId = R.drawable.image_e15024,
                category = "ktx",
                format = FrameFormat.STANDARD
            ),
            Frame(
                id = "long_form_white",
                name = "Long Form White",
                date = "25.01.13",
                station = "KTX",
                title = "Long Form White",
                isPremium = false,
                drawableId = R.drawable.long_form_white,
                category = "long form",
                format = FrameFormat.LONG_FORM
            ),
            Frame(
                id = "long_form_black",
                name = "Long Form Black",
                date = "25.01.13",
                station = "KTX",
                title = "Long Form Black",
                isPremium = false,
                drawableId = R.drawable.long_form_black,
                category = "long form",
                format = FrameFormat.LONG_FORM
            )
        )
        
        _frames.value = initialFrames
    }
    
    /**
     * 프레임 목록 가져오기
     */
    fun getFrames(): List<Frame> {
        return _frames.value
    }
    
    /**
     * 특정 ID의 프레임 가져오기
     */
    fun getFrameById(id: String): Frame? {
        return _frames.value.find { it.id == id }
    }
    
    /**
     * 카테고리별 프레임 목록 가져오기
     */
    fun getFramesByCategory(category: String): List<Frame> {
        return _frames.value.filter { it.category == category }
    }
    
    /**
     * 포맷별 프레임 목록 가져오기
     */
    fun getFramesByFormat(format: FrameFormat): List<Frame> {
        return _frames.value.filter { it.format == format }
    }
    
    /**
     * 모든 카테고리 목록 가져오기
     */
    fun getCategories(): List<String> {
        return _frames.value.mapNotNull { it.category }.distinct()
    }
    
    /**
     * 모든 포맷 목록 가져오기
     */
    fun getFormats(): List<FrameFormat> {
        return _frames.value.map { it.format }.distinct()
    }
    
    /**
     * 프리미엄 프레임만 가져오기
     */
    fun getPremiumFrames(): List<Frame> {
        return _frames.value.filter { it.isPremium }
    }
    
    /**
     * 프레임 추가
     */
    fun addFrame(frame: Frame) {
        val currentFrames = _frames.value.toMutableList()
        currentFrames.add(frame)
        _frames.value = currentFrames
    }
    
    /**
     * 프레임 업데이트
     */
    fun updateFrame(frame: Frame) {
        val currentFrames = _frames.value.toMutableList()
        val index = currentFrames.indexOfFirst { it.id == frame.id }
        if (index != -1) {
            currentFrames[index] = frame
            _frames.value = currentFrames
        }
    }
    
    /**
     * assets/frames.json 파일에서 슬롯 정보를 읽어와 기존 Frame 객체에 병합
     * @param context Context (assets 파일 읽기용)
     */
    fun loadSlotsFromJson(context: Context) {
        try {
            val inputStream: InputStream = context.assets.open("frames.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // JSON 구조: { "frames": [{ "id": "...", "slots": [...] }, ...] }
            val gson = Gson()
            val framesJsonType = object : TypeToken<Map<String, List<FrameJson>>>() {}.type
            val jsonMap = gson.fromJson<Map<String, List<FrameJson>>>(jsonString, framesJsonType)
            
            val framesList = jsonMap["frames"] ?: emptyList()
            val slotsMap = framesList.associate { it.id to it.slots }
            
            // 기존 Frame 목록을 순회하면서 slots 정보를 병합
            val updatedFrames = _frames.value.map { frame ->
                slotsMap[frame.id]?.let { slots ->
                    frame.copy(slots = slots)
                } ?: frame
            }
            
            _frames.value = updatedFrames
            
            println("FrameRepository: JSON에서 ${slotsMap.size}개의 프레임 슬롯 정보 로드 완료")
        } catch (e: Exception) {
            println("FrameRepository: JSON 로드 실패 - ${e.message}")
            e.printStackTrace()
            // JSON 로드 실패 시 기존 프레임 목록은 그대로 유지
        }
    }
    
    /**
     * JSON 파싱을 위한 내부 데이터 클래스
     */
    private data class FrameJson(
        val id: String,
        val slots: List<Slot>
    )
}

