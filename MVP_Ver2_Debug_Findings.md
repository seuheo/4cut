# MVP Ver2 테스트 중 발견된 문제 및 수정 사항

**작성일**: 2025년 1월 13일  
**버전**: MVP Ver2  
**상태**: 문제 발견 및 수정 진행 중

---

## 🔍 발견된 문제

### 문제 1: `long_form_black` 프레임의 `slots`가 null로 표시됨

**증상**:
- JSON 파일 로드 성공: `FrameRepository: JSON에서 3개의 프레임 슬롯 정보 로드 완료`
- 하지만 `long_form_black` 프레임 선택 시 `slots=null`로 표시됨
- 결과적으로 "기존 하드코딩된 레이아웃 사용" 로그가 나타남

**로그 분석**:
```
2025-11-01 21:04:01.131  8507-8547  System.out              com.example.a4cut                    I  프레임 slots: 0개
2025-11-01 21:04:01.196  8507-8547  System.out              com.example.a4cut                    I  기존 하드코딩된 레이아웃 사용
```

**원인 분석**:
1. `FrameRepository.loadSlotsFromJson()`은 `_frames.value`만 업데이트함
2. `FrameViewModel.selectFrame()`은 전달받은 `Frame` 객체를 그대로 `_selectedFrame.value`에 저장함
3. 문제: 프레임을 선택할 때 이미 선택된 `Frame` 객체는 JSON 슬롯 정보가 업데이트되기 전의 객체일 수 있음

**해결 방법**:
- `selectFrame()`에서 프레임을 선택할 때, 항상 최신 `_frames.value`에서 해당 ID의 프레임을 찾아서 선택하도록 수정
- 이렇게 하면 JSON 슬롯 정보가 포함된 최신 Frame 객체를 선택할 수 있음

**수정 내용**:
```kotlin
fun selectFrame(frame: Frame) {
    // 최신 _frames.value에서 해당 프레임을 찾아서 선택 (JSON 슬롯 정보 포함)
    val updatedFrame = _frames.value.find { it.id == frame.id } ?: frame
    _selectedFrame.value = updatedFrame
    println("프레임 선택 완료: ${updatedFrame.name} (ID: ${updatedFrame.id}, slots: ${updatedFrame.slots?.size ?: 0}개)")
    
    clearError()
    triggerHapticFeedback()
}
```

---

### 문제 2: 동영상 생성은 성공했지만 DB에 `videoPath`가 저장되지 않음

**증상**:
- 동영상 생성 성공: `JCodec 동영상 생성 완료: /storage/emulated/0/Android/data/com.example.a4cut/files/videos/slideshow_1761998663643.mp4 (총 240프레임)`
- 하지만 `PhotoDetailScreen`에서 공유 시 `동영상=null`로 표시됨

**로그 분석**:
```
2025-11-01 21:04:23.644  8507-8549  VideoSlideShowCreator   com.example.a4cut                    D  JCodec 동영상 생성 시작: ...
2025-11-01 21:05:10.697  8507-8549  VideoSlideShowCreator   com.example.a4cut                    D  JCodec 동영상 생성 완료: ... (총 240프레임)

2025-11-01 21:04:38.831  8507-8507  PhotoDetailScreen       com.example.a4cut                    D  공유 시작: 이미지=content://media/external_primary/images/media/119, 동영상=null
```

**타임라인 분석**:
- 21:04:23.644: 동영상 생성 시작
- 21:04:23.617: 갤러리 저장 완료 (`content://media/external_primary/images/media/119`)
- 21:04:38.831: 공유 시도 (동영상 생성 중) → `동영상=null`
- 21:05:10.697: 동영상 생성 완료 (약 1분 47초 후)
- 21:05:10.712: 사진 개수 업데이트 (DB 저장 완료?)

**원인 분석**:
1. `createSlideShowVideo()`는 `suspend fun`임
2. 현재 코드:
   ```kotlin
   val videoPath = context?.let { ctx ->
       VideoSlideShowCreator.createSlideShowVideo(_photos.value, ctx)
   }
   ```
3. 문제: `suspend fun`을 `let` 블록 안에서 호출하면, 코루틴 컨텍스트가 제대로 전달되지 않을 수 있음
4. 또한 동영상 생성은 비동기로 진행되므로, `createKTXPhoto()` 호출 시점에 아직 동영상이 생성되지 않았을 수 있음

**해결 방법**:
- `context?.let { ... }` 블록 대신 직접 호출하도록 수정
- `suspend fun`이므로 `viewModelScope.launch` 블록 안에서 자동으로 await됨

**수정 내용**:
```kotlin
// 원본 사진 4장으로 슬라이드쇼 동영상 생성 (suspend fun이므로 await 필요)
val videoPath = if (context != null) {
    VideoSlideShowCreator.createSlideShowVideo(_photos.value, context!!)
} else {
    null
}

Log.d("FrameViewModel", "동영상 생성 완료: videoPath=$videoPath")
```

---

## 📋 수정 사항 요약

### 수정 1: FrameViewModel.selectFrame()
- 최신 `_frames.value`에서 프레임을 찾아서 선택하도록 수정
- JSON 슬롯 정보가 포함된 최신 Frame 객체를 선택할 수 있도록 개선

### 수정 2: FrameViewModel.saveImage()
- `suspend fun` 호출 방식을 개선
- `context?.let { ... }` 블록 대신 직접 호출
- 디버그 로그 추가

---

## 🧪 재테스트 필요 항목

### Phase 1 재테스트
- [ ] `long_form_black` 프레임 선택 시 `slots` 정보가 정상적으로 표시되는지 확인
- [ ] "JSON 슬롯 정보 사용: 4개" 로그가 나타나는지 확인

### Phase 2 재테스트
- [ ] 동영상 생성 완료 후 DB에 `videoPath`가 정상적으로 저장되는지 확인
- [ ] `PhotoDetailScreen`에서 공유 시 동영상이 함께 표시되는지 확인

---

## ✅ 수정 완료 상태

- [x] 수정 1: `FrameViewModel.selectFrame()` 수정 완료
- [x] 수정 2: `FrameViewModel.saveImage()` 수정 완료
- [ ] 재테스트 필요

---

**수정 상태**: 완료  
**재테스트 상태**: 대기 중

