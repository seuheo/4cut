package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 프레임 화면
 * 4컷 사진 선택 + 프레임 적용 + 미리보기
 */
@Composable
fun FrameScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "4컷 사진을 선택하고 KTX 프레임을 적용하세요",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // TODO: 4컷 사진 선택 그리드 구현
        Text(
            text = "4컷 사진 선택 그리드가 여기에 표시됩니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // TODO: 프레임 미리보기 구현
        Text(
            text = "프레임 미리보기가 여기에 표시됩니다",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = { /* TODO: 사진 선택 로직 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("사진 선택")
        }
        
        Button(
            onClick = { /* TODO: 프레임 적용 로직 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("프레임 적용")
        }
    }
}
