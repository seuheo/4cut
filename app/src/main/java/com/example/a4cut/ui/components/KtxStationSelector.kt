package com.example.a4cut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a4cut.data.model.KtxLine
import com.example.a4cut.data.model.KtxStation
import com.example.a4cut.data.model.KtxStationGroup
import com.example.a4cut.data.repository.KtxStationRepository

/**
 * KTX역 선택 컴포넌트
 * 호남선과 경부선의 KTX역을 선택할 수 있는 UI
 */
@Composable
fun KtxStationSelector(
    selectedStation: KtxStation?,
    onStationSelected: (KtxStation) -> Unit,
    modifier: Modifier = Modifier,
    repository: KtxStationRepository = remember { KtxStationRepository() }
) {
    val stationGroups by repository.stationGroups.collectAsState(initial = emptyList())
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 제목
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "KTX",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "KTX역 선택",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
        
        // 노선별 KTX역 목록
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stationGroups) { group ->
                KtxStationGroupCard(
                    group = group,
                    selectedStation = selectedStation,
                    onStationSelected = onStationSelected
                )
            }
        }
    }
}

/**
 * 노선별 KTX역 그룹 카드
 */
@Composable
private fun KtxStationGroupCard(
    group: KtxStationGroup,
    selectedStation: KtxStation?,
    onStationSelected: (KtxStation) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 노선명
            Text(
                text = group.line.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = when (group.line) {
                    KtxLine.HONAM -> Color(0xFF4CAF50)
                    KtxLine.GYEONGBU -> Color(0xFF2196F3)
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // KTX역 목록
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                group.stations.forEach { station ->
                    KtxStationChip(
                        station = station,
                        isSelected = selectedStation?.id == station.id,
                        onClick = { onStationSelected(station) }
                    )
                }
            }
        }
    }
}

/**
 * 개별 KTX역 칩
 */
@Composable
private fun KtxStationChip(
    station: KtxStation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    when (station.line) {
                        KtxLine.HONAM -> Color(0xFF4CAF50)
                        KtxLine.GYEONGBU -> Color(0xFF2196F3)
                    }
                } else {
                    Color(0xFFF5F5F5)
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) {
                    when (station.line) {
                        KtxLine.HONAM -> Color(0xFF388E3C)
                        KtxLine.GYEONGBU -> Color(0xFF1976D2)
                    }
                } else {
                    Color(0xFFE0E0E0)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = station.name,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color(0xFF424242),
            textAlign = TextAlign.Center
        )
    }
}
