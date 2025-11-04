# MVP Ver2 Release Notes

## 릴리즈 정보
- **Version**: 1.0.0
- **Release Date**: 2025년 10월 27일
- **Build Type**: Release
- **Target SDK**: Android 34 (API Level 34)

## 주요 개선 사항

### 🐛 버그 수정

#### Bug #1, #2: 사진 선택 데이터 유지 문제
**문제**: 프레임 선택 화면에서 뒤로가기 시 선택했던 사진 데이터가 손실되었습니다.

**해결**:
- `PhotoSelectionScreen.kt`와 `FrameScreen.kt`의 불필요한 `DisposableEffect` 제거
- 사진 선택 상태가 화면 전환 시에도 안정적으로 유지되도록 개선

---

#### Bug #4: 프레임 재선택 시 이전 프레임이 적용되는 문제
**문제**: 다른 프레임을 선택해도 항상 동일한 프레임이 적용되었습니다.

**해결**:
- `FrameViewModel.kt`에 `clearComposedImage()` 함수 추가
- `FramePickerScreen.kt`에서 프레임 선택 시 이전 합성 결과 초기화 로직 추가
- 프레임 변경 시 새로 선택한 프레임이 정확히 적용되도록 개선

---

#### Bug #5: KTX 역 목록 불일치 문제
**문제**: 캘린더 탭과 결과 화면의 KTX 역 목록이 일치하지 않았습니다.

**해결**:
- `ResultScreen.kt`에서 `KtxStationData`를 데이터 소스로 사용하도록 변경
- 모든 화면에서 "경부선"과 "호남선"만 일관되게 표시되도록 개선

---

## 🆕 신규 기능

### Phase B: 캘린더 월 이동 기능
**설명**: 캘린더 탭에서 월 단위로 이동하며 해당 월의 사진을 확인할 수 있습니다.

**주요 기능**:
- 이전/다음 월 이동 버튼 추가
- 월 변경 시 해당 월의 사진 데이터만 필터링하여 표시
- 선택된 날짜 정보가 월 변경 시 자동 초기화

---

### Phase C: 노선도(잇다) 캠페인 기능
**설명**: 사용자가 방문한 KTX 역을 추적하고 완주를 달성하는 게이미피케이션 기능입니다.

**주요 기능**:
- 연도별 방문 역 추적 (2023 ~ 2026)
- 경부선/호남선 역 목록 표시
- 방문 여부 시각적 표시 (체크 아이콘/원)
- 완주 달성 시 배지 표시
- 프로필 탭에서 접근 가능

---

## 테스트 완료 항목

### Phase 1: Happy Path 테스트 (8개 케이스)
- ✅ 사진 선택 → 프레임 선택 → 저장
- ✅ 캘린더 → 홈 → 지도 연동
- ✅ 노선도 캠페인 기능

### Phase 2: 버그 회귀 테스트
- ✅ Bug #1, #2 해결 확인
- ✅ Bug #4 해결 확인
- ✅ Bug #5 해결 확인

### Phase 3: 신규 기능 상세 테스트
- ✅ 캘린더 월 이동 기능
- ✅ 노선도 캠페인 기능

### Phase 4: Edge Case 테스트
- ✅ 사진 미선택 시 버튼 비활성화
- ✅ 빠른 화면 전환 안정성

**전체 테스트 통과율**: 100% (8/8)

---

## 기술 스택

### 개발 환경
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Navigation**: Compose Navigation
- **Coroutines & Flow**: 비동기 처리 및 상태 관리

### 주요 라이브러리
- Compose Material 3
- Room Database
- Hilt (Dependency Injection)
- Navigation Compose
- Coroutines & Flow
- osmdroid (OpenStreetMap)

---

## 배포 파일 정보

### APK 파일
- **파일명**: `app-release-unsigned.apk`
- **위치**: `app/build/outputs/apk/release/`
- **서명 상태**: Unsigned (디버그용)
- **참고**: Google Play 배포 시에는 Signed App Bundle 생성 필요

---

## 사용 방법

### 앱 설치
1. `app-release-unsigned.apk` 파일을 Android 기기에 전송
2. 기기에서 "알 수 없는 소스" 설치 허용 설정
3. APK 파일을 탭하여 설치

### 주의사항
- Unsigned APK이므로 Google Play Store에는 직접 배포 불가
- 공식 배포 시 Signed App Bundle (.aab) 형식으로 빌드 필요
- Google Play Console에서 서명 키 설정 필수

---

## 향후 개선 계획 (선택 사항)

### 가능한 추가 기능
1. **SNS 공유 기능**: 인생 네컷 결과물을 SNS에 공유
2. **성능 최적화**: 앱 시작 시 프레임 드롭 개선
3. **UX 개선**: 화면 전환 애니메이션 강화

---

## 결론

MVP Ver2는 모든 핵심 기능이 안정적으로 작동하며, 배포 준비가 완료되었습니다.

**총 개발 일정**:
- 버그 수정 (Phase 1, 2): 완료
- 신규 기능 개발 (Phase B, C): 완료
- 통합 테스트: 100% 통과
- Release APK 빌드: 완료

**앱 상태**: **배포 가능** ✅

