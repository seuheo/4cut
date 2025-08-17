package com.example.a4cut.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4cut.R
import com.example.a4cut.ui.screens.EmptyScreen
import com.example.a4cut.ui.screens.FrameScreen
import com.example.a4cut.ui.screens.HomeScreen
import com.example.a4cut.ui.screens.SearchScreen

/**
 * 앱의 메인 네비게이션 구조
 * Phase 4.3.2: 이미지 URI 전달을 위한 네비게이션 개선
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    NavigationItem(
                        route = "home_screen",
                        icon = R.drawable.ic_home,
                        label = "홈"
                    ),
                    NavigationItem(
                        route = "search_screen",
                        icon = R.drawable.ic_calendar,
                        label = "검색"
                    )
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(painter = painterResource(id = item.icon), contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home_screen",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home_screen") {
                HomeScreen(
                    onNavigateToPhotoDetail = { photoId ->
                        // TODO: Phase 4.3.2 Week 2에서 PhotoDetailScreen 네비게이션 구현
                        // 현재는 임시로 빈 함수로 처리
                    },
                    onNavigateToFrame = { imageUris ->
                        // FrameScreen으로 이동 (imageUris와 함께)
                        val route = "frame_screen/${imageUris.joinToString(",") { android.net.Uri.encode(it) }}"
                        navController.navigate(route)
                    }
                )
            }
            
            composable(
                route = "frame_screen/{imageUris}",
                arguments = listOf(
                    navArgument("imageUris") { 
                        type = NavType.StringType 
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                // 전달받은 인자를 디코딩하여 사용
                val encodedUris = backStackEntry.arguments?.getString("imageUris") ?: ""
                val imageUris = if (encodedUris.isNotEmpty()) {
                    encodedUris.split(",").map { android.net.Uri.decode(it) }
                } else {
                    emptyList()
                }
                
                FrameScreen(
                    imageUris = imageUris,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("search_screen") {
                SearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPhotoDetail = { photoId ->
                        // TODO: Phase 4.3.2 Week 2에서 PhotoDetailScreen 네비게이션 구현
                        // 현재는 임시로 빈 함수로 처리
                    }
                )
            }
            
            // TODO: Phase 4.3.2 Week 2에서 PhotoDetailScreen과 PhotoEditScreen 네비게이션 구현
            // 현재는 임시로 주석 처리하여 빌드 오류 방지
            
            composable("empty_screen") {
                EmptyScreen(
                    title = "빈 화면",
                    description = "이 화면은 Phase 4.3.2 Week 2에서 구현될 예정입니다."
                )
            }
        }
    }
}

/**
 * 네비게이션 아이템 데이터 클래스
 */
data class NavigationItem(
    val route: String,
    val icon: Int,
    val label: String
)
