package com.agentic.quartet.kisan.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {
    private val SIGNED_IN = booleanPreferencesKey("signed_in")

    suspend fun setSignedIn(value: Boolean) {
        context.dataStore.edit { prefs -> prefs[SIGNED_IN] = value }
    }

    suspend fun isSignedIn(): Boolean {
        return context.dataStore.data.map { prefs -> prefs[SIGNED_IN] ?: false }.first()
    }
}