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
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4cut.ui.screens.EmptyScreen
import com.example.a4cut.ui.screens.FrameScreen
import com.example.a4cut.ui.screens.HomeScreen
import com.example.a4cut.ui.screens.PhotoDetailScreen
import com.example.a4cut.R
import com.example.a4cut.data.database.entity.PhotoEntity

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

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                listOf(
                    Screen.Home,
                    Screen.Frame,
                    Screen.Calendar,
                    Screen.Settings,
                    Screen.Profile
                ).forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.icon),
                                contentDescription = stringResource(screen.title)
                            )
                        },
                        label = { Text(stringResource(screen.title)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToPhotoDetail = { photoId ->
                        navController.navigate("photo_detail/$photoId")
                    }
                )
            }
            composable(Screen.Frame.route) {
                FrameScreen()
            }
            composable(Screen.Calendar.route) {
                EmptyScreen(
                    title = stringResource(R.string.title_calendar),
                    description = stringResource(R.string.description_calendar)
                )
            }
            composable(Screen.Settings.route) {
                EmptyScreen(
                    title = stringResource(R.string.title_settings),
                    description = stringResource(R.string.description_settings)
                )
            }
            composable(Screen.Profile.route) {
                EmptyScreen(
                    title = stringResource(R.string.title_profile),
                    description = stringResource(R.string.description_profile)
                )
            }
            
            // 사진 상세 보기 화면
            composable(
                route = "photo_detail/{photoId}",
                arguments = listOf(
                    navArgument("photoId") { type = NavType.IntType }
                )
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
                PhotoDetailScreen(
                    photo = dummyPhoto,
                    onNavigateBack = { navController.popBackStack() },
                    onEditPhoto = { /* TODO: 편집 화면으로 이동 */ },
                    onToggleFavorite = { /* TODO: 즐겨찾기 토글 */ },
                    onDeletePhoto = { /* TODO: 삭제 처리 */ }
                )
            }
        }
    }
}
