package com.example.a4cut.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * GPS 위치 서비스
 * 사용자의 현재 위치를 가져오는 기능을 제공
 */
class LocationService(private val context: Context) {
    
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    /**
     * 위치 권한이 허용되었는지 확인
     */
    fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || 
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 현재 위치를 가져옴 (비동기)
     * @return Location 객체 또는 null (권한이 없거나 위치를 가져올 수 없는 경우)
     */
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resumeWithException(
                            task.exception ?: Exception("위치를 가져올 수 없습니다")
                        )
                    }
                }
                
                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 마지막으로 알려진 위치를 가져옴 (빠른 응답)
     * @return Location 객체 또는 null
     */
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }
        
        return try {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(null)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 두 지점 간의 거리를 계산 (미터 단위)
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 거리 (미터)
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}
