package com.example.a4cut.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4cut.ui.screens.CalendarScreen
import com.example.a4cut.ui.screens.EmptyScreen
import com.example.a4cut.ui.screens.FrameScreen
import com.example.a4cut.ui.screens.HomeScreen
import com.example.a4cut.ui.screens.PhotoDetailScreen
import com.example.a4cut.ui.screens.PhotoSelectionScreen
import com.example.a4cut.ui.screens.FrameSelectionScreen
import com.example.a4cut.ui.screens.ResultScreen
import com.example.a4cut.ui.screens.ProfileScreen
import com.example.a4cut.ui.screens.SearchScreen
import com.example.a4cut.ui.screens.SettingsScreen
import com.example.a4cut.R
import com.example.a4cut.data.database.entity.PhotoEntity
import com.example.a4cut.ui.viewmodel.FrameViewModel
import com.example.a4cut.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 인스타그램 스타일 네비게이션 바 아이템
 */
@Composable
private fun InstagramNavigationItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = screen.icon),
            contentDescription = stringResource(screen.title),
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

/**
 * 인스타그램 스타일 하단 네비게이션 바
 */
@Composable
private fun InstagramBottomNavigation(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceLight)
            .padding(horizontal = 0.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Screen.Home,
                Screen.Frame,
                Screen.Calendar,
                Screen.Settings,
                Screen.Profile
            ).forEach { screen ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                
                InstagramNavigationItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = { onNavigate(screen.route) }
                )
            }
        }
    }
}

/**
 * 앱의 메인 네비게이션 구조
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // ViewModel을 네비게이션 그래프 레벨에서 공유
    val frameViewModel: FrameViewModel = viewModel()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            InstagramBottomNavigation(
                currentDestination = currentDestination,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            composable(
                route = Screen.Home.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                HomeScreen(
                    onNavigateToPhotoDetail = { photoId ->
                        navController.navigate("photo_detail/$photoId")
                    },
                    onNavigateToFrame = {
                        navController.navigate(Screen.Frame.route)
                    },
                    onNavigateToSearch = {
                        navController.navigate("search")
                    }
                )
            }
            // 1단계: 사진 선택 화면
            composable(
                route = Screen.Frame.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                PhotoSelectionScreen(
                    frameViewModel = frameViewModel,
                    onNext = {
                        navController.navigate("frame_selection")
                    }
                )
            }
            
            // 2단계: 프레임 선택 화면
            composable(
                route = "frame_selection",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                FrameSelectionScreen(
                    frameViewModel = frameViewModel,
                    onNext = {
                        navController.navigate("result")
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 3단계: 결과 확인 및 저장/공유 화면
            composable(
                route = "result",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                ResultScreen(
                    frameViewModel = frameViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onRestart = {
                        // 첫 번째 단계로 돌아가기
                        navController.navigate(Screen.Frame.route) {
                            popUpTo(Screen.Frame.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            
            // 검색 화면
            composable(
                route = "search",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                SearchScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPhotoDetail = { photoId ->
                        navController.popBackStack()
                        navController.navigate("photo_detail/$photoId")
                    }
                )
            }
            composable(
                route = Screen.Calendar.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                CalendarScreen(
                    onNavigateToPhotoDetail = { photoId ->
                        navController.navigate("photo_detail/$photoId")
                    }
                )
            }
            composable(
                route = Screen.Settings.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                SettingsScreen()
            }
            composable(
                route = Screen.Profile.route,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                ProfileScreen()
            }
            
            // 사진 상세 보기 화면
            composable(
                route = "photo_detail/{photoId}",
                arguments = listOf(
                    navArgument("photoId") { type = NavType.IntType }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getInt("photoId") ?: 0
                // TODO: PhotoDetailViewModel을 통해 photoId로 사진 정보를 가져와야 함
                // 임시로 더미 데이터 사용
                val dummyPhoto = PhotoEntity(
                    id = photoId,
                    imagePath = "dummy_path",
                    createdAt = System.currentTimeMillis(),
                    title = "더미 제목",
                    location = "더미 위치",
                    frameType = "ktx_signature"
                )
                
                // PhotoDetailViewModel 생성 (실제로는 ViewModelFactory를 사용해야 함)
                val photoDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.a4cut.ui.viewmodel.PhotoDetailViewModel>()
                
                // 사진 정보 설정
                androidx.compose.runtime.LaunchedEffect(photoId) {
                    photoDetailViewModel.setPhoto(dummyPhoto)
                }
                
                PhotoDetailScreen(
                    viewModel = photoDetailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { navController.navigate("photo_edit/$photoId") },
                    onNavigateToFrameApply = { navController.navigate("frame_apply/$photoId") }
                )
            }
            
            // 사진 편집 화면
            composable(
                route = "photo_edit/{photoId}",
                arguments = listOf(
                    navArgument("photoId") { type = NavType.IntType }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getInt("photoId") ?: 0
                
                // PhotoDetailViewModel 생성 (실제로는 ViewModelFactory를 사용해야 함)
                val photoDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.a4cut.ui.viewmodel.PhotoDetailViewModel>()
                
                // 사진 정보 설정
                androidx.compose.runtime.LaunchedEffect(photoId) {
                    // TODO: 실제 데이터베이스에서 photoId로 사진 정보를 가져와야 함
                    val dummyPhoto = PhotoEntity(
                        id = photoId,
                        imagePath = "dummy_path",
                        createdAt = System.currentTimeMillis(),
                        title = "더미 제목",
                        location = "더미 위치",
                        frameType = "ktx_signature"
                    )
                    photoDetailViewModel.setPhoto(dummyPhoto)
                }
                
                com.example.a4cut.ui.screens.PhotoEditScreen(
                    viewModel = photoDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // 프레임 적용 화면
            composable(
                route = "frame_apply/{photoId}",
                arguments = listOf(
                    navArgument("photoId") { type = NavType.IntType }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getInt("photoId") ?: 0
                val context = LocalContext.current
                
                // 데이터베이스 및 Repository 초기화
                val database = com.example.a4cut.data.database.AppDatabase.getDatabase(context)
                val photoRepository = com.example.a4cut.data.repository.PhotoRepository(database.photoDao())
                val frameRepository = com.example.a4cut.data.repository.FrameRepository()
                
                // FrameApplyViewModel 생성
                val frameApplyViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.a4cut.ui.viewmodel.FrameApplyViewModel>(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.example.a4cut.ui.viewmodel.FrameApplyViewModel(photoRepository, frameRepository, context) as T
                        }
                    }
                )
                
                // 사진 정보 로드
                androidx.compose.runtime.LaunchedEffect(photoId) {
                    frameApplyViewModel.loadPhoto(photoId)
                }
                
                com.example.a4cut.ui.screens.FrameApplyScreen(
                    viewModel = frameApplyViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
