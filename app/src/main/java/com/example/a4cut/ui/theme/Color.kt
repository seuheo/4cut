package com.example.a4cut.ui.theme

import androidx.compose.ui.graphics.Color

// Instagram-style Color Palette 정의
// 인스타그램의 미니멀하고 깔끔한 색상 시스템을 적용합니다.

// Primary Colors - KTX 브랜드 아이덴티티 유지
val KTXBlue = Color(0xFF1E3A8A) // KTX 시그니처 블루 (포인트 컬러로 사용)
val InstagramBlue = Color(0xFF0095F6) // 인스타그램 블루 (액센트 컬러)

// Background Colors - 인스타그램 스타일
val BackgroundLight = Color(0xFFFFFFFF) // 순수한 흰색 배경
val BackgroundSecondary = Color(0xFFFAFAFA) // 보조 배경 (그리드 구분선 등)
val BackgroundDark = Color(0xFF000000) // 다크 모드 배경

// Surface Colors
val SurfaceLight = Color(0xFFFFFFFF) // 카드 및 컴포넌트 배경
val SurfaceDark = Color(0xFF1A1A1A) // 다크 모드 카드 배경
val SurfaceElevated = Color(0xFFFFFFFF) // 상단 바, 모달 등

// Text Colors - 인스타그램 스타일
val TextPrimary = Color(0xFF262626) // 가장 중요한 텍스트 (인스타그램 스타일)
val TextSecondary = Color(0xFF8E8E8E) // 보조적인 텍스트
val TextTertiary = Color(0xFFC7C7CC) // 더 흐린 텍스트
val TextLink = Color(0xFF0095F6) // 링크 텍스트

// Border & Divider Colors
val BorderLight = Color(0xFFDBDBDB) // 경계선 색상
val BorderDark = Color(0xFF383838) // 다크 모드 경계선
val DividerLight = Color(0xFFEFEFEF) // 구분선 색상

// Interactive Colors
val LikeRed = Color(0xFFED4956) // 좋아요 빨간색
val StoryGradientStart = Color(0xFF833AB4) // 스토리 그라데이션 시작
val StoryGradientEnd = Color(0xFFE1306C) // 스토리 그라데이션 끝

// Status Colors
val SuccessGreen = Color(0xFF00C851) // 성공 상태
val WarningOrange = Color(0xFFFF8800) // 경고 상태
val ErrorRed = Color(0xFFFF4444) // 에러 상태

// 기존 Material Design 호환성을 위한 색상들 (점진적 마이그레이션용)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)