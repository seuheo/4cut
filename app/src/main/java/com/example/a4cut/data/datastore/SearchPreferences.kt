package com.example.a4cut.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 검색 기록을 영구 저장하기 위한 DataStore
 * 최근 검색어 10개를 사용자 기기에 저장하여 앱 재시작 시에도 유지
 */
class SearchPreferences(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_preferences")
        private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
    }
    
    /**
     * 검색 기록 가져오기
     */
    val searchHistory: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val historyString = preferences[SEARCH_HISTORY_KEY] ?: "[]"
        try {
            // JSON 형태로 저장된 검색 기록을 List로 변환
            parseSearchHistory(historyString)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 검색 기록 저장하기
     */
    suspend fun saveSearchHistory(history: List<String>) {
        context.dataStore.edit { preferences ->
            val historyString = serializeSearchHistory(history)
            preferences[SEARCH_HISTORY_KEY] = historyString
        }
    }
    
    /**
     * 검색어 추가하기
     */
    suspend fun addSearchQuery(query: String) {
        // 현재 검색 기록을 가져와서 처리
        searchHistory.collect { currentHistory ->
            val updatedHistory = currentHistory.toMutableList()
            // 중복 제거
            updatedHistory.remove(query)
            // 맨 앞에 추가
            updatedHistory.add(0, query)
            // 최대 10개만 유지
            if (updatedHistory.size > 10) {
                updatedHistory.removeAt(updatedHistory.size - 1)
            }
            saveSearchHistory(updatedHistory)
            return@collect
        }
    }
    
    /**
     * 검색 기록 초기화
     */
    suspend fun clearSearchHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(SEARCH_HISTORY_KEY)
        }
    }
    
    /**
     * 검색 기록을 JSON 문자열로 직렬화
     */
    private fun serializeSearchHistory(history: List<String>): String {
        return history.joinToString(",")
    }
    
    /**
     * JSON 문자열을 검색 기록 List로 역직렬화
     */
    private fun parseSearchHistory(historyString: String): List<String> {
        return if (historyString.isEmpty() || historyString == "[]") {
            emptyList()
        } else {
            historyString.split(",").filter { it.isNotEmpty() }
        }
    }
}
