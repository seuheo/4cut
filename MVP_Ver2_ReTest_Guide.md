# MVP Ver2 재테스트 가이드

**작성일**: 2025년 1월 13일  
**버전**: MVP Ver2  
**상태**: 수정 완료, 재테스트 필요

---

## 🔧 수정 완료 사항

### 수정 1: `FrameViewModel.setContext()`에서 JSON 슬롯 정보 로드 ✅

**문제**: 
- `FrameViewModel`의 `frameRepository`와 `AppNavigation`의 `frameRepository`가 서로 다른 인스턴스
- `AppNavigation`에서 `loadSlotsFromJson()`을 호출해도 `FrameViewModel`의 `frameRepository`는 업데이트되지 않음

**해결 방법**:
- `FrameViewModel.setContext()`에서 `frameRepository.loadSlotsFromJson(context)` 호출
- JSON 슬롯 정보 로드 후, `_frames.value` 업데이트

**수정 코드**:
```kotlin
fun setContext(context: Context) {
    // ...
    // FrameRepository에서 JSON 슬롯 정보 로드 (Phase 1 수정)
    frameRepository.loadSlotsFromJson(context)
    
    // JSON 슬롯 정보 로드 후, _frames.value 업데이트
    _frames.value = frameRepository.getFrames()
    println("FrameViewModel: JSON 슬롯 정보 로드 완료, _frames 업데이트: ${_frames.value.size}개")
    // ...
}
```

---

## 🧪 재테스트 항목

### Phase 1 재테스트

#### 테스트 1: JSON 슬롯 정보 로드 확인

**테스트 절차**:
1. 앱 실행
2. Logcat에서 다음 로그 확인:
   - `FrameRepository: JSON에서 3개의 프레임 슬롯 정보 로드 완료`
   - `FrameViewModel: JSON 슬롯 정보 로드 완료, _frames 업데이트: 4개`

**예상 결과**:
- ✅ 두 로그 모두 나타남
- ✅ `_frames.value`에 JSON 슬롯 정보가 포함됨

---

#### 테스트 2: `long_form_black` 프레임 선택 시 slots 정보 확인

**테스트 절차**:
1. 갤러리에서 사진 4장 선택
2. `long_form_black` 프레임 선택
3. Logcat에서 다음 로그 확인:
   - `프레임 선택 완료: Long Form Black (ID: long_form_black, slots: 4개)`
   - `프레임 slots: 4개`
   - `JSON 슬롯 정보 사용: 4개` (또는 유사한 로그)

**예상 결과**:
- ✅ `slots: 4개` 로그 확인
- ✅ `기존 하드코딩된 레이아웃 사용` 로그 **없음**
- ✅ JSON 슬롯 정보 사용 로그 확인

---

#### 테스트 3: JSON 슬롯 정보를 사용한 이미지 합성 확인

**테스트 절차**:
1. 테스트 2 완료 후
2. "인생네컷 만들기" 버튼 클릭
3. 이미지 합성 실행
4. Logcat에서 다음 로그 확인:
   - `JSON 슬롯 정보 사용: 4개` (또는 유사한 로그)
   - `기존 하드코딩된 레이아웃 사용` 로그 **없음**

**예상 결과**:
- ✅ JSON 슬롯 정보 사용 로그 확인
- ✅ 합성된 이미지가 정상적으로 생성됨
- ✅ 사진이 프레임 슬롯에 올바르게 배치됨

---

### Phase 2 재테스트

#### 테스트 4: 동영상 생성 및 DB 저장 확인

**테스트 절차**:
1. 갤러리에서 사진 4장 선택
2. 프레임 선택 및 이미지 합성
3. KTX 역 선택
4. "저장" 버튼 클릭
5. Logcat에서 다음 로그 확인:
   - `JCodec 동영상 생성 시작: ...`
   - `FrameViewModel: 동영상 생성 완료: videoPath=...` ← **이 로그 확인**
   - `FrameViewModel: PhotoEntity 저장 완료: imagePath=..., videoPath=...` ← **이 로그 확인**
   - `JCodec 동영상 생성 완료: ... (총 240프레임)`

**예상 결과**:
- ✅ 동영상 생성 시작 및 완료 로그 확인
- ✅ `FrameViewModel: 동영상 생성 완료` 로그 확인 (새로 추가된 로그)
- ✅ `FrameViewModel: PhotoEntity 저장 완료` 로그 확인 (새로 추가된 로그)
- ✅ `videoPath`가 null이 아닌 경로로 저장됨

---

#### 테스트 5: PhotoDetailScreen에서 동영상 공유 확인

**테스트 절차**:
1. 테스트 4 완료 후
2. 홈 화면에서 저장된 사진 선택
3. PhotoDetailScreen으로 이동
4. Share 아이콘 클릭
5. Logcat에서 다음 로그 확인:
   - `공유 시작: 이미지=..., 동영상=/storage/.../slideshow_....mp4` ← **동영상 경로 확인**

**예상 결과**:
- ✅ 공유 시 `동영상=...` (null이 아님)
- ✅ Android 네이티브 공유 시트가 표시됨
- ✅ 이미지와 동영상이 함께 공유 가능한 상태

---

## 📊 테스트 결과 기록

### Phase 1 테스트 결과
- [ ] 테스트 1: JSON 슬롯 정보 로드 확인 - 성공 / 실패
- [ ] 테스트 2: `long_form_black` 프레임 선택 시 slots 정보 확인 - 성공 / 실패
- [ ] 테스트 3: JSON 슬롯 정보를 사용한 이미지 합성 확인 - 성공 / 실패

### Phase 2 테스트 결과
- [ ] 테스트 4: 동영상 생성 및 DB 저장 확인 - 성공 / 실패
- [ ] 테스트 5: PhotoDetailScreen에서 동영상 공유 확인 - 성공 / 실패

---

## ✅ 성공 기준

다음 조건이 모두 충족되면 테스트를 통과한 것으로 간주합니다:

1. **Phase 1**:
   - [ ] JSON 슬롯 정보 로드 로그 확인
   - [ ] `long_form_black` 프레임 선택 시 `slots: 4개` 로그 확인
   - [ ] JSON 슬롯 정보 사용 로그 확인 (또는 "기존 하드코딩된 레이아웃 사용" 로그 없음)

2. **Phase 2**:
   - [ ] `FrameViewModel: 동영상 생성 완료` 로그 확인
   - [ ] `FrameViewModel: PhotoEntity 저장 완료` 로그에서 `videoPath` 확인
   - [ ] PhotoDetailScreen에서 공유 시 `동영상=...` (null이 아님)

---

**테스트 상태**: 수정 완료, 재테스트 대기  
**다음 단계**: 실제 디바이스/에뮬레이터에서 재테스트 실행

