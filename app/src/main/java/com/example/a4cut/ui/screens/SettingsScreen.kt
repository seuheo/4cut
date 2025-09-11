package com.example.a4cut.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.viewmodel.HomeViewModel

/**
 * 설정 화면
 * 앱 정보, 테마 설정, 알림 설정 등 기본적인 설정 기능 제공
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    
    // 설정 상태 관리
    var isDarkMode by remember { mutableStateOf(false) }
    var isSystemTheme by remember { mutableStateOf(true) }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isSaveNotificationEnabled by remember { mutableStateOf(true) }
    
    // ViewModel 초기화
    LaunchedEffect(Unit) {
        try {
            homeViewModel.setContext(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // 사진 개수 상태
    val latestPhotos by homeViewModel.latestPhotos.collectAsState()
    val totalPhotos = latestPhotos.size
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "설정", 
                        fontWeight = FontWeight.Bold
                    ) 
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // 앱 정보 섹션
            SettingsSection(
                title = "앱 정보",
                icon = Icons.Default.Info
            ) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "앱 버전",
                    subtitle = "1.0.0 (MVP)",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "개발자",
                    subtitle = "KTX 네컷 팀",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "오픈소스 라이선스",
                    subtitle = "MIT License",
                    onClick = { }
                )
            }
            
            // 테마 설정 섹션
            SettingsSection(
                title = "테마 설정",
                icon = Icons.Default.Settings
            ) {
                SettingsSwitchItem(
                    icon = Icons.Default.Phone,
                    title = "시스템 테마 따르기",
                    subtitle = "시스템 설정에 따라 자동 변경",
                    checked = isSystemTheme,
                    onCheckedChange = { isSystemTheme = it }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Settings,
                    title = "다크 모드",
                    subtitle = "어두운 테마 사용",
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it },
                    enabled = !isSystemTheme
                )
            }
            
            // 알림 설정 섹션
            SettingsSection(
                title = "알림 설정",
                icon = Icons.Default.Notifications
            ) {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "푸시 알림",
                    subtitle = "앱 업데이트 및 새로운 기능 알림",
                    checked = isNotificationEnabled,
                    onCheckedChange = { isNotificationEnabled = it }
                )
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "저장 완료 알림",
                    subtitle = "사진 저장이 완료되면 알림",
                    checked = isSaveNotificationEnabled,
                    onCheckedChange = { isSaveNotificationEnabled = it }
                )
            }
            
            // 데이터 관리 섹션
            SettingsSection(
                title = "데이터 관리",
                icon = Icons.Default.Settings
            ) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "저장된 사진",
                    subtitle = "${totalPhotos}장",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "캐시 정리",
                    subtitle = "임시 파일 삭제",
                    onClick = { 
                        // TODO: 캐시 정리 기능 구현
                    }
                )
            }
            
            // 도움말 섹션
            SettingsSection(
                title = "도움말",
                icon = Icons.Default.Info
            ) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "자주 묻는 질문",
                    subtitle = "FAQ",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "문의하기",
                    subtitle = "개발팀에 연락",
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "앱 평가하기",
                    subtitle = "Google Play Store",
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 설정 섹션 컴포넌트
 */
@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

/**
 * 설정 아이템 컴포넌트 (클릭 가능)
 */
@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "이동",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 설정 스위치 아이템 컴포넌트
 */
@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
