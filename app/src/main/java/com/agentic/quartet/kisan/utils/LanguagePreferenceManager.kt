package com.agentic.quartet.kisan.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

private val Context.languageDataStore by preferencesDataStore(name = "settings")

class LanguagePreferenceManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    suspend fun saveLanguage(language: String) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun getSavedLanguage(): String? {
        return context.languageDataStore.data
            .map { preferences: Preferences ->
                preferences[LANGUAGE_KEY]
            }.first()
    }
}