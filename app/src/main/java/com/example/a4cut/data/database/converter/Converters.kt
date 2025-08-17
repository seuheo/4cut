package com.example.a4cut.data.database.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room 데이터베이스에서 List<String> 타입을 지원하기 위한 TypeConverter
 * 4컷 이미지 URI 목록을 JSON 형태로 저장하고 불러옵니다.
 */
class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
