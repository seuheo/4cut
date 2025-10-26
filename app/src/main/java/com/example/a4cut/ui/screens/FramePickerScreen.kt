package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.a4cut.data.model.Frame
import com.example.a4cut.data.model.FrameFormat
import com.example.a4cut.ui.viewmodel.FrameViewModel

/**
 * 프레임 선택 화면
 * 포맷 선택 -> 해당 포맷의 프레임 목록 -> 최종 프레임 선택
 */
@Composable
fun FramePickerScreen(
    navController: NavController,
    viewModel: FrameViewModel
) {
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val framesByFormat by viewModel.framesByFormat.collectAsState()
    val currentlySelectedFrame by viewModel.selectedFrame.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- 1. 프레임 포맷 선택 UI ---
        Text(
            text = "프레임 포맷 선택",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.selectFormat(FrameFormat.STANDARD) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFormat == FrameFormat.STANDARD)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) { 
                Text("일반 프레임") 
            }

            Button(
                onClick = { viewModel.selectFormat(FrameFormat.LONG_FORM) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFormat == FrameFormat.LONG_FORM)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) { 
                Text("Long Form 프레임") 
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. 선택된 포맷의 프레임 목록 표시 ---
        Text(
            text = "프레임 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 프레임 선택 UI
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(framesByFormat) { frame ->
                FrameSelectionCard(
                    frame = frame,
                    isSelected = currentlySelectedFrame?.id == frame.id,
                    onClick = {
                        // 프레임 선택 시 ViewModel 상태 업데이트
                        viewModel.selectFrame(frame)
                        // 이전 화면(FrameScreen)으로 돌아가기
                        navController.popBackStack()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. 취소 버튼 ---
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text("선택 취소")
        }
    }
}

/**
 * 프레임 선택 카드
 */
@Composable
private fun FrameSelectionCard(
    frame: Frame,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = frame.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
