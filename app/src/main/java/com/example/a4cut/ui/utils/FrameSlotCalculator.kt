package com.example.a4cut.ui.utils

import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.model.Slot

/**
 * 프레임 슬롯의 실제 픽셀 크기를 계산하는 유틸리티
 * frames.json에 정의된 정규화된 좌표를 실제 픽셀 크기로 변환
 */
object FrameSlotCalculator {
    /**
     * 프레임의 출력 크기를 가져옵니다
     * @param frameId 프레임 ID
     * @return Pair<width, height> 픽셀 크기
     */
    fun getOutputSize(frameId: String?): Pair<Int, Int> {
        return when (frameId) {
            "long_form_white", "long_form_black" -> {
                ImageComposer.getLongFormOutputSize()
            }
            else -> {
                // 일반 프레임: 1080x1920 (인스타그램 스토리 최적화)
                Pair(ImageComposer.OUTPUT_WIDTH, ImageComposer.OUTPUT_HEIGHT)
            }
        }
    }
    
    /**
     * 슬롯의 실제 픽셀 크기를 계산합니다
     * @param slot 슬롯 정보 (정규화된 좌표)
     * @param frameId 프레임 ID (출력 크기 결정용)
     * @return Pair<width, height> 실제 픽셀 크기
     */
    fun calculateSlotPixelSize(slot: Slot, frameId: String?): Pair<Int, Int> {
        val (outputWidth, outputHeight) = getOutputSize(frameId)
        val slotWidth = (slot.width * outputWidth).toInt()
        val slotHeight = (slot.height * outputHeight).toInt()
        return Pair(slotWidth, slotHeight)
    }
    
    /**
     * 슬롯의 비율 문자열을 생성합니다 (예: "497:336")
     * @param slot 슬롯 정보
     * @param frameId 프레임 ID
     * @return "width:height" 형식의 문자열
     */
    fun getSlotRatioString(slot: Slot, frameId: String?): String {
        val (width, height) = calculateSlotPixelSize(slot, frameId)
        val ratioString = "$width:$height"
        println("FrameSlotCalculator: 슬롯 비율 계산")
        println("  프레임 ID: $frameId")
        println("  슬롯 좌표 (정규화): x=${slot.x}, y=${slot.y}, width=${slot.width}, height=${slot.height}")
        println("  계산된 픽셀 크기: ${width}x${height}")
        println("  비율 문자열: $ratioString")
        return ratioString
    }
}

