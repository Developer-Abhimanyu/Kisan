package com.agentic.quartet.kisan

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.agentic.quartet.kisan.presentation.ui.theme.KisanTheme
import com.agentic.quartet.kisan.utils.LanguagePreferenceManager
import com.agentic.quartet.kisan.utils.LocaleHelper
import com.agentic.quartet.kisan.utils.UserPreferences
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences

    override fun attachBaseContext(newBase: Context) {
        val langCode = runBlocking {
            LanguagePreferenceManager(newBase).getSavedLanguage() ?: "en"
        }
        val updatedContext = LocaleHelper.setAppLocale(langCode, newBase)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)
        setContent {
            KisanTheme {
                KisanAppRoot()
            }
        }
    }
}