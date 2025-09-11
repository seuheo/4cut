package com.example.a4cut.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 4컷 사진 그리드 컴포넌트
 * 2x2 세로형 레이아웃으로 고정, 각 셀은 1:1 정사각형 비율
 */
@Composable
fun PhotoGrid(
    modifier: Modifier = Modifier,
    photos: List<Bitmap?>,
    onPhotoClick: (Int) -> Unit,
    onAddPhotoClick: () -> Unit,
    testPhotos: List<Bitmap> = emptyList(),
    onTestPhotoClick: (Int, Int) -> Unit = { _, _ -> },
    onRandomPhotoClick: () -> Unit = { }
) {
    // 현재 선택된 그리드 위치 상태
    var selectedGridIndex by remember { mutableStateOf(-1) }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 제목
        Text(
            text = "4컷 사진 선택",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 2x2 그리드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 첫 번째 행 (1, 2번 사진)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhotoCell(
                    photo = photos.getOrNull(0),
                    index = 0,
                    onClick = { index ->
                        selectedGridIndex = index
                        onPhotoClick(index)
                    },
                    isSelected = selectedGridIndex == 0,
                    modifier = Modifier.weight(1f)
                )
                PhotoCell(
                    photo = photos.getOrNull(1),
                    index = 1,
                    onClick = { index ->
                        selectedGridIndex = index
                        onPhotoClick(index)
                    },
                    isSelected = selectedGridIndex == 1,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // 두 번째 행 (3, 4번 사진)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhotoCell(
                    photo = photos.getOrNull(2),
                    index = 2,
                    onClick = { index ->
                        selectedGridIndex = index
                        onPhotoClick(index)
                    },
                    isSelected = selectedGridIndex == 2,
                    modifier = Modifier.weight(1f)
                )
                PhotoCell(
                    photo = photos.getOrNull(3),
                    index = 3,
                    onClick = { index ->
                        selectedGridIndex = index
                        onPhotoClick(index)
                    },
                    isSelected = selectedGridIndex == 3,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 테스트용 사진 선택 영역
        if (testPhotos.isNotEmpty()) {
            TestPhotoSelector(
                testPhotos = testPhotos,
                selectedGridIndex = selectedGridIndex,
                onTestPhotoClick = onTestPhotoClick,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 사진 추가 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 랜덤 사진 추가 버튼 (항상 표시)
            RandomPhotoButton(
                onClick = onRandomPhotoClick,
                modifier = Modifier.weight(1f)
            )
            
            // 갤러리 사진 추가 버튼
            AddPhotoButton(
                onClick = onAddPhotoClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 개별 사진 셀 컴포넌트
 */
@Composable
private fun PhotoCell(
    photo: Bitmap?,
    index: Int,
    onClick: (Int) -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // 1:1 정사각형 비율
            .clickable { onClick(index) }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            if (photo != null) {
                // 선택된 사진 표시
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = "선택된 사진 ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // 사진 번호 표시 (우상단)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // 빈 셀 (사진 추가 안내)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "사진 추가",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "사진 ${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 사진 추가 버튼
 */
@Composable
private fun AddPhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "사진 추가",
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "갤러리에서 사진 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

/**
 * 테스트용 사진 선택 컴포넌트
 */
@Composable
private fun TestPhotoSelector(
    testPhotos: List<Bitmap>,
    selectedGridIndex: Int,
    onTestPhotoClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "테스트용 사진 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = if (selectedGridIndex != -1) {
                "위치 ${selectedGridIndex + 1}에 사진을 선택하세요"
            } else {
                "4컷 그리드의 빈 자리를 클릭한 후 아래 사진을 선택하세요"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(testPhotos) { index, bitmap ->
                TestPhotoItem(
                    bitmap = bitmap,
                    index = index,
                    onClick = { testPhotoIndex ->
                        // 현재 선택된 그리드 위치에 테스트 사진 할당
                        if (selectedGridIndex != -1) {
                            onTestPhotoClick(selectedGridIndex, testPhotoIndex)
                        }
                    }
                )
            }
        }
    }
}

/**
 * 테스트용 사진 개별 아이템
 */
@Composable
private fun TestPhotoItem(
    bitmap: Bitmap,
    index: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(80.dp)
            .clickable { onClick(index) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "테스트 사진 ${index + 1}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * 랜덤 사진 추가 버튼
 */
@Composable
private fun RandomPhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "랜덤 사진 추가",
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(6.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "랜덤 사진 추가",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

