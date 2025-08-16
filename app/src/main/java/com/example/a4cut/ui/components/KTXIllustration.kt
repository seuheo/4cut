package com.example.a4cut.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

/**
 * KTX 기차 일러스트 컴포넌트
 * Canvas API를 사용하여 KTX 기차를 그립니다
 * Phase 2: 그라데이션, 반사 효과, 그림자 등 고급 효과 추가
 */
@Composable
fun KTXIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        
        // KTX 브랜드 컬러 (Material Design 3 기준)
        val ktxBlue = Color(0xFF1E3A8A)
        val ktxBlueLight = Color(0xFF3B82F6)
        val ktxSilver = Color(0xFFE5E7EB)
        val ktxSilverLight = Color(0xFFF3F4F6)
        val ktxOrange = Color(0xFFF59E0B)
        val ktxOrangeLight = Color(0xFFFBBF24)
        val ktxDark = Color(0xFF111827)
        
        // 그라데이션 브러시들
        val trainBodyGradient = Brush.linearGradient(
            colors = listOf(ktxBlue, ktxBlueLight),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )
        
        val trainFrontGradient = Brush.linearGradient(
            colors = listOf(ktxOrange, ktxOrangeLight),
            start = Offset(0f, 0f),
            end = Offset(width, height)
        )
        
        val windowGradient = Brush.radialGradient(
            colors = listOf(ktxSilverLight, ktxSilver),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = width * 0.1f
        )
        
        // 배경 그라데이션 (KTX 브랜드 느낌)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFE2E8F0),
                    Color(0xFFCBD5E1)
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.8f
            ),
            topLeft = Offset(0f, 0f),
            size = Size(width, height)
        )
        
        // 기차 그림자 (깊이감 표현)
        drawRect(
            color = ktxDark.copy(alpha = 0.1f),
            topLeft = Offset(width * 0.12f, height * 0.72f),
            size = Size(width * 0.76f, height * 0.08f)
        )
        
        // 기차 본체 (파란색 그라데이션)
        drawRect(
            brush = trainBodyGradient,
            topLeft = Offset(width * 0.1f, height * 0.3f),
            size = Size(width * 0.8f, height * 0.4f)
        )
        
        // 기차 본체 테두리 (깊이감)
        drawRect(
            color = ktxBlue.copy(alpha = 0.3f),
            topLeft = Offset(width * 0.1f, height * 0.3f),
            size = Size(width * 0.8f, height * 0.4f),
            style = Stroke(width = 2f)
        )
        
        // 기차 창문들 (실버 그라데이션 + 반사 효과)
        val windowWidth = width * 0.15f
        val windowHeight = height * 0.2f
        val windowSpacing = width * 0.05f
        
        for (i in 0..3) {
            val x = width * 0.15f + i * (windowWidth + windowSpacing)
            val y = height * 0.35f
            
            // 창문 배경
            drawRect(
                brush = windowGradient,
                topLeft = Offset(x, y),
                size = Size(windowWidth, windowHeight)
            )
            
            // 창문 테두리
            drawRect(
                color = ktxSilver.copy(alpha = 0.5f),
                topLeft = Offset(x, y),
                size = Size(windowWidth, windowHeight),
                style = Stroke(width = 1f)
            )
            
            // 창문 반사 효과 (빛나는 느낌)
            drawRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(x + 2f, y + 2f),
                size = Size(windowWidth * 0.3f, windowHeight * 0.4f)
            )
        }
        
        // 기차 앞부분 (오렌지 그라데이션)
        drawRect(
            brush = trainFrontGradient,
            topLeft = Offset(width * 0.05f, height * 0.35f),
            size = Size(width * 0.15f, height * 0.3f)
        )
        
        // 기차 앞부분 테두리
        drawRect(
            color = ktxOrange.copy(alpha = 0.3f),
            topLeft = Offset(width * 0.05f, height * 0.35f),
            size = Size(width * 0.15f, height * 0.3f),
            style = Stroke(width = 2f)
        )
        
        // 기차 바퀴들 (그림자 + 하이라이트)
        val wheelRadius = height * 0.08f
        val wheelY = height * 0.7f
        
        for (i in 0..3) {
            val x = width * 0.2f + i * (width * 0.2f)
            
            // 바퀴 그림자
            drawCircle(
                color = ktxDark.copy(alpha = 0.2f),
                radius = wheelRadius + 2f,
                center = Offset(x + 2f, wheelY + 2f)
            )
            
            // 바퀴 본체
            drawCircle(
                color = ktxSilver,
                radius = wheelRadius,
                center = Offset(x, wheelY)
            )
            
            // 바퀴 테두리
            drawCircle(
                color = ktxDark.copy(alpha = 0.3f),
                radius = wheelRadius,
                center = Offset(x, wheelY),
                style = Stroke(width = 2f)
            )
            
            // 바퀴 하이라이트 (금속 느낌)
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = wheelRadius * 0.3f,
                center = Offset(x - wheelRadius * 0.3f, wheelY - wheelRadius * 0.3f)
            )
        }
        
        // KTX 로고 영역 (더 명확하고 매력적으로)
        val logoCenterX = width * 0.5f
        val logoCenterY = height * 0.5f
        val logoSize = height * 0.12f
        
        // 로고 배경 원
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ktxBlue, ktxBlueLight),
                center = Offset(logoCenterX, logoCenterY),
                radius = logoSize
            ),
            radius = logoSize,
            center = Offset(logoCenterX, logoCenterY)
        )
        
        // 로고 테두리
        drawCircle(
            color = Color.White,
            radius = logoSize,
            center = Offset(logoCenterX, logoCenterY),
            style = Stroke(width = 3f)
        )
        
        // KTX 텍스트 (간단한 기하학적 표현)
        // K 자
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX - logoSize * 0.3f, logoCenterY - logoSize * 0.4f),
            end = Offset(logoCenterX - logoSize * 0.3f, logoCenterY + logoSize * 0.4f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX - logoSize * 0.3f, logoCenterY),
            end = Offset(logoCenterX - logoSize * 0.1f, logoCenterY - logoSize * 0.2f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX - logoSize * 0.3f, logoCenterY),
            end = Offset(logoCenterX - logoSize * 0.1f, logoCenterY + logoSize * 0.2f),
            strokeWidth = 4f
        )
        
        // T 자
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX - logoSize * 0.05f, logoCenterY - logoSize * 0.4f),
            end = Offset(logoCenterX + logoSize * 0.05f, logoCenterY - logoSize * 0.4f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX, logoCenterY - logoSize * 0.4f),
            end = Offset(logoCenterX, logoCenterY + logoSize * 0.4f),
            strokeWidth = 4f
        )
        
        // X 자
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX + logoSize * 0.1f, logoCenterY - logoSize * 0.4f),
            end = Offset(logoCenterX + logoSize * 0.3f, logoCenterY + logoSize * 0.4f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.White,
            start = Offset(logoCenterX + logoSize * 0.1f, logoCenterY + logoSize * 0.4f),
            end = Offset(logoCenterX + logoSize * 0.3f, logoCenterY - logoSize * 0.4f),
            strokeWidth = 4f
        )
        
        // 로고 하이라이트 (빛나는 효과)
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = logoSize * 0.2f,
            center = Offset(logoCenterX - logoSize * 0.2f, logoCenterY - logoSize * 0.2f)
        )
        
        // 기차 연기 효과 (동적인 느낌)
        val smokeCenterX = width * 0.15f
        val smokeCenterY = height * 0.25f
        
        for (i in 0..2) {
            val smokeRadius = (height * 0.03f) * (1f - i * 0.2f)
            val smokeX = smokeCenterX + i * (width * 0.02f)
            val smokeY = smokeCenterY - i * (height * 0.02f)
            val smokeAlpha = 0.3f * (1f - i * 0.3f)
            
            drawCircle(
                color = ktxSilver.copy(alpha = smokeAlpha),
                radius = smokeRadius,
                center = Offset(smokeX, smokeY)
            )
        }
    }
}
