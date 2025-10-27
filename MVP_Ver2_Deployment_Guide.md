# MVP Ver2 배포 가이드

## 📦 생성된 파일

### 1. App Bundle (권장 - Google Play 배포용)
- **파일명**: `app-release.aab`
- **위치**: `app/build/outputs/bundle/release/`
- **용도**: Google Play Console 업로드

### 2. Unsigned APK (직접 설치용)
- **파일명**: `app-release-unsigned.apk`
- **위치**: `app/build/outputs/apk/release/`
- **용도**: 직접 설치 또는 내부 테스트

---

## 🚀 Google Play 배포 절차

### Step 1: Play Console 접속
1. [Google Play Console](https://play.google.com/console) 접속
2. 계정 로그인

### Step 2: 앱 생성 (신규 앱인 경우)
1. "앱 만들기" 클릭
2. 앱 이름: "KTX 네컷 앱"
3. 기본 언어: 한국어
4. 앱 또는 게임: 앱
5. 무료 또는 유료: 무료
6. Google Play 정책에 동의 및 앱 만들기

### Step 3: 제작 설정 완료
필수 정보 입력:
- **앱 아이콘**: 512x512 PNG (투명 배경 권장)
- **기능 그래픽**: 1024x500 PNG
- **약관 URL**: (선택사항)
- **개인정보 처리방침 URL**: (필수)

### Step 4: App Bundle 업로드
1. 왼쪽 메뉴 → "제작" → "프로덕션" (또는 "내부 테스트")
2. "새 버전 만들기" 클릭
3. **출시 이름**: "MVP Ver2" 또는 "버전 1.0.0"
4. **게시를 통해 앱 서명 사용** 체크 (Google Play App Signing 사용)
5. "AAB 파일 업로드" 클릭
6. `app-release.aab` 파일 선택
7. "계속" 클릭

### Step 5: 버전 릴리스
1. Google Play에서 자동으로 앱 번들 검증 (약 10-15분)
2. 검증 완료 후 "검토 제출" 클릭
3. Google Play 리뷰 프로세스 진행 (보통 1-2일)

---

## 📋 배포 체크리스트

### 앱 정보
- ⬜ 앱 이름
- ⬜ 앱 아이콘
- ⬜ 기능 그래픽
- ⬜ 스크린샷 (최소 2개)
- ⬜ 개인정보 처리방침 URL

### 테스트
- ✅ Internal Testing Track (내부 테스트)
- ⬜ Closed Testing Track (비공개 테스트)
- ⬜ Open Testing Track (공개 테스트)

### 법적 요구사항
- ⬜ 개인정보 처리방침 준비
- ⬜ 사용자 데이터 수집/보관 정책 명시
- ⬜ 서비스 약관 (선택사항)

---

## 🎯 배포 전 최종 확인사항

### 기능 검증
- ✅ 사진 선택 → 프레임 선택 → 저장
- ✅ 캘린더 월 이동
- ✅ 노선도 캠페인 기능
- ✅ KTX 역 목록 일관성
- ✅ 데이터 유지 안정성

### 성능
- ✅ 빌드 오류 없음
- ✅ 런타임 크래시 없음
- ✅ UI 반응성 양호

### 보안
- ⬜ ProGuard 설정 확인
- ⬜ 민감한 정보 하드코딩 확인
- ⬜ API 키 보안 확인

---

## 📄 관련 문서
- `MVP_Ver2_Release_Notes.md`: 릴리즈 노트
- `MVP_Ver2_Test_Results_Final.md`: 최종 테스트 결과
- `MVP_Ver2_Final_Integration_Test.md`: 통합 테스트 계획

---

## 🎉 배포 완료 후

배포가 완료되면:
1. 내부 테스트자들에게 테스트 링크 공유
2. 사용자 피드백 수집
3. 다음 버전 계획 수립

---

## 문의 및 지원

추가 배포 지원이 필요한 경우 알려주세요.

