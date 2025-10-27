package com.example.a4cut.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.a4cut.ui.viewmodel.CampaignViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * ✅ MVP Ver2: 노선도(잇다) 캠페인 화면
 * 사용자가 방문한 KTX 역을 추적하고, 완주 여부를 확인하는 게이미피케이션 기능
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignScreen(
    viewModel: CampaignViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    // ViewModel 상태 수집
    val selectedYear by viewModel.selectedYear.collectAsState()
    val visitedStations by viewModel.visitedStationsInYear.collectAsState()
    val gyeongbuLineStatus by viewModel.gyeongbuLineStatus.collectAsState()
    val honamLineStatus by viewModel.honamLineStatus.collectAsState()
    val isGyeongbuComplete by viewModel.isGyeongbuComplete.collectAsState()
    val isHonamComplete by viewModel.isHonamComplete.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("노선도(잇다)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 연도 선택기
            item {
                Text(
                    text = "연도 선택",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("2023", "2024", "2025", "2026").forEach { year ->
                        FilterChip(
                            selected = selectedYear == year,
                            onClick = { viewModel.selectYear(year) },
                            label = { Text(year) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // 완주 배지
            if (isGyeongbuComplete || isHonamComplete) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎉",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "완주 달성!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (isGyeongbuComplete) {
                                    Text(
                                        text = "경부선 완주",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                if (isHonamComplete) {
                                    Text(
                                        text = "호남선 완주",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 경부선 상태
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGyeongbuComplete) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "경부선 ${gyeongbuLineStatus.count { it.second }}/${gyeongbuLineStatus.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // 경부선 역 목록
                        gyeongbuLineStatus.forEach { (stationName, isVisited) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isVisited) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "방문함",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "○",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                }
                                Text(
                                    text = stationName,
                                    color = if (isVisited) 
                                        MaterialTheme.colorScheme.onSurface 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                }
            }
            
            // 호남선 상태
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHonamComplete) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "호남선 ${honamLineStatus.count { it.second }}/${honamLineStatus.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // 호남선 역 목록
                        honamLineStatus.forEach { (stationName, isVisited) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isVisited) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "방문함",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "○",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    )
                                }
                                Text(
                                    text = stationName,
                                    color = if (isVisited) 
                                        MaterialTheme.colorScheme.onSurface 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

