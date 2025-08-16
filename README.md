# KTX 네컷 앱 📱

## 📖 프로젝트 개요

**프로젝트명**: KTX 네컷 앱  
**버전**: MVP v1.0  
**목표**: KTX를 자주 이용하는 20대 대학생 '김나영'처럼, 친구들과의 여행을 특별하게 기록하고 싶어 하는 사용자를 위해, 기차역에서의 순간을 트렌디한 4컷 사진으로 만들어 즉시 공유하는 모바일 앱

## 🎯 핵심 가치 제안

1. **원스톱 경험**: 여러 앱을 거칠 필요 없이 앱 하나로 촬영부터 KTX 감성이 담긴 프레임 적용, 인스타그램 스토리 공유까지 한 번에 해결
2. **트렌디한 포토존**: 평범한 기차역을 감성적인 포토존으로 바꿔주는 KTX 티켓 모티브의 시그니처 프레임 제공
3. **스토리 최적화 공유**: 인스타그램 스토리에 딱 맞는 세로형 4컷 결과물을 제공하여 사용자의 공유 욕구를 자극

## 👤 타겟 사용자

**주요 페르소나: 김나영 (21세, 대학생)**
- **사용 패턴**: 주말마다 KTX 이용, 인스타그램 스토리 공유 선호
- **핵심 니즈**: 친구들과의 여행 기념, 트렌디한 사진 촬영
- **불만사항**: 
  - "기차역에서 사진 찍을 만한 곳이 마땅치 않아"
  - "일반 카메라로 찍는 건 너무 평범해 보여"
  - "사진 찍고 꾸미려면 여러 앱을 거쳐야 해서 번거로워"

## 🛠️ 기술 스택

### UI & 프레임워크
- **UI**: Jetpack Compose
- **디자인**: Material Design 3 + KTX 브랜드 컬러
- **언어**: Kotlin
- **네비게이션**: Compose Navigation

### 아키텍처 & 패턴
- **아키텍처**: MVVM (Model-View-ViewModel)
- **상태 관리**: Compose State + ViewModel
- **비동기 처리**: Coroutines & Flow

### 이미지 처리
- **이미지 로딩**: Coil
- **이미지 합성**: Canvas API, Bitmap
- **저장 형식**: JPEG (SNS 공유 최적화)
- **일러스트**: Canvas API로 KTX 기차 그리기

### 권한 관리
- **카메라**: CameraX (추후 구현)
- **저장소**: MediaStore API
- **권한**: AndroidX Activity Result Contracts

## 🏗️ 프로젝트 구조

```
app/
├── src/main/
│   ├── java/com/example/a4cut/
│   │   ├── MainActivity.kt
│   │   ├── data/                  // 데이터 모델 및 소스
│   │   │   ├── model/
│   │   │   │   ├── Photo.kt
│   │   │   │   └── Frame.kt       // 프레임 데이터 모델
│   │   │   └── repository/
│   │   │       ├── PhotoRepository.kt
│   │   │       └── FrameRepository.kt
│   │   ├── ui/                    // UI 관련 클래스
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt       // KTX 브랜드 컬러
│   │   │   │   ├── Theme.kt
│   │   │   │   └── Type.kt
│   │   │   ├── screens/
│   │   │   │   ├── HomeScreen.kt  // 홈 화면 (기차 배경 + 프레임 캐러셀 + 캘린더)
│   │   │   │   ├── FrameScreen.kt // 프레임 화면 (4컷 사진 선택 + 프레임 적용)
│   │   │   │   └── EmptyScreen.kt // 빈 화면 (달력, 설정, 프로필)
│   │   │   ├── components/
│   │   │   │   ├── KTXIllustration.kt // KTX 기차 일러스트
│   │   │   │   ├── FrameCarousel.kt   // 프레임 캐러셀
│   │   │   │   ├── CalendarView.kt    // 캘린더 뷰
│   │   │   │   └── BottomNavigation.kt // 하단 네비게이션
│   │   │   └── viewmodel/
│   │   │       ├── HomeViewModel.kt
│   │   │       └── FrameViewModel.kt
│   │   └── utils/                 // 유틸리티 클래스
│   │       ├── ImageProcessor.kt
│   │       ├── PermissionHelper.kt
│   │       └── KTXColors.kt       // KTX 브랜드 컬러 정의
│   ├── res/
│   └── AndroidManifest.xml
```

## 🚀 개발 환경 설정

### 필수 요구사항
- **Android Studio**: Jellyfish (2023.3.1) 이상
- **Android SDK**: API 24 (Android 7.0) 이상
- **Kotlin**: 2.0.21 이상
- **JDK**: 17 이상

### 의존성 버전
```kotlin
// build.gradle.kts
compileSdk = 36
targetSdk = 36
minSdk = 24

// Jetpack Compose
composeBom = "2024.09.00"
composeCompiler = "1.5.10"

// 주요 라이브러리
coil = "2.6.0"
viewmodelCompose = "2.9.2"
navigationCompose = "2.7.7"
```

## 📱 MVP v1.0 기능

### ✅ 구현 예정 기능
- [x] 4컷 사진 그리드 UI (인스타그램 스토리에 최적화된 2x2 세로 레이아웃) ✅ **Week 1 완료**
- [x] 기본 UI 구조 및 MVVM 아키텍처 ✅ **Week 1 완료**
- [ ] **홈 화면**: KTX 기차 일러스트 + 프레임 캐러셀 + 캘린더
- [ ] **프레임 화면**: 1행 4열 4컷 사진 선택 + 프레임 적용
- [ ] **하단 네비게이션**: 홈, 프레임, 달력, 설정, 프로필 탭
- [ ] KTX 티켓을 모티브로 한 시그니처 프레임 1종 제공
- [ ] 4컷 이미지 하나로 합성 및 JPEG 형식으로 저장
- [ ] 인스타그램 스토리로 바로 공유하는 기능
- [ ] 필수 권한(저장소) 요청 및 기본 에러 처리

### 🔄 추후 구현 예정
- [ ] KTX 역별 스페셜 프레임 (예: 부산역 - 갈매기, 전주역 - 한옥)
- [ ] 사진 촬영 기능 (CameraX 연동)
- [ ] 갤러리에서 사진 선택 기능
- [ ] 간단한 스티커 및 텍스트 추가 기능
- [ ] GIF(움짤) 촬영 모드

## 📊 현재 진행 상황

### 🎯 전체 진행률: 35% (Week 1 완료)

#### ✅ Week 1: 기본 구조 구축 (100% 완료)
- [x] Jetpack Compose 프로젝트 설정
- [x] MVVM 아키텍처 구축
- [x] 4컷 사진 그리드 UI 구현
- [x] 기본 UI 컴포넌트 (저장/공유 버튼)
- [x] 의존성 설정 및 빌드 성공

#### 🔄 Week 2: 홈 화면 및 네비게이션 구현 (0% 시작)
- [ ] 하단 네비게이션 바 구현
- [ ] 홈 화면: KTX 기차 일러스트 + 프레임 캐러셀 + 캘린더
- [ ] 프레임 화면: 4컷 사진 선택 + 프레임 적용
- [ ] 빈 화면들 (달력, 설정, 프로필)

#### 📋 Week 3: 핵심 기능 및 최적화 (0% 시작)
- [ ] 이미지 합성 및 저장 기능 구현
- [ ] 인스타그램 스토리 공유 기능 구현
- [ ] 권한 처리 및 에러 핸들링
- [ ] UI/UX 최종 다듬기
- [ ] MVP v1.0 완성 및 배포 준비

## 🎨 UI/UX 디자인 방향

### KTX 브랜드 컬러
- **Primary**: KTX 파란색 (#1E3A8A)
- **Secondary**: 기차 실버 (#E5E7EB)  
- **Accent**: KTX 오렌지 (#F59E0B)
- **Background**: 밝은 회색 (#F9FAFB)

### 최신 트렌드 요소
- **Glassmorphism**: 반투명 효과와 블러
- **부드러운 애니메이션**: 프레임 전환, 버튼 상호작용
- **Material Design 3**: 최신 컴포넌트 및 스타일
- **반응형 레이아웃**: 다양한 화면 크기 지원

### 화면 구성
1. **홈 화면**: KTX 기차 배경 + 프레임 캐러셀 + 캘린더
2. **프레임 화면**: 4컷 사진 선택 + 프레임 적용 + 미리보기
3. **기타 탭들**: 달력, 설정, 프로필 (빈 화면, 나중에 구현)

## 🏃‍♂️ 빌드 및 실행

### 1. 프로젝트 클론
```bash
git clone [repository-url]
cd 4cut
```

### 2. Android Studio에서 열기
- Android Studio 실행
- "Open an existing project" 선택
- 프로젝트 폴더 선택

### 3. 의존성 동기화
- Gradle 동기화 대기
- 필요한 SDK 다운로드

### 4. 에뮬레이터에서 실행
- AVD Manager에서 에뮬레이터 생성 (API 24 이상)
- Run 버튼 클릭하여 앱 실행

## 🧪 테스트

### 현재 테스트 범위
- **에뮬레이터 테스트**: 기본 UI 동작 확인 ✅
- **기본 기능 테스트**: 4컷 그리드 표시, 기본 UI 구조 ✅

### 추후 테스트 계획
- **단위 테스트**: JUnit + Mockk
- **UI 테스트**: Compose Test
- **통합 테스트**: 실제 기기 테스트
- **사용성 테스트**: 실제 타겟 사용자(20대 대학생)를 대상으로 MVP 버전의 만족도 및 개선점 피드백 수렴

## 📅 개발 일정

### 🚀 Week 2: 홈 화면 및 네비게이션 구현 (2-3일)
- [ ] 하단 네비게이션 바 구현
- [ ] 홈 화면: KTX 기차 일러스트 + 프레임 캐러셀 + 캘린더
- [ ] 프레임 화면: 4컷 사진 선택 + 프레임 적용
- [ ] 빈 화면들 (달력, 설정, 프로필)

### 🎯 Week 3: 핵심 기능 및 최적화 (2-3일)
- [ ] 이미지 합성 및 저장 기능 구현
- [ ] 인스타그램 스토리 공유 기능 구현
- [ ] 권한 처리 및 에러 핸들링
- [ ] UI/UX 최종 다듬기
- [ ] MVP v1.0 완성 및 배포 준비

## 🎉 주요 성과

### Week 1 완료 성과
1. **빌드 성공**: 모든 의존성 정상 통합
2. **아키텍처 구축**: MVVM + Repository 패턴 구현
3. **UI 기본 구조**: Compose 기반 4컷 그리드 완성
4. **상태 관리**: StateFlow 기반 반응형 UI 구현

## 🚧 현재 이슈 및 해결 방안

### ✅ 해결된 이슈
1. **TopAppBar API 경고**: @OptIn(ExperimentalMaterial3Api::class) 적용으로 해결
2. **Kotlin 플러그인 버전**: libs.versions.toml에서 kotlin 버전 참조 수정
3. **빌드 실패**: 모든 의존성 정상 추가로 해결

### 🔍 잠재적 리스크
1. **Canvas API 복잡성**: KTX 기차 일러스트 그리기 구현 난이도
2. **프레임 캐러셀**: 부드러운 애니메이션 및 성능 최적화
3. **권한 처리**: Android 13+ 권한 정책 변경사항
4. **Instagram API**: 공식 API 제한 및 변경 가능성

## 📞 문의 및 기여

- **개발자**: [개발자 정보]
- **이슈 리포트**: GitHub Issues
- **기여 방법**: Pull Request

## 📄 라이선스

[라이선스 정보 추가 예정]

---

**문서 작성일**: 2025년 1월 16일  
**문서 버전**: 3.0 (홈 화면 요구사항 반영)  
**작성자**: AI Assistant
