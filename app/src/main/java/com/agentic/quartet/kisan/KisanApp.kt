package com.agentic.quartet.kisan

import android.app.Application
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.agentic.quartet.kisan.presentation.navigation.AppNavGraph
import com.agentic.quartet.kisan.presentation.ui.theme.KisanTheme
import com.agentic.quartet.kisan.utils.LanguagePreferenceManager
import com.agentic.quartet.kisan.utils.LocaleHelper
import com.agentic.quartet.kisan.utils.UserPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class KisanApp : Application()

@Composable
fun KisanAppRoot() {
    val context = LocalContext.current
    val languagePref = remember { LanguagePreferenceManager(context) }
    val navController = rememberNavController()
    val userPreferences = remember { UserPreferences(context) }

    var selectedLangCode by remember { mutableStateOf("en") }
    val scope = rememberCoroutineScope()

    // Fetch saved language once app launches
    LaunchedEffect(Unit) {
        val savedLang = languagePref.getSavedLanguage()
        if (!savedLang.isNullOrBlank()) {
            selectedLangCode = savedLang
            LocaleHelper.setAppLocale(savedLang, context)
        }
    }

    // When language changes in runtime
    SideEffect {
        LocaleHelper.setAppLocale(selectedLangCode, context)
    }

    KisanTheme(darkTheme = isSystemInDarkTheme()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(
                navController = navController,
                userPreferences = userPreferences,
                selectedLangCode = selectedLangCode,
                onLanguageChange = { newLang ->
                    selectedLangCode = newLang
                    scope.launch {
                        languagePref.saveLanguage(newLang)
                        LocaleHelper.setAppLocale(newLang, context)
                    }
                }
            )
        }
    }
}