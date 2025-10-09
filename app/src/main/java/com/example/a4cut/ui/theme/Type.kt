package com.example.a4cut.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Pretendard 또는 SUIT 같은 깔끔한 산세리프 폰트를 res/font 폴더에 추가하고 FontFamily를 정의하세요.
// val pretendard = FontFamily(
//     Font(R.font.pretendard_regular, FontWeight.Normal),
//     Font(R.font.pretendard_medium, FontWeight.Medium),
//     Font(R.font.pretendard_bold, FontWeight.Bold),
// )

// Instagram-style Typography 정의
// 인스타그램의 깔끔하고 읽기 쉬운 타이포그래피 시스템을 적용합니다.
val InstagramTypography = Typography(
    // 화면 타이틀 (예: "KTX 네컷")
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, // TODO: Instagram Proxima Nova 폰트로 교체
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = -0.5.sp
    ),
    // 섹션 제목 (예: "최근 4컷 사진")
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = -0.3.sp
    ),
    // 카드 제목 (예: "부산역 프레임")
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    // 본문 텍스트 (예: 설명, 메시지)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // 버튼 텍스트
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    // 작은 보조 텍스트 (예: 시간, 카운트)
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // 매우 작은 텍스트 (예: 캡션, 태그)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )
)

// 기존 Toss 스타일과의 호환성을 위한 별칭
val TossTypography = InstagramTypography

// 기존 호환성을 위한 Typography (점진적 마이그레이션용)
val Typography = TossTypography