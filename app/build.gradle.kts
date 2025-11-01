plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
}

android {
    namespace = "com.example.a4cut"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.a4cut"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    
    // Lifecycle ViewModel Compose (직접 버전 지정)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0-rc01")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Gson (JSON 직렬화/역직렬화)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // DataStore (설정 및 데이터 영구 저장)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Google Play 서비스: 위치 정보 (GPS) - GPS 좌표 획득용으로 유지
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Google Play 서비스: 장소 (주변 KTX 역 검색용)
    implementation("com.google.android.libraries.places:places:3.1.0")
    
    // OpenStreetMap (osmdroid) - Google Maps 대체
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // FFmpeg 래퍼 라이브러리 (동영상 슬라이드쇼 생성용)
    // FFmpegKit이 2025년 1월 6일부로 공식 은퇴하여 대체 라이브러리 사용
    // ws.schild:jave-core는 FFmpeg를 Java/Kotlin에서 사용할 수 있게 해주는 래퍼
    // 참고: FFmpegKit은 com.arthenica:ffmpeg-kit-* 패키지가 더 이상 Maven Central에 없음
    // TODO: jave-core 또는 다른 FFmpeg 래퍼 라이브러리 확인 필요
    // 일단 주석 처리 (실제 라이브러리 선택 후 활성화)
    // implementation("ws.schild:jave-core:3.4.0")
    
    // Core Library Desugaring (Java 8+ API 지원)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    
    // Accompanist Pager 제거 - LazyRow로 대체
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}