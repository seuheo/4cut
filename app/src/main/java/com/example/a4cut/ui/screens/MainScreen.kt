package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.viewmodel.MainViewModel

/**
 * 메인 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KTX 네컷 앱",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 4컷 사진 그리드
            PhotoGrid(
                photos = uiState.photos,
                onPhotoClick = { index ->
                    // TODO: 사진 선택 로직 구현
                },
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 액션 버튼들
            ActionButtons(
                isSaving = uiState.isSaving,
                isSharing = uiState.isSharing,
                onSaveClick = { viewModel.savePhotos() },
                onShareClick = { viewModel.shareToInstagram() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 액션 버튼들 (저장, 공유)
 */
@Composable
private fun ActionButtons(
    isSaving: Boolean,
    isSharing: Boolean,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onSaveClick,
                enabled = !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isSaving) "저장 중..." else "저장",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Button(
                onClick = onShareClick,
                enabled = !isSharing,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isSharing) "공유 중..." else "인스타그램 공유",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
