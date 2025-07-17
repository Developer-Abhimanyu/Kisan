package com.agentic.quartet.kisan.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.languageDataStore by preferencesDataStore("settings")

class LanguagePreferenceManager(private val context: Context) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    suspend fun saveLanguage(language: String) {
        context.languageDataStore.edit { it[LANGUAGE_KEY] = language }
    }

    suspend fun getSavedLanguage(): String? {
        return context.languageDataStore.data.map { it[LANGUAGE_KEY] }.first()
    }
}