package com.example.a4cut

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.a4cut.ui.navigation.AppNavigation
import com.example.a4cut.ui.theme._4cutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
            setContent {
                _4cutTheme {
                    AppNavigation()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "앱 초기화 중 오류 발생", e)
            // 에러 발생 시 기본 UI 표시
            setContent {
                _4cutTheme {
                    AppNavigation()
                }
            }
        }
    }
}