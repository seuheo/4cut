package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.a4cut.ui.components.PhotoLogCard
import com.example.a4cut.ui.viewmodel.FilterOptions
import com.example.a4cut.ui.viewmodel.SearchViewModel
import com.example.a4cut.ui.viewmodel.SortOption

/**
 * 검색 및 필터링 기능을 제공하는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPhotoDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val selectedSeasons by viewModel.selectedSeasons.collectAsState()
    val selectedMoods by viewModel.selectedMoods.collectAsState()
    val selectedWeather by viewModel.selectedWeather.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    val focusRequester = remember { FocusRequester() }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // TopAppBar
        TopAppBar(
            title = { Text("검색") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            actions = {
                // 필터 초기화 버튼
                if (selectedSeasons.isNotEmpty() || selectedMoods.isNotEmpty() || selectedWeather.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("필터 초기화")
                    }
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 검색 바
            item {
                SearchBar(
                    viewModel = viewModel,
                    focusRequester = focusRequester
                )
            }
            
            // 검색 히스토리 (검색어가 없을 때만 표시)
            if (searchQuery.isEmpty() && searchHistory.isNotEmpty()) {
                item {
                    SearchHistorySection(
                        searchHistory = searchHistory,
                        onSelectHistory = { viewModel.selectFromHistory(it) }
                    )
                }
            }
            
            // 필터 섹션
            item {
                FilterSection(
                    selectedSeasons = selectedSeasons,
                    selectedMoods = selectedMoods,
                    selectedWeather = selectedWeather,
                    onSeasonToggle = { viewModel.toggleSeason(it) },
                    onMoodToggle = { viewModel.toggleMood(it) },
                    onWeatherToggle = { viewModel.toggleWeather(it) }
                )
            }
            
            // 정렬 옵션
            item {
                SortSection(
                    currentSort = sortBy,
                    onSortChange = { viewModel.updateSortOption(it) }
                )
            }
            
            // 검색 결과
            if (searchResults.isNotEmpty()) {
                item {
                    Text(
                        text = "검색 결과 (${searchResults.size}개)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                items(searchResults) { photo ->
                    PhotoLogCard(
                        photo = photo,
                        onFavoriteToggle = { /* TODO: 즐겨찾기 토글 구현 */ },
                        onCardClick = { onNavigateToPhotoDetail(photo.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (searchQuery.isNotEmpty() && !isLoading) {
                item {
                    EmptySearchResult()
                }
            }
        }
        
        // 로딩 인디케이터
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // 에러 메시지
        errorMessage?.let { error ->
            LaunchedEffect(error) {
                // 3초 후 에러 메시지 자동 제거
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
            
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("확인")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
    
    // 화면 진입 시 검색창에 포커스
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

/**
 * 검색 바 컴포넌트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    viewModel: SearchViewModel,
    focusRequester: FocusRequester
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.updateSearchQuery(it) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { Text("제목, 태그, 설명으로 검색") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "검색어 지우기")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { viewModel.performSearch() }
        ),
        singleLine = true
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    Button(
        onClick = { viewModel.performSearch() },
        modifier = Modifier.fillMaxWidth(),
        enabled = searchQuery.isNotEmpty()
    ) {
        Icon(Icons.Default.Search, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("검색")
    }
}

/**
 * 검색 히스토리 섹션
 */
@Composable
private fun SearchHistorySection(
    searchHistory: List<String>,
    onSelectHistory: (String) -> Unit
) {
    Column {
        Text(
            text = "최근 검색어",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                            items(searchHistory) { query ->
                    FilterChip(
                        // ✅ 수정된 부분: 'selected' 파라미터를 추가했습니다.
                        selected = false,
                        onClick = { onSelectHistory(query) },
                        label = { Text(query) }
                    )
                }
        }
    }
}

/**
 * 필터 섹션
 */
@Composable
private fun FilterSection(
    selectedSeasons: Set<String>,
    selectedMoods: Set<String>,
    selectedWeather: Set<String>,
    onSeasonToggle: (String) -> Unit,
    onMoodToggle: (String) -> Unit,
    onWeatherToggle: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 계절 필터
        FilterChipGroup(
            title = "계절",
            options = FilterOptions.seasons,
            selectedOptions = selectedSeasons,
            onOptionToggle = onSeasonToggle
        )
        
        // 감정 필터
        FilterChipGroup(
            title = "감정",
            options = FilterOptions.moods,
            selectedOptions = selectedMoods,
            onOptionToggle = onMoodToggle
        )
        
        // 날씨 필터
        FilterChipGroup(
            title = "날씨",
            options = FilterOptions.weather,
            selectedOptions = selectedWeather,
            onOptionToggle = onWeatherToggle
        )
    }
}

/**
 * 필터 칩 그룹
 */
@Composable
private fun FilterChipGroup(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionToggle: (String) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(options) { option ->
                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = { onOptionToggle(option) },
                    label = { Text(option) }
                )
            }
        }
    }
}

/**
 * 정렬 섹션
 */
@Composable
private fun SortSection(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    Column {
        Text(
            text = "정렬",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SortOption.values()) { sortOption ->
                FilterChip(
                    selected = currentSort == sortOption,
                    onClick = { onSortChange(sortOption) },
                    label = { Text(sortOption.displayName) }
                )
            }
        }
    }
}

/**
 * 빈 검색 결과
 */
@Composable
private fun EmptySearchResult() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "검색 결과가 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "다른 검색어나 필터를 시도해보세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
