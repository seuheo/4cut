package com.example.a4cut.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.TransitionState
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
import androidx.compose.runtime.remember
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
import com.example.a4cut.ui.screens.FramePickerScreen
import com.example.a4cut.ui.screens.OnboardingScreen
import com.example.a4cut.ui.screens.HomeScreen
import com.example.a4cut.ui.screens.PhotoDetailScreen
import com.example.a4cut.ui.screens.PhotoSelectionScreen
import com.example.a4cut.ui.screens.FrameSelectionScreen
import com.example.a4cut.ui.screens.ResultScreen
import com.example.a4cut.ui.screens.ProfileScreen
import com.example.a4cut.ui.screens.SearchScreen
import com.example.a4cut.ui.screens.SettingsScreen
import com.example.a4cut.ui.screens.CampaignScreen
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
            tint = if (isSelected) IosColors.label else IosColors.secondaryLabel
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
            .background(IosColors.systemBackground)
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
    navController: NavHostController = rememberNavController(),
    frameViewModel: FrameViewModel? = null,
    openGallery: (() -> Unit)? = null
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    
    // 데이터베이스 및 Repository를 네비게이션 그래프 레벨에서 싱글톤으로 관리
    val database = remember { com.example.a4cut.data.database.AppDatabase.getDatabase(context) }
    val photoRepository = remember { com.example.a4cut.data.repository.PhotoRepository(database.photoDao()) }
    val frameRepository = remember { 
        val repo = com.example.a4cut.data.repository.FrameRepository()
        // JSON에서 슬롯 정보 로드 및 기존 Frame 객체에 병합
        repo.loadSlotsFromJson(context)
        repo
    }
    
    // ViewModel을 네비게이션 그래프 레벨에서 공유 (전달받은 것이 있으면 사용, 없으면 새로 생성)
    val sharedFrameViewModel: FrameViewModel = frameViewModel ?: viewModel()
    
    // HomeViewModel을 네비게이션 그래프 레벨에서 공유
    val sharedHomeViewModel: com.example.a4cut.ui.viewmodel.HomeViewModel = viewModel()
    
    // HomeViewModel 초기화
    androidx.compose.runtime.LaunchedEffect(Unit) {
        sharedHomeViewModel.setContext(context)
    }

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
                            // 홈 버튼 클릭 시 항상 초기 화면으로 이동하도록 saveState 제거
                            // saveState = true // 제거됨
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // 홈 버튼 클릭 시 항상 초기 화면으로 이동하도록 restoreState 제거
                        // restoreState = true // 제거됨
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )
            }
        ) {
            // 온보딩 화면
            composable(
                route = Screen.Onboarding.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(500))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                }
            ) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.Home.route,
                arguments = listOf(
                    navArgument("location") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
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
                val location = backStackEntry.arguments?.getString("location")
                HomeScreen(
                    homeViewModel = sharedHomeViewModel,
                    selectedLocation = location,
                    onNavigateToPhotoDetail = { photoId ->
                        navController.navigate("photo_detail/$photoId")
                    },
                    onNavigateToFrame = {
                        // 워크플로우 변경: 프레임 선택을 먼저 함
                        navController.navigate("frame_selection")
                    },
                    onNavigateToSearch = {
                        navController.navigate("search")
                    }
                )
            }
            // 1단계: 프레임 선택 화면 (워크플로우 변경: 프레임 먼저 선택)
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
                    frameViewModel = sharedFrameViewModel,
                    onNext = {
                        // 프레임 선택 후 사진 선택 화면으로 이동
                        navController.navigate(Screen.Frame.route)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            // 2단계: 사진 선택 화면 (프레임 정보를 가지고 사진 선택)
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
                    frameViewModel = sharedFrameViewModel,
                    onNext = {
                        // 사진 선택 후 결과 화면으로 이동 (이미지 합성 시작)
                        sharedFrameViewModel.startImageComposition()
                        navController.navigate("result")
                    },
                    openGallery = openGallery,
                    navController = navController
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
                    frameViewModel = sharedFrameViewModel,
                    photoRepository = photoRepository, // DB 저장을 위한 Repository 전달
                    onBack = {
                        try {
                            if (navController.previousBackStackEntry != null && navController.previousBackStackEntry!!.destination.route != null) {
                                navController.popBackStack()
                            } else {
                                // 백 스택이 없으면 홈으로 이동
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // 네비게이션 스택이 비어있거나 오류 발생 시 홈으로 이동
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onRestart = {
                        // 첫 번째 단계로 돌아가기 (사진 선택 화면)
                        navController.navigate(Screen.Frame.route) {
                            popUpTo(Screen.Frame.route) {
                                inclusive = true
                            }
                        }
                    },
                    onRestartWithPhotos = {
                        // 기존 사진 유지하고 프레임 선택 화면으로
                        navController.navigate("frame_selection")
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
                        try {
                            if (navController.previousBackStackEntry != null && navController.previousBackStackEntry!!.destination.route != null) {
                                navController.popBackStack()
                            } else {
                                // 백 스택이 없으면 홈으로 이동
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // 네비게이션 스택이 비어있거나 오류 발생 시 홈으로 이동
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onNavigateToPhotoDetail = { photoId ->
                        try {
                            // PhotoDetailScreen으로 이동 (popBackStack 불필요)
                            navController.navigate("photo_detail/$photoId")
                        } catch (e: Exception) {
                            // 네비게이션 오류 시 직접 이동
                            navController.navigate("photo_detail/$photoId")
                        }
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
                    homeViewModel = sharedHomeViewModel,
                    onNavigateToPhotoDetail = { photoId ->
                        navController.navigate("photo_detail/$photoId")
                    },
                    onNavigateToHomeWithLocation = { location ->
                        navController.navigate("home?location=$location")
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
                ProfileScreen(
                    onNavigateToCampaign = {
                        navController.navigate(Screen.Campaign.route)
                    }
                )
            }
            
            // ✅ MVP Ver2: 노선도(잇다) 캠페인 화면
            composable(
                route = Screen.Campaign.route,
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
                CampaignScreen(
                    onNavigateBack = {
                        try {
                            if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            // 네비게이션 스택이 비어있거나 오류 발생 시 홈으로 이동
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            }
            
            // 사진 상세 보기 화면 - 확대 전환 효과
            composable(
                route = "photo_detail/{photoId}",
                arguments = listOf(
                    navArgument("photoId") { type = NavType.IntType }
                ),
                enterTransition = {
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    )
                }
            ) { backStackEntry ->
                val photoId = backStackEntry.arguments?.getInt("photoId") ?: 0
                
                // 공유된 Repository를 사용하여 PhotoDetailViewModel 생성
                val photoDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.a4cut.ui.viewmodel.PhotoDetailViewModel>(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.example.a4cut.ui.viewmodel.PhotoDetailViewModel(photoRepository) as T
                        }
                    }
                )
                
                // 사진 정보 로드
                androidx.compose.runtime.LaunchedEffect(photoId) {
                    photoDetailViewModel.loadPhoto(photoId)
                }
                
                PhotoDetailScreen(
                    viewModel = photoDetailViewModel,
                    onNavigateBack = { 
                        try {
                            if (navController.previousBackStackEntry != null && navController.previousBackStackEntry!!.destination.route != null) {
                                navController.popBackStack()
                            } else {
                                // 백 스택이 없으면 홈으로 이동
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // 네비게이션 오류 시 홈으로 이동
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    },
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
                
                // 공유된 Repository를 사용하여 PhotoDetailViewModel 생성
                val photoDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.a4cut.ui.viewmodel.PhotoDetailViewModel>(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.example.a4cut.ui.viewmodel.PhotoDetailViewModel(photoRepository) as T
                        }
                    }
                )
                
                // 사진 정보 로드
                androidx.compose.runtime.LaunchedEffect(photoId) {
                    photoDetailViewModel.loadPhoto(photoId)
                }
                
                com.example.a4cut.ui.screens.PhotoEditScreen(
                    viewModel = photoDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // 프레임 적용 화면
            // 이미지 크롭 화면 (long_form 프레임용)
            composable(
                route = "${Screen.Crop.route}?uri={uri}&ratio={ratio}&slotIndex={slotIndex}",
                arguments = listOf(
                    navArgument("uri") { type = NavType.StringType; defaultValue = "" },
                    navArgument("ratio") { type = NavType.StringType; defaultValue = "3:4" },
                    navArgument("slotIndex") { type = NavType.IntType; defaultValue = -1 }
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
                val imageUriString = backStackEntry.arguments?.getString("uri") ?: ""
                val aspectRatioString = backStackEntry.arguments?.getString("ratio") ?: "3:4"
                val slotIndex = backStackEntry.arguments?.getInt("slotIndex") ?: -1
                
                com.example.a4cut.ui.screens.CropScreen(
                    navController = navController,
                    imageUriString = imageUriString,
                    aspectRatioString = aspectRatioString,
                    slotIndex = slotIndex
                )
            }
            
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
                
                // 공유된 Repository를 사용하여 FrameApplyViewModel 생성
                val frameApplyViewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.example.a4cut.ui.viewmodel.FrameApplyViewModel>(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return com.example.a4cut.ui.viewmodel.FrameApplyViewModel(photoRepository, frameRepository, context, sharedFrameViewModel) as T
                        }
                    }
                )
                
                // 사진 정보 로드
                androidx.compose.runtime.LaunchedEffect(photoId) {
                    frameApplyViewModel.loadPhoto(photoId)
                }
                
                com.example.a4cut.ui.screens.FrameApplyScreen(
                    viewModel = frameApplyViewModel,
                    frameViewModel = sharedFrameViewModel,
                    onNavigateBack = { 
                        try {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            // 네비게이션 오류 시 홈으로 이동
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                )
            }
            
            // 프레임 선택 화면
            composable(
                route = Screen.FramePicker.route,
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
                FramePickerScreen(
                    navController = navController,
                    viewModel = sharedFrameViewModel
                )
            }
        }
    }
}
