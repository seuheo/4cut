package com.example.a4cut.util

import android.content.Context
import android.net.Uri
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.data.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * 테스트 데이터 생성 유틸리티
 * Phase 4.3.2: 개발 속도와 테스트 효율을 높이기 위한 테스트 데이터 생성 기능
 * 디버그 모드에서 버튼 클릭 한 번으로 여러 개의 테스트용 포토로그 데이터를 Room DB에 생성
 */
class TestDataGenerator(
    private val photoRepository: PhotoRepository,
    private val context: Context
) {
    
    companion object {
        private const val TEST_PHOTO_COUNT = 15 // 테스트용 포토로그 데이터 개수
        private val TEST_STATIONS = listOf(
            "서울역", "부산역", "대구역", "전주역", "강릉역", 
            "여수엑스포역", "인천공항역", "수원역", "천안아산역", "동탄역"
        )
        private val TEST_SEASONS = listOf("봄", "여름", "가을", "겨울")
        private val TEST_MOODS = listOf("즐거운", "평온한", "신나는", "로맨틱한", "힐링되는")
        private val TEST_WEATHER = listOf("맑음", "흐림", "비", "눈", "안개")
        private val TEST_TITLES = listOf(
            "기차 여행의 시작", "새로운 도시 탐험", "친구들과의 추억", "가족 여행", "혼자만의 여행",
            "봄날의 기차 여행", "여름 휴가", "가을 단풍 여행", "겨울 설경 여행", "해변 여행",
            "산악 여행", "도시 탐방", "문화재 관람", "맛집 탐방", "자연 속 힐링"
        )
        private val TEST_DESCRIPTIONS = listOf(
            "기차를 타고 떠나는 새로운 모험의 시작입니다.",
            "새로운 도시의 풍경과 문화를 경험했습니다.",
            "친구들과 함께한 소중한 추억의 여행입니다.",
            "가족과 함께한 따뜻한 여행의 기록입니다.",
            "혼자만의 여유로운 시간을 보낸 여행입니다.",
            "봄꽃이 만발한 계절의 아름다운 여행입니다.",
            "시원한 바다와 함께한 여름 휴가입니다.",
            "단풍으로 물든 가을의 낭만적인 여행입니다.",
            "하얀 설경이 아름다운 겨울 여행입니다.",
            "푸른 바다와 하얀 모래사장의 해변 여행입니다.",
            "푸른 산과 맑은 공기의 산악 여행입니다.",
            "도시의 번화가와 문화를 체험한 여행입니다.",
            "역사와 전통이 살아있는 문화재 관람입니다.",
            "현지의 맛있는 음식을 탐방한 여행입니다.",
            "자연 속에서 마음을 정화하는 힐링 여행입니다."
        )
    }
    
    /**
     * 테스트용 포토로그 데이터를 일괄 생성하여 Room DB에 저장
     * @return 생성된 데이터 개수
     */
    suspend fun generateTestPhotologs(): Int = withContext(Dispatchers.IO) {
        try {
            val testPhotos = mutableListOf<PhotoEntity>()
            
            repeat(TEST_PHOTO_COUNT) { index ->
                val photo = createTestPhotoEntity(index)
                testPhotos.add(photo)
            }
            
            // Room DB에 일괄 저장
            photoRepository.insertPhotos(testPhotos)
            
            return@withContext testPhotos.size
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }
    
    /**
     * 개별 테스트 포토로그 데이터 생성
     */
    private fun createTestPhotoEntity(index: Int): PhotoEntity {
        val station = TEST_STATIONS[index % TEST_STATIONS.size]
        val season = TEST_SEASONS[index % TEST_SEASONS.size]
        val mood = TEST_MOODS[index % TEST_MOODS.size]
        val weather = TEST_WEATHER[index % TEST_WEATHER.size]
        val title = TEST_TITLES[index % TEST_TITLES.size]
        val description = TEST_DESCRIPTIONS[index % TEST_DESCRIPTIONS.size]
        
        // 테스트용 더미 이미지 URI 생성 (실제로는 존재하지 않는 URI)
        val dummyImageUris = List(4) { "dummy_image_${index}_${it}.jpg" }
        
        return PhotoEntity(
            id = 0, // Room이 자동으로 ID 할당
            imagePath = "dummy_image_${index}_main.jpg", // 필수 필드
            title = "$title ($station)",
            description = "$description - $season, $mood, $weather, $station",
            imageUris = dummyImageUris,
            location = station,
            weather = weather,
            mood = mood,
            season = season,
            createdAt = System.currentTimeMillis() - (index * 24 * 60 * 60 * 1000L) // 각각 하루씩 이전
        )
    }
    
    /**
     * 특정 개수의 테스트 데이터 생성
     * @param count 생성할 데이터 개수
     * @return 생성된 데이터 개수
     */
    suspend fun generateTestPhotologs(count: Int): Int = withContext(Dispatchers.IO) {
        try {
            val testPhotos = mutableListOf<PhotoEntity>()
            
            repeat(count) { index ->
                val photo = createTestPhotoEntity(index)
                testPhotos.add(photo)
            }
            
            // Room DB에 일괄 저장
            photoRepository.insertPhotos(testPhotos)
            
            return@withContext testPhotos.size
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }
    
    /**
     * 테스트 데이터 초기화 (모든 테스트 데이터 삭제)
     * @return 삭제된 데이터 개수
     */
    suspend fun clearTestData(): Int = withContext(Dispatchers.IO) {
        try {
            // 테스트 데이터만 삭제하는 로직 구현 필요
            // 현재는 모든 데이터를 삭제하는 것으로 구현
            val allPhotos = photoRepository.getAllPhotos()
            var testPhotoCount = 0
            
            allPhotos.collect { photos ->
                testPhotoCount = photos.count { it.title.contains("(") && it.title.contains(")") }
                
                // 테스트 데이터 삭제 (제목에 괄호가 포함된 데이터)
                val testPhotos = photos.filter { it.title.contains("(") && it.title.contains(")") }
                testPhotos.forEach { photo ->
                    photoRepository.deletePhoto(photo)
                }
            }
            
            return@withContext testPhotoCount
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }
    
    /**
     * 테스트 데이터 상태 확인
     * @return 테스트 데이터 개수
     */
    suspend fun getTestDataCount(): Int = withContext(Dispatchers.IO) {
        try {
            val allPhotos = photoRepository.getAllPhotos()
            var count = 0
            
            allPhotos.collect { photos ->
                count = photos.count { it.title.contains("(") && it.title.contains(")") }
            }
            
            return@withContext count
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext 0
        }
    }
}
