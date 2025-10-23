package com.example.a4cut.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a4cut.ui.theme.IosColors
import com.example.a4cut.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * iOS 스타일 프로필 화면
 * 20대 사용자들이 선호하는 세련되고 깔끔한 미니멀리즘 디자인
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val homeViewModel: HomeViewModel = viewModel()
    val context = LocalContext.current
    
    // ViewModel 초기화
    LaunchedEffect(Unit) {
        try {
            homeViewModel.setContext(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // 사진 데이터 상태
    val latestPhotos by homeViewModel.latestPhotos.collectAsState()
    val totalPhotos = latestPhotos.size
    
    // 에러 메시지 상태
    val errorMessage by homeViewModel.errorMessage.collectAsState()
    
    // 통계 계산
    val thisMonthPhotos = latestPhotos.count { photo ->
        val photoDate = LocalDate.ofEpochDay(photo.createdAt / (24 * 60 * 60 * 1000))
        val currentDate = LocalDate.now()
        photoDate.year == currentDate.year && photoDate.month == currentDate.month
    }
    
    val favoritePhotos = latestPhotos.count { it.isFavorite }
    val mostUsedFrame = "KTX 시그니처" // TODO: 실제 데이터에서 계산
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "프로필", 
                        fontWeight = FontWeight.Bold,
                        color = IosColors.label
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = IosColors.systemBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(IosColors.secondarySystemBackground)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // iOS 스타일 사용자 정보 섹션
            IOSUserInfoSection(
                userName = "KTX 여행자",
                totalPhotos = totalPhotos,
                joinDate = "2024년 12월"
            )
            
            // iOS 스타일 통계 섹션
            IOSStatisticsSection(
                thisMonthPhotos = thisMonthPhotos,
                favoritePhotos = favoritePhotos,
                mostUsedFrame = mostUsedFrame
            )
            
            // iOS 스타일 활동 기록 섹션
            IOSActivitySection(
                totalPhotos = totalPhotos,
                thisMonthPhotos = thisMonthPhotos
            )
            
            // 앱 정보 섹션
            AppInfoSection(totalPhotos = totalPhotos)
            
            // 에러 메시지 표시
            errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "오류",
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { homeViewModel.refreshData() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "새로고침",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // 테스트 데이터 추가 버튼 비활성화 (중복 저장 방지)
                            // IconButton(
                            //     onClick = { homeViewModel.addTestLocationData() }
                            // ) {
                            //     Icon(
                            //         imageVector = Icons.Default.Add,
                            //         contentDescription = "테스트 데이터 추가",
                            //         tint = MaterialTheme.colorScheme.onErrorContainer,
                            //         modifier = Modifier.size(20.dp)
                            //     )
                            // }
                            IconButton(
                                onClick = { homeViewModel.forceReinitializeDatabase(context) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "재초기화",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { homeViewModel.clearErrorMessage() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "닫기",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 사용자 정보 섹션
 */
@Composable
private fun UserInfoSection(
    userName: String,
    totalPhotos: Int,
    joinDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로필 아바타
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "프로필",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 사용자 이름
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 가입일
            Text(
                text = "가입일: $joinDate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 총 사진 개수
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StatItem(
                    value = totalPhotos.toString(),
                    label = "총 사진"
                )
            }
        }
    }
}

/**
 * 통계 섹션
 */
@Composable
private fun StatisticsSection(
    thisMonthPhotos: Int,
    favoritePhotos: Int,
    mostUsedFrame: String
) {
    ProfileSection(
        title = "통계",
        icon = Icons.Default.Info
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = thisMonthPhotos.toString(),
                label = "이번 달"
            )
            StatItem(
                value = favoritePhotos.toString(),
                label = "즐겨찾기"
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileItem(
            icon = Icons.Default.Info,
            title = "가장 많이 사용한 프레임",
            subtitle = mostUsedFrame
        )
    }
}

/**
 * 활동 기록 섹션
 */
@Composable
private fun ActivitySection(
    totalPhotos: Int,
    thisMonthPhotos: Int
) {
    ProfileSection(
        title = "활동 기록",
        icon = Icons.Default.Info
    ) {
        ProfileItem(
            icon = Icons.Default.Info,
            title = "총 촬영 횟수",
            subtitle = "${totalPhotos}장"
        )
        ProfileItem(
            icon = Icons.Default.Info,
            title = "이번 달 촬영",
            subtitle = "${thisMonthPhotos}장"
        )
        ProfileItem(
            icon = Icons.Default.Star,
            title = "즐겨찾기 사진",
            subtitle = "${totalPhotos}장"
        )
    }
}

/**
 * 앱 정보 섹션
 */
@Composable
private fun AppInfoSection(totalPhotos: Int) {
    ProfileSection(
        title = "앱 정보",
        icon = Icons.Default.Info
    ) {
        ProfileItem(
            icon = Icons.Default.Info,
            title = "앱 버전",
            subtitle = "1.0.0 (MVP)"
        )
        ProfileItem(
            icon = Icons.Default.Info,
            title = "앱 사용 기간",
            subtitle = "1개월"
        )
        ProfileItem(
            icon = Icons.Default.Info,
            title = "저장 공간 사용량",
            subtitle = "약 ${(totalPhotos * 2)}MB"
        )
    }
}

/**
 * 프로필 섹션 컴포넌트
 */
@Composable
private fun ProfileSection(
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
 * 프로필 아이템 컴포넌트
 */
@Composable
private fun ProfileItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
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
        }
    }
}

/**
 * 통계 아이템 컴포넌트
 */
@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = IosColors.label
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = IosColors.secondaryLabel,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * iOS 스타일 사용자 정보 섹션
 */
@Composable
private fun IOSUserInfoSection(
    userName: String,
    totalPhotos: Int,
    joinDate: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = IosColors.systemBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 프로필 아이콘
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = IosColors.systemGray5,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(40.dp),
                    tint = IosColors.systemGray2
                )
            }
            
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = IosColors.label
            )
            
            Text(
                text = "총 ${totalPhotos}장의 사진",
                style = MaterialTheme.typography.bodyMedium,
                color = IosColors.secondaryLabel
            )
            
            Text(
                text = "가입일: $joinDate",
                style = MaterialTheme.typography.bodySmall,
                color = IosColors.tertiaryLabel
            )
        }
    }
}

/**
 * iOS 스타일 통계 섹션
 */
@Composable
private fun IOSStatisticsSection(
    thisMonthPhotos: Int,
    favoritePhotos: Int,
    mostUsedFrame: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = IosColors.systemBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "통계",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = IosColors.label
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IOSStatItem(
                    value = thisMonthPhotos.toString(),
                    label = "이번 달"
                )
                IOSStatItem(
                    value = favoritePhotos.toString(),
                    label = "즐겨찾기"
                )
            }
            
            Divider(
                color = IosColors.separator,
                thickness = 0.5.dp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "가장 많이 사용한 프레임",
                    style = MaterialTheme.typography.bodyMedium,
                    color = IosColors.secondaryLabel
                )
                Text(
                    text = mostUsedFrame,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = IosColors.label
                )
            }
        }
    }
}

/**
 * iOS 스타일 활동 기록 섹션
 */
@Composable
private fun IOSActivitySection(
    totalPhotos: Int,
    thisMonthPhotos: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = IosColors.systemBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "활동 기록",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = IosColors.label
            )
            
            IOSActivityItem(
                        icon = Icons.Default.Star,
                title = "총 촬영 횟수",
                value = "${totalPhotos}장"
            )
            
            Divider(
                color = IosColors.separator,
                thickness = 0.5.dp
            )
            
            IOSActivityItem(
                icon = Icons.Default.DateRange,
                title = "이번 달 촬영",
                value = "${thisMonthPhotos}장"
            )
            
            Divider(
                color = IosColors.separator,
                thickness = 0.5.dp
            )
            
            IOSActivityItem(
                icon = Icons.Default.Favorite,
                title = "즐겨찾기 사진",
                value = "${totalPhotos}장"
            )
        }
    }
}

/**
 * iOS 스타일 통계 아이템
 */
@Composable
private fun IOSStatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = IosColors.label
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = IosColors.secondaryLabel
        )
    }
}

/**
 * iOS 스타일 활동 아이템
 */
@Composable
private fun IOSActivityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = IosColors.systemGray2
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = IosColors.label
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = IosColors.secondaryLabel
        )
    }
}
