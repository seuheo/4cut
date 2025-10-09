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

// 토스 스타일의 명확한 타이포그래피 계층 구조를 정의합니다.
val TossTypography = Typography(
    // 예: "나의 포토로그"와 같은 화면 타이틀
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, // TODO: Custom Font로 교체
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    // 예: "KTX와 함께한 N개의 추억"
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // 예: 카드 내 제목
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // 예: 일반적인 본문 텍스트
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // 예: 버튼 텍스트
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // 예: 작은 보조 텍스트
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

// 기존 호환성을 위한 Typography (점진적 마이그레이션용)
val Typography = TossTypography