package com.example.a4cut.ui.navigation

import android.net.Uri

/**
 * 앱의 모든 화면을 정의하는 sealed class
 * 각 화면의 route와 필요한 파라미터를 관리
 */
sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Frame : Screen("frame_screen/{imageUris}") {
        fun createRoute(imageUris: List<String>): String {
            // URI 리스트를 하나의 문자열로 인코딩하여 전달
            val encodedUris = imageUris.joinToString(",") { Uri.encode(it) }
            return "frame_screen/$encodedUris"
        }
    }
    object Search : Screen("search_screen")
    object PhotoDetail : Screen("photo_detail_screen/{photoId}") {
        fun createRoute(photoId: Int): String = "photo_detail_screen/$photoId"
    }
    object PhotoEdit : Screen("photo_edit_screen/{photoId}") {
        fun createRoute(photoId: Int): String = "photo_edit_screen/$photoId"
    }
    object Empty : Screen("empty_screen")
}
