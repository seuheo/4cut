package com.example.a4cut.ui.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

/**
 * 권한 처리를 위한 헬퍼 클래스
 * Android 13+ 권한 정책을 대응하고 권한 거부 시 사용자 안내를 제공
 */
class PermissionHelper(private val context: Context) {
    
    /**
     * 이미지 읽기 권한이 필요한지 확인
     */
    fun isImagePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            // Android 12 이하
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }
    
    /**
     * 필요한 권한 목록 반환
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    /**
     * 권한 요청을 위한 ActivityResultContracts 생성
     */
    fun createPermissionRequestContract(): ActivityResultContracts.RequestMultiplePermissions {
        return ActivityResultContracts.RequestMultiplePermissions()
    }
    
    /**
     * 설정 화면으로 이동하는 Intent 생성
     */
    fun createSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    /**
     * 권한이 거부된 이유를 설명하는 다이얼로그 표시
     */
    @Composable
    fun PermissionRationaleDialog(
        onRequestPermission: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("권한이 필요해요") },
            text = { 
                Text(
                    "사진을 불러오려면 갤러리 접근 권한이 필요합니다. " +
                    "이 권한은 선택한 사진만 접근하며, 다른 개인정보에는 접근하지 않습니다."
                )
            },
            confirmButton = {
                TextButton(onClick = onRequestPermission) {
                    Text("권한 요청")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        )
    }
    
    /**
     * 권한이 영구 거부된 경우 설정으로 이동하는 다이얼로그
     */
    @Composable
    fun PermissionDeniedDialog(
        onGoToSettings: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("권한이 거부되었습니다") },
            text = { 
                Text(
                    "갤러리 접근 권한이 거부되어 사진을 불러올 수 없습니다. " +
                    "기기 설정에서 직접 권한을 허용해주세요."
                )
            },
            confirmButton = {
                TextButton(onClick = onGoToSettings) {
                    Text("설정으로 이동")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("취소")
                }
            }
        )
    }
}
