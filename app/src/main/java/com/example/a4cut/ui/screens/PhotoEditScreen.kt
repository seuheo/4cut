package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.a4cut.ui.viewmodel.EditData
import com.example.a4cut.ui.viewmodel.PhotoDetailViewModel

/**
 * 사진 편집 화면
 * 제목, 설명, 태그를 편집할 수 있는 별도 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditScreen(
    viewModel: PhotoDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val editData by viewModel.editData.collectAsState()

    
    // 로컬 편집 상태
    var localTitle by remember { mutableStateOf(editData.title) }
    var localDescription by remember { mutableStateOf(editData.description) }
    var localTags by remember { mutableStateOf(editData.tags) }
    
    // 편집 데이터가 변경될 때마다 로컬 상태 업데이트
    LaunchedEffect(editData) {
        localTitle = editData.title
        localDescription = editData.description
        localTags = editData.tags
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("사진 편집") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    // 저장 버튼
                    IconButton(
                        onClick = {
                            viewModel.updateEditData(
                                title = localTitle,
                                description = localDescription,
                                tags = localTags
                            )
                            viewModel.saveEditData()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "저장",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 제목 입력 필드
            OutlinedTextField(
                value = localTitle,
                onValueChange = { localTitle = it },
                label = { Text("제목") },
                placeholder = { Text("사진의 제목을 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // 설명 입력 필드
            OutlinedTextField(
                value = localDescription,
                onValueChange = { localDescription = it },
                label = { Text("설명") },
                placeholder = { Text("사진에 대한 설명을 입력하세요") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // 태그 입력 필드
            OutlinedTextField(
                value = localTags,
                onValueChange = { localTags = it },
                label = { Text("태그") },
                placeholder = { Text("쉼표로 구분하여 태그를 입력하세요 (예: 여행, KTX, 친구)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // 편집 가이드
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "편집 가이드",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 제목: 사진을 대표하는 간단한 제목을 입력하세요\n" +
                                "• 설명: 사진의 배경이나 특별한 순간에 대해 설명하세요\n" +
                                "• 태그: 쉼표로 구분하여 관련 키워드를 입력하세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 하단 여백
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
