package com.example.a4cut.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a4cut.data.model.KtxStation
import android.util.Log

@Composable
fun KtxStationSelector(
    stations: List<KtxStation>,
    selectedStation: String?,
    onStationSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stations) { station ->
            val isSelected = station.name == selectedStation
            FilterChip(
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        Log.d("KtxStationSelector", "역 선택 해제: ${station.name}")
                        onStationSelected(null) // 다시 누르면 선택 해제
                    } else {
                        Log.d("KtxStationSelector", "역 선택: ${station.name}")
                        onStationSelected(station.name)
                    }
                },
                label = { Text(station.name) }
            )
        }
    }
}