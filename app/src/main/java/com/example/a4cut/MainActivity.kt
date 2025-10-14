package com.example.a4cut

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.a4cut.ui.navigation.AppNavigation
import com.example.a4cut.ui.theme._4cutTheme
import com.example.a4cut.ui.viewmodel.FrameViewModel

class MainActivity : ComponentActivity() {

    // FrameViewModel 인스턴스 가져오기
    private val frameViewModel: FrameViewModel by viewModels()

    // 갤러리에서 여러 이미지를 선택하기 위한 ActivityResultLauncher
    private val pickImagesLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            Log.d("MainActivity", "갤러리에서 선택된 이미지: ${uris.size}개")
            // 이미지가 선택되었을 경우 ViewModel에 전달
            if (uris.isNotEmpty()) {
                Log.d("MainActivity", "ViewModel에 이미지 URI 전달 시작")
                frameViewModel.onImagesSelected(uris)
            } else {
                Log.d("MainActivity", "선택된 이미지가 없습니다")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 디버그 모드에서 테스트 Activity로 이동 (임시로 주석 처리)
        // if (BuildConfig.DEBUG) {
        //     Log.d("MainActivity", "디버그 모드: 테스트 Activity로 이동")
        //     startActivity(Intent(this, ImageComposerTestActivity::class.java))
        //     finish()
        //     return
        // }
        
        try {
            enableEdgeToEdge()
            setContent {
                _4cutTheme {
                    // AppNavigation에 ViewModel과 갤러리 실행 람다 함수를 전달
                    AppNavigation(
                        frameViewModel = frameViewModel,
                        openGallery = {
                            Log.d("MainActivity", "갤러리 열기 버튼 클릭됨")
                            pickImagesLauncher.launch("image/*")
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "앱 초기화 중 오류 발생", e)
            // 에러 발생 시 기본 UI 표시
            setContent {
                _4cutTheme {
                    AppNavigation(
                        frameViewModel = frameViewModel,
                        openGallery = {
                            Log.d("MainActivity", "갤러리 열기 버튼 클릭됨 (에러 복구)")
                            pickImagesLauncher.launch("image/*")
                        }
                    )
                }
            }
        }
    }
}