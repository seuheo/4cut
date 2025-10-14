package com.example.a4cut.ui.theme

import androidx.compose.ui.graphics.Color

// iOS 스타일 색상 팔레트 정의
// 20대 사용자들이 선호하는 세련되고 깔끔한 미니멀리즘 디자인

// iOS 시스템 색상
object IosColors {
    // 기본 색상
    val White = Color.White
    val Black = Color.Black
    val SystemBlue = Color(0xFF007AFF) // iOS 시스템 블루
    val SystemGreen = Color(0xFF34C759) // iOS 시스템 그린
    val SystemRed = Color(0xFFFF3B30) // iOS 시스템 레드
    val SystemOrange = Color(0xFFFF9500) // iOS 시스템 오렌지
    
    // 배경색
    val systemBackground = Color(0xFFFFFFFF) // 기본 배경
    val secondarySystemBackground = Color(0xFFF2F2F7) // 그룹화된 컴포넌트 배경
    val tertiarySystemBackground = Color(0xFFFFFFFF) // 3차 배경
    
    // 텍스트 색상
    val label = Color(0xFF000000) // 기본 텍스트
    val secondaryLabel = Color(0x993C3C43) // 보조 텍스트
    val tertiaryLabel = Color(0x4D3C3C43) // 3차 텍스트
    val quaternaryLabel = Color(0x2D3C3C43) // 4차 텍스트
    
    // 구분선
    val separator = Color(0x33C6C6C8) // 기본 구분선
    val opaqueSeparator = Color(0xFFC6C6C8) // 불투명 구분선
    
    // 회색 계열
    val systemGray = Color(0xFF8E8E93) // 시스템 그레이
    val systemGray2 = Color(0xFFAEAEB2) // 시스템 그레이 2
    val systemGray3 = Color(0xFFC7C7CC) // 시스템 그레이 3
    val systemGray4 = Color(0xFFD1D1D6) // 시스템 그레이 4
    val systemGray5 = Color(0xFFE5E5EA) // 시스템 그레이 5
    val systemGray6 = Color(0xFFF2F2F7) // 시스템 그레이 6
    
    // 그룹화된 배경
    val groupedBackground = Color(0xFFF2F2F7) // 그룹화된 배경
    val secondaryGroupedBackground = Color(0xFFFFFFFF) // 보조 그룹화된 배경
    val tertiaryGroupedBackground = Color(0xFFF2F2F7) // 3차 그룹화된 배경
    
    // 채우기 색상
    val systemFill = Color(0x78787880) // 시스템 채우기
    val secondarySystemFill = Color(0x52787880) // 보조 시스템 채우기
    val tertiarySystemFill = Color(0x3D787880) // 3차 시스템 채우기
    val quaternarySystemFill = Color(0x2D787880) // 4차 시스템 채우기
}

// 기존 KTX 브랜드 색상 (호환성 유지)
val KTXBlue = IosColors.SystemBlue // iOS 시스템 블루로 통일

// 기존 Material Design 호환성을 위한 색상들 (점진적 마이그레이션용)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)