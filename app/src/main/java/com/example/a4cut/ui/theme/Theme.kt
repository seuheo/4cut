package com.example.a4cut.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// iOS 스타일 Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = IosColors.SystemBlue,
    onPrimary = IosColors.White,
    secondary = IosColors.SystemBlue, // KTX 브랜드 아이덴티티 유지
    onSecondary = IosColors.White,
    background = IosColors.systemBackground,
    onBackground = IosColors.label,
    surface = IosColors.systemBackground,
    onSurface = IosColors.label,
    surfaceVariant = IosColors.secondarySystemBackground,
    onSurfaceVariant = IosColors.secondaryLabel,
    outline = IosColors.systemGray4,
    outlineVariant = IosColors.separator,
    error = IosColors.SystemRed,
    onError = IosColors.White,
    errorContainer = IosColors.SystemRed.copy(alpha = 0.1f),
    onErrorContainer = IosColors.SystemRed,
    tertiary = IosColors.SystemRed,
    onTertiary = IosColors.White,
    tertiaryContainer = IosColors.SystemRed.copy(alpha = 0.1f),
    onTertiaryContainer = IosColors.SystemRed,
)

// iOS 스타일 Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = IosColors.SystemBlue,
    onPrimary = IosColors.Black,
    secondary = IosColors.SystemBlue,
    onSecondary = IosColors.White,
    background = IosColors.Black,
    onBackground = IosColors.White,
    surface = IosColors.Black,
    onSurface = IosColors.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF404040),
    error = IosColors.SystemRed,
    onError = IosColors.White,
    errorContainer = IosColors.SystemRed.copy(alpha = 0.2f),
    onErrorContainer = IosColors.SystemRed,
    tertiary = IosColors.SystemRed,
    onTertiary = IosColors.White,
    tertiaryContainer = IosColors.SystemRed.copy(alpha = 0.2f),
    onTertiaryContainer = IosColors.SystemRed,
)

@Composable
fun A4CutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 현재 디자인 시스템과 충돌할 수 있으므로 비활성화합니다.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InstagramTypography,
        content = content
    )
}

// 기존 호환성을 위한 함수 (점진적 마이그레이션용)
@Composable
fun _4cutTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    A4CutTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}