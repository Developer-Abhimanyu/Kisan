package com.agentic.quartet.kisan

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.agentic.quartet.kisan.presentation.ui.theme.KisanTheme
import com.agentic.quartet.kisan.utils.UserPreferences

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences
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