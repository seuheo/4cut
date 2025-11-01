package com.example.a4cut.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
// TODO: FFmpeg-kit 라이브러리 의존성 확인 후 추가 필요
// import com.arthenica.ffmpegkit.FFmpegKit
// import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 원본 사진 4장으로 슬라이드쇼 MP4 동영상을 생성하는 유틸리티 클래스
 * 잠신네컷의 동영상 기능을 모바일 환경에 맞게 구현
 */
object VideoSlideShowCreator {
    
    private const val TAG = "VideoSlideShowCreator"
    private const val VIDEO_WIDTH = 1080 // 인스타그램 스토리 최적화
    private const val VIDEO_HEIGHT = 1920
    private const val PHOTO_DURATION_SECONDS = 2 // 각 사진 표시 시간 (초)
    private const val VIDEO_CODEC = "libx264" // H.264 코덱
    private const val VIDEO_BITRATE = "8M" // 비트레이트
    
    /**
     * 원본 사진 4장으로 슬라이드쇼 MP4 동영상 생성 (FFmpeg 기반)
     * @param photos 원본 사진 Bitmap 목록 (4장)
     * @param context Context
     * @return 생성된 MP4 파일 경로 (실패 시 null)
     */
    suspend fun createSlideShowVideo(
        photos: List<Bitmap?>,
        context: Context
    ): String? = withContext(Dispatchers.IO) {
        val validPhotos = photos.filterNotNull()
        if (validPhotos.isEmpty()) {
            Log.e(TAG, "사진이 없어 동영상을 생성할 수 없습니다")
            return@withContext null
        }
        
        val tempDir = File(context.cacheDir, "video_temp_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        
        val outputFile = File(
            context.getExternalFilesDir(null)?.let { 
                File(it, "videos")
            } ?: context.cacheDir,
            "slideshow_${System.currentTimeMillis()}.mp4"
        )
        
        // 비디오 디렉토리 생성
        outputFile.parentFile?.mkdirs()
        
        val tempImageFiles = mutableListOf<File>()
        
        try {
            Log.d(TAG, "슬라이드쇼 동영상 생성 시작: ${validPhotos.size}장의 사진")
            
            // 1. 각 Bitmap을 임시 PNG 파일로 저장
            validPhotos.forEachIndexed { index, bitmap ->
                val resizedBitmap = resizeBitmap(bitmap, VIDEO_WIDTH, VIDEO_HEIGHT)
                val tempImageFile = File(tempDir, "frame_${String.format("%02d", index + 1)}.png")
                
                FileOutputStream(tempImageFile).use { out ->
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                
                tempImageFiles.add(tempImageFile)
                Log.d(TAG, "임시 이미지 파일 생성: ${tempImageFile.absolutePath}")
                
                // 리사이즈된 Bitmap은 원본과 다르면 해제
                if (resizedBitmap != bitmap) {
                    resizedBitmap.recycle()
                }
            }
            
            // 2. FFmpeg 명령어 구성: 각 이미지를 2초씩 표시하고 연결
            // 예시: ffmpeg -loop 1 -t 2 -i frame_01.png -loop 1 -t 2 -i frame_02.png ... 
            //       -filter_complex "[0:v][1:v][2:v][3:v]concat=n=4:v=1:a=0" -c:v libx264 -b:v 8M -y output.mp4
            val ffmpegCommand = buildFFmpegCommand(tempImageFiles, outputFile)
            
            Log.d(TAG, "FFmpeg 명령어 구성 완료: $ffmpegCommand")
            
            // TODO: FFmpeg-kit 라이브러리 의존성 추가 후 아래 주석 해제
            // 3. FFmpeg 실행
            /*
            val session = FFmpegKit.execute(ffmpegCommand)
            val returnCode = session.returnCode
            
            if (ReturnCode.isSuccess(returnCode)) {
                Log.d(TAG, "동영상 생성 성공: ${outputFile.absolutePath}")
                outputFile.absolutePath
            } else {
                val output = session.allLogsAsString
                Log.e(TAG, "FFmpeg 실행 실패: $output")
                outputFile.delete()
                null
            }
            */
            
            // 임시: FFmpeg 라이브러리 없이 파일 경로만 반환
            Log.w(TAG, "FFmpeg 라이브러리 미설치로 인해 실제 동영상 생성 생략")
            Log.w(TAG, "FFmpeg-kit 의존성 추가 후 주석 해제 필요")
            // outputFile.createNewFile() // 임시 파일 생성
            null // 현재는 null 반환 (실제 동영상 생성 불가)
            
        } catch (e: Exception) {
            Log.e(TAG, "동영상 생성 실패", e)
            e.printStackTrace()
            outputFile.delete()
            null
        } finally {
            // 임시 파일 정리
            tempImageFiles.forEach { it.delete() }
            tempDir.deleteRecursively()
            Log.d(TAG, "임시 파일 정리 완료")
        }
    }
    
    /**
     * FFmpeg 명령어 생성
     * 각 이미지를 2초씩 반복하여 표시하고 연결
     */
    private fun buildFFmpegCommand(imageFiles: List<File>, outputFile: File): String {
        val inputs = imageFiles.joinToString(" ") { file ->
            "-loop 1 -t $PHOTO_DURATION_SECONDS -i \"${file.absolutePath}\""
        }
        
        // concat 필터: 모든 비디오를 순차적으로 연결
        val filterInputs = imageFiles.indices.joinToString("") { "[$it:v]" }
        val filterComplex = "$filterInputs concat=n=${imageFiles.size}:v=1:a=0 [v]"
        
        val command = "-y $inputs " +
                "-filter_complex \"$filterComplex\" " +
                "-map \"[v]\" " +
                "-c:v $VIDEO_CODEC " +
                "-b:v $VIDEO_BITRATE " +
                "-preset medium " +
                "-pix_fmt yuv420p " +
                "-s ${VIDEO_WIDTH}x${VIDEO_HEIGHT} " +
                "\"${outputFile.absolutePath}\""
        
        return command
    }
    
    /**
     * Bitmap을 지정된 크기로 리사이즈 (비율 유지)
     */
    private fun resizeBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scale = minOf(
            targetWidth.toFloat() / width,
            targetHeight.toFloat() / height
        )
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
