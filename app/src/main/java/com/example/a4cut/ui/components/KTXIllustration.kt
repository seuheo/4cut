package com.example.a4cut.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * KTX 기차 일러스트 컴포넌트
 * Canvas API를 사용하여 KTX 기차를 그립니다
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
        
        // KTX 브랜드 컬러
        val ktxBlue = Color(0xFF1E3A8A)
        val ktxSilver = Color(0xFFE5E7EB)
        val ktxOrange = Color(0xFFF59E0B)
        
        // 기차 본체 (파란색)
        drawRect(
            color = ktxBlue,
            topLeft = Offset(width * 0.1f, height * 0.3f),
            size = androidx.compose.ui.geometry.Size(width * 0.8f, height * 0.4f)
        )
        
        // 기차 창문들 (실버)
        val windowWidth = width * 0.15f
        val windowHeight = height * 0.2f
        val windowSpacing = width * 0.05f
        
        for (i in 0..3) {
            val x = width * 0.15f + i * (windowWidth + windowSpacing)
            drawRect(
                color = ktxSilver,
                topLeft = Offset(x, height * 0.35f),
                size = androidx.compose.ui.geometry.Size(windowWidth, windowHeight)
            )
        }
        
        // 기차 앞부분 (오렌지)
        drawRect(
            color = ktxOrange,
            topLeft = Offset(width * 0.05f, height * 0.35f),
            size = androidx.compose.ui.geometry.Size(width * 0.15f, height * 0.3f)
        )
        
        // 기차 바퀴들
        val wheelRadius = height * 0.08f
        val wheelY = height * 0.7f
        
        for (i in 0..3) {
            val x = width * 0.2f + i * (width * 0.2f)
            drawCircle(
                color = ktxSilver,
                radius = wheelRadius,
                center = Offset(x, wheelY)
            )
        }
        
        // KTX 로고 텍스트 (간단한 표현)
        drawCircle(
            color = Color.White,
            radius = height * 0.04f,
            center = Offset(width * 0.5f, height * 0.5f)
        )
    }
}
