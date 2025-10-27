package com.example.a4cut.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.a4cut.R

/**
 * 앱의 네비게이션 화면들을 정의
 */
sealed class Screen(
    val route: String,
    @StringRes val title: Int,
    @DrawableRes val icon: Int
) {
    object Home : Screen(
        route = "home",
        title = R.string.nav_home,
        icon = R.drawable.ic_home
    )
    
    object Frame : Screen(
        route = "frame",
        title = R.string.nav_frame,
        icon = R.drawable.ic_frame
    )
    
    object Calendar : Screen(
        route = "calendar",
        title = R.string.nav_calendar,
        icon = R.drawable.ic_calendar
    )
    
    object Settings : Screen(
        route = "settings",
        title = R.string.nav_settings,
        icon = R.drawable.ic_settings
    )
    
    object Profile : Screen(
        route = "profile",
        title = R.string.nav_profile,
        icon = R.drawable.ic_profile
    )
    
    object Onboarding : Screen(
        route = "onboarding",
        title = R.string.nav_onboarding,
        icon = R.drawable.ic_home
    )
    
    object FramePicker : Screen(
        route = "frame_picker",
        title = R.string.nav_frame,
        icon = R.drawable.ic_frame
    )
    
    object Campaign : Screen(
        route = "campaign",
        title = R.string.nav_campaign,
        icon = R.drawable.ic_campaign
    )
}
