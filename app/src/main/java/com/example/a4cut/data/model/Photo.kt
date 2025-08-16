package com.example.a4cut.data.model

import android.net.Uri

/**
 * 사진 데이터 모델
 * @param uri 사진의 URI
 * @param id 고유 식별자
 */
data class Photo(
    val uri: Uri? = null,
    val id: String = ""
)
