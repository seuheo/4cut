# MVP Ver2 테스트 실행 가이드

**작성일**: 2025년 1월 13일  
**버전**: MVP Ver2  
**목적**: 실제 디바이스/에뮬레이터에서 MVP Ver2 기능 테스트를 수행하기 위한 단계별 가이드

---

## 📋 테스트 전 준비사항

### 필수 체크리스트
- [ ] 앱이 최신 코드로 빌드되었는지 확인
- [ ] 디바이스/에뮬레이터에 앱 설치 완료
- [ ] Logcat 필터 설정: `tag:com.example.a4cut` 또는 `package:com.example.a4cut`
- [ ] 디바이스에 최소 4장의 테스트 사진 준비 (갤러리)
- [ ] ADB 연결 확인 (파일 시스템 접근용, 선택사항)

---

## 🧪 Phase 1: JSON 프레임 시스템 테스트

### 테스트 1.1: JSON 파일 로드 확인

**준비**:
1. Logcat 필터 설정: `FrameRepository` 또는 `com.example.a4cut`

**테스트 절차**:
1. 앱 실행
2. Logcat 확인:
   - 앱 시작 시 "FrameRepository: JSON에서 3개의 프레임 슬롯 정보 로드 완료" 메시지 확인
   - 또는 "JSON 로드 실패" 오류 메시지 확인

**예상 로그**:
```
FrameRepository: JSON에서 3개의 프레임 슬롯 정보 로드 완료
```

**검증 포인트**:
- [ ] JSON 파일 로드 성공 메시지 확인
- [ ] 오류 메시지 없음 확인

**실패 시 대응**:
- `frames.json` 파일이 `app/src/main/assets/` 경로에 있는지 확인
- JSON 형식이 올바른지 확인 (`{"frames": [...]}` 구조)
- 앱 재설치 및 재시작

---

### 테스트 1.2: JSON 슬롯 정보를 사용한 이미지 합성

**준비**:
1. 갤러리에 최소 4장의 테스트 사진 준비
2. Logcat 필터 설정: `ImageComposer` 또는 `FrameViewModel`

**테스트 절차**:
1. 앱 실행 → "갤러리에서 사진 선택" 버튼 클릭
2. 사진 4장 선택
3. 프레임 선택 화면에서 `image_e15024` 프레임 선택
4. "인생네컷 만들기" 버튼 클릭
5. Logcat에서 다음 메시지 확인:
   - "JSON 슬롯 정보 사용: 4개"
   - "ImageComposer: composeLife4CutFrame 완료"

**예상 로그**:
```
=== ImageComposer: composeLife4CutFrame 시작 ===
프레임 ID: image_e15024
프레임 slots: 4개
JSON 슬롯 정보 사용: 4개
=== ImageComposer: composeLife4CutFrame 완료 ===
```

**검증 포인트**:
- [ ] "JSON 슬롯 정보 사용: 4개" 로그 확인
- [ ] 미리보기 다이얼로그에서 사진이 올바른 위치에 배치됨 (시각적 확인)
- [ ] 이미지 합성 완료 메시지 확인

**실패 시 대응**:
- "기존 하드코딩된 레이아웃 사용" 메시지가 나오는 경우:
  - Frame 객체에 slots 정보가 제대로 병합되지 않음
  - AppNavigation에서 `loadSlotsFromJson()` 호출 확인
- 이미지 합성 실패 시: 로그에서 구체적인 오류 메시지 확인

---

### 테스트 1.3: 기존 프레임 하위 호환성 확인

**준비**:
1. 갤러리에 최소 4장의 테스트 사진 준비
2. Logcat 필터 설정: `ImageComposer`

**테스트 절차**:
1. 갤러리에서 사진 4장 선택
2. `single_frame` 프레임 선택 (슬롯 정보 없음)
3. "인생네컷 만들기" 버튼 클릭
4. Logcat 확인:
   - "기존 하드코딩된 레이아웃 사용" 메시지 확인
   - "JSON 슬롯 정보 사용" 메시지 없음 확인

**예상 로그**:
```
=== ImageComposer: composeLife4CutFrame 시작 ===
프레임 slots: 0개
기존 하드코딩된 레이아웃 사용
=== ImageComposer: composeLife4CutFrame 완료 ===
```

**검증 포인트**:
- [ ] "기존 하드코딩된 레이아웃 사용" 로그 확인
- [ ] 이미지 합성 정상 완료 확인
- [ ] 합성된 이미지 품질 확인

---

## 🎬 Phase 2: 동영상 생성 및 공유 테스트

### 테스트 2.1: 슬라이드쇼 동영상 생성

**준비**:
1. 갤러리에 최소 4장의 테스트 사진 준비
2. Logcat 필터 설정: `VideoSlideShowCreator` 또는 `FrameViewModel`

**테스트 절차**:
1. 갤러리에서 사진 4장 선택
2. 프레임 선택 및 이미지 합성
3. "저장" 버튼 클릭
4. Logcat 확인:
   - "JCodec 동영상 생성 시작" 메시지
   - 각 사진별 인코딩 진행 로그
   - "JCodec 동영상 생성 완료" 메시지 및 파일 경로

**예상 로그**:
```
JCodec 동영상 생성 시작: /data/data/com.example.a4cut/files/videos/slideshow_XXXXX.mp4 (4장의 사진)
1번째 사진 인코딩 중...
1번째 사진 인코딩 완료 (60프레임)
2번째 사진 인코딩 중...
...
JCodec 동영상 생성 완료: /data/data/com.example.a4cut/files/videos/slideshow_XXXXX.mp4 (총 240프레임)
```

**검증 포인트**:
- [ ] 동영상 생성 함수 호출 확인 (로그)
- [ ] 동영상 파일 경로 확인 (로그)
- [ ] 오류 없이 동영상 생성 완료 확인

**파일 시스템 확인 (ADB 사용 시)**:
```bash
# 동영상 파일 존재 확인
adb shell ls -lh /data/data/com.example.a4cut/files/videos/

# 또는 외부 저장소 사용 시
adb shell ls -lh /storage/emulated/0/Android/data/com.example.a4cut/files/videos/
```

**실패 시 대응**:
- "슬라이드쇼를 만들 유효한 사진이 없습니다" 메시지:
  - `_photos.value`에 유효한 Bitmap이 포함되어 있는지 확인
- "JCodec 동영상 생성 실패" 메시지:
  - 로그에서 구체적인 오류 메시지 확인
  - 저장소 권한 확인
  - 디스크 공간 확인

---

### 테스트 2.2: 동영상 파일 형식 및 품질 확인

**준비**:
1. 테스트 2.1 완료 후 생성된 동영상 파일 경로 확인

**테스트 절차**:
1. 생성된 동영상 파일을 디바이스에서 재생
2. 동영상 정보 확인:
   - 파일 확장자: `.mp4`
   - 재생 길이: 약 8초 (4장 × 2초)
   - 해상도: 1080x1920

**검증 포인트**:
- [ ] 동영상 파일이 정상적으로 재생됨
- [ ] 각 사진이 2초씩 표시됨
- [ ] 해상도가 1080x1920인지 확인 (선택사항)

**실패 시 대응**:
- 동영상이 재생되지 않는 경우:
  - 파일 경로가 올바른지 확인
  - 파일 크기가 0이 아닌지 확인
  - 다른 동영상 플레이어로 시도

---

### 테스트 2.3: PhotoDetailScreen에서 동영상 공유

**준비**:
1. 테스트 2.1 완료 후 저장된 사진이 홈 화면에 표시되는지 확인
2. Logcat 필터 설정: `PhotoDetailScreen`

**테스트 절차**:
1. 홈 화면에서 저장된 사진 선택
2. PhotoDetailScreen으로 이동
3. Share 아이콘 클릭
4. Android 네이티브 공유 시트 확인

**예상 로그**:
```
PhotoDetailScreen: 공유 시작: 이미지=content://..., 동영상=/data/data/.../videos/slideshow_XXXXX.mp4
PhotoDetailScreen: 이미지 URI (content://): content://...
PhotoDetailScreen: 동영상 URI (FileProvider): content://com.example.a4cut.provider/external_files/videos/slideshow_XXXXX.mp4
PhotoDetailScreen: 공유할 파일 개수: 2개
PhotoDetailScreen: 다중 파일 공유 Intent 생성: 파일 2개
PhotoDetailScreen: 공유 시트 표시 완료
```

**검증 포인트**:
- [ ] Share 아이콘이 표시됨
- [ ] 공유 시트가 정상적으로 표시됨
- [ ] 공유 대상 앱에서 이미지와 동영상이 함께 표시됨

**실패 시 대응**:
- "공유할 파일을 찾을 수 없습니다" Toast 메시지:
  - 파일 경로 확인
  - 파일 존재 여부 확인
- "공유할 수 있는 앱이 없습니다" Toast 메시지:
  - 디바이스에 공유 가능한 앱(인스타그램, 카카오톡 등) 설치 확인
- FileProvider URI 생성 실패:
  - `provider_paths.xml` 설정 확인
  - `AndroidManifest.xml` FileProvider 설정 확인

---

### 테스트 2.4: 이미지만 있는 사진 공유 (하위 호환성)

**준비**:
1. 동영상 생성 이전에 저장된 사진 또는 동영상 생성 실패한 사진 선택

**테스트 절차**:
1. 동영상이 없는 사진을 PhotoDetailScreen에서 열기
2. Share 아이콘 클릭
3. 공유 시트에서 이미지만 포함되어 있는지 확인

**예상 로그**:
```
PhotoDetailScreen: 공유 시작: 이미지=content://..., 동영상=null
PhotoDetailScreen: 이미지 URI (content://): content://...
PhotoDetailScreen: 공유할 파일 개수: 1개
PhotoDetailScreen: 단일 파일 공유 Intent 생성: type=image/jpeg
PhotoDetailScreen: 공유 시트 표시 완료
```

**검증 포인트**:
- [ ] Share 기능이 정상적으로 작동함
- [ ] 공유 시트에서 이미지만 포함되어 있음

---

## 🔍 통합 테스트

### 테스트 3.1: 전체 워크플로우 테스트

**목표**: 사진 선택부터 저장 및 공유까지 전체 워크플로우 테스트

**준비**:
1. 갤러리에 최소 4장의 테스트 사진 준비
2. Logcat 전체 로그 확인 준비

**테스트 절차**:
1. 앱 실행
2. "갤러리에서 사진 선택" 버튼 클릭
3. 사진 4장 선택
4. 프레임 선택 (`image_e15024`)
5. "인생네컷 만들기" 버튼 클릭
6. 미리보기 확인 후 "저장" 버튼 클릭
7. 저장 완료 대기 (동영상 생성 포함)
8. 홈 화면에서 저장된 사진 확인
9. 사진 선택하여 PhotoDetailScreen으로 이동
10. Share 아이콘 클릭하여 공유 확인

**검증 포인트**:
- [ ] 모든 단계가 오류 없이 진행됨
- [ ] 이미지 합성 및 저장 성공
- [ ] 동영상 생성 및 저장 성공
- [ ] 공유 기능 정상 작동

**실패 시 대응**:
- 각 단계별 로그를 확인하여 오류 발생 지점 파악
- `MVP_Ver2_Test_Results.md`에 오류 내용 기록

---

## 📊 로그 분석 가이드

### Phase 1 관련 로그
- `FrameRepository`: JSON 파일 로드 관련
- `ImageComposer`: 이미지 합성 관련
- `FrameViewModel`: 프레임 선택 및 합성 트리거 관련

### Phase 2 관련 로그
- `VideoSlideShowCreator`: 동영상 생성 관련
- `PhotoDetailScreen`: 공유 기능 관련
- `FrameViewModel`: 동영상 생성 호출 관련

---

## 🐛 문제 해결 가이드

### 문제 1: JSON 파일 로드 실패
**증상**: "JSON 로드 실패" 로그
**해결**:
1. `app/src/main/assets/frames.json` 파일 존재 확인
2. JSON 형식 검증 (JSON Lint 사용)
3. 앱 재설치 및 재시작

### 문제 2: 동영상 생성 실패
**증상**: "JCodec 동영상 생성 실패" 로그
**해결**:
1. JCodec 라이브러리 의존성 확인 (`app/build.gradle.kts`)
2. 저장소 권한 확인 (AndroidManifest.xml)
3. 디스크 공간 확인
4. 로그에서 구체적인 오류 메시지 확인

### 문제 3: 공유 기능 실패
**증상**: Share 아이콘 클릭 시 공유 시트 미표시
**해결**:
1. FileProvider 설정 확인
2. 파일 경로 확인
3. ActivityNotFoundException 확인
4. 로그에서 구체적인 오류 메시지 확인

---

## ✅ 테스트 완료 체크리스트

### Phase 1
- [ ] 테스트 1.1: JSON 파일 로드 성공
- [ ] 테스트 1.2: JSON 슬롯 정보를 사용한 이미지 합성 성공
- [ ] 테스트 1.3: 기존 프레임 하위 호환성 확인 완료

### Phase 2
- [ ] 테스트 2.1: 슬라이드쇼 동영상 생성 성공
- [ ] 테스트 2.2: 동영상 파일 형식 및 품질 확인 완료
- [ ] 테스트 2.3: PhotoDetailScreen에서 동영상 공유 성공
- [ ] 테스트 2.4: 이미지만 있는 사진 공유 성공

### 통합 테스트
- [ ] 테스트 3.1: 전체 워크플로우 성공

---

**다음 단계**: 실제 디바이스/에뮬레이터에서 테스트 실행 후 `MVP_Ver2_Test_Results.md`에 결과 기록

