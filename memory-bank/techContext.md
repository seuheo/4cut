# KTX 네컷 앱 - 기술 컨텍스트

## 🛠️ 기술 스택 현황

### 현재 구현된 기술
- ✅ **언어**: Kotlin 2.0.21
- ✅ **UI 프레임워크**: Jetpack Compose
- ✅ **아키텍처**: MVVM + Repository Pattern
- ✅ **상태 관리**: Compose State + StateFlow
- ✅ **비동기 처리**: Coroutines & Flow
- ✅ **이미지 로딩**: Coil 2.6.0
- ✅ **의존성 관리**: Gradle (Kotlin DSL)

### 추가 구현 예정 기술
- 🔄 **이미지 처리**: Canvas API, Bitmap
- 🔄 **파일 저장**: MediaStore API
- 🔄 **권한 관리**: Activity Result Contracts
- 🔄 **공유 기능**: Intent, Instagram Story API

## 🚀 개발 환경 설정

### 필수 요구사항
- **Android Studio**: Jellyfish (2023.3.1) 이상
- **Android SDK**: API 24 (Android 7.0) 이상
- **Kotlin**: 2.0.21 이상
- **JDK**: 17 이상
- **Gradle**: 8.12.0 이상

### 권장 설정
- **compileSdk**: 36
- **targetSdk**: 36
- **minSdk**: 24
- **Compose BOM**: 2024.09.00
- **Compose Compiler**: Kotlin 2.0.21 호환 버전

## 📱 플랫폼 제약사항

### Android API 레벨
- **최소 지원**: API 24 (Android 7.0 Nougat)
- **타겟**: API 36 (Android 14)
- **권장**: API 34+ (Android 14+)

### 기기 호환성
- **화면 크기**: 4.7인치 이상 권장
- **메모리**: 최소 2GB RAM
- **저장공간**: 최소 100MB 여유 공간
- **카메라**: 후면 카메라 필수 (추후 구현)

### 권한 요구사항
- **저장소**: READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
- **카메라**: CAMERA (추후 구현)
- **네트워크**: INTERNET (공유 기능용)

## 🔧 빌드 시스템

### Gradle 설정
```kotlin
// build.gradle.kts (app)
android {
    namespace = "com.example.a4cut"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.example.a4cut"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}
```

### 의존성 관리
```kotlin
dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // 이미지 로딩
    implementation(libs.coil.compose)
    
    // 기타
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
}
```

## 🎨 UI/UX 기술적 요구사항

### Compose 컴포넌트
- **Material 3**: 최신 Material Design 시스템 준수
- **반응형 레이아웃**: 다양한 화면 크기 지원
- **다크 모드**: 자동 테마 전환 지원
- **접근성**: ContentDescription, TalkBack 지원

### 성능 요구사항
- **앱 시작**: 3초 이내 메인 화면 표시
- **이미지 로딩**: 1초 이내 썸네일 표시
- **이미지 합성**: 3초 이내 완료
- **메모리 사용**: 최대 512MB 제한

### 애니메이션
- **전환 효과**: Material 3 표준 애니메이션
- **로딩 상태**: 스켈레톤 UI 또는 프로그레스 바
- **상호작용**: 터치 피드백, 리플 효과

## 🖼️ 이미지 처리 기술

### 이미지 형식
- **입력**: JPEG, PNG, WebP 지원
- **출력**: JPEG (SNS 공유 최적화)
- **품질**: 80% 압축률로 용량과 품질 균형

### 이미지 크기
- **입력**: 최대 4096x4096 픽셀
- **출력**: 1080x1920 (인스타그램 스토리 최적화)
- **썸네일**: 200x200 픽셀

### 메모리 관리
- **Bitmap 재사용**: WeakReference 활용
- **이미지 스케일링**: 적절한 크기로 다운샘플링
- **가비지 컬렉션**: 메모리 누수 방지

## 🔐 보안 및 권한

### 권한 요청 방식
```kotlin
// Activity Result Contracts 사용
private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    handlePermissionResult(isGranted)
}
```

### 데이터 보안
- **로컬 저장**: 기기 내부 저장소만 사용
- **네트워크 전송**: 민감한 데이터 전송 없음
- **권한 최소화**: 필요한 권한만 요청

## 🧪 테스트 환경

### 테스트 도구
- **단위 테스트**: JUnit 4
- **UI 테스트**: Compose Test
- **모킹**: Mockk
- **테스트 러너**: AndroidJUnitRunner

### 테스트 범위
- **ViewModel**: 비즈니스 로직 테스트
- **Repository**: 데이터 처리 테스트
- **UI 컴포넌트**: 사용자 인터페이스 테스트
- **통합 테스트**: 전체 플로우 테스트

## 📊 성능 모니터링

### 메트릭 수집
- **앱 시작 시간**: Cold/Warm/Hot 시작 시간
- **메모리 사용량**: 힙 메모리, 네이티브 메모리
- **CPU 사용률**: 백그라운드 처리 시간
- **배터리 영향**: 백그라운드 작업 최소화

### 최적화 전략
- **레이지 로딩**: 필요할 때만 리소스 로드
- **이미지 캐싱**: 메모리 및 디스크 캐시 활용
- **백그라운드 처리**: UI 블로킹 방지
- **리소스 정리**: 적절한 시점에 메모리 해제

## 🔄 배포 및 유지보수

### 빌드 변형
- **Debug**: 개발용, 로깅 활성화
- **Release**: 프로덕션용, 코드 난독화
- **Staging**: 테스트용, 프로덕션과 유사한 환경

### 버전 관리
- **시맨틱 버저닝**: MAJOR.MINOR.PATCH
- **현재 버전**: 1.0.0 (MVP)
- **다음 버전**: 1.1.0 (카메라 기능 추가)

### 업데이트 전략
- **자동 업데이트**: Google Play Store를 통한 자동 업데이트
- **점진적 배포**: 사용자 그룹별 단계적 배포
- **롤백 계획**: 문제 발생 시 이전 버전으로 복구
