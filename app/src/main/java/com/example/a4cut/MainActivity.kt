package com.example.a4cut

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.a4cut.ui.navigation.AppNavigation
import com.example.a4cut.ui.theme._4cutTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _4cutTheme {
                AppNavigation()
            }
        }
    }
}