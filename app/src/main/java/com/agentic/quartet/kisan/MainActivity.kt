package com.agentic.quartet.kisan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import com.agentic.quartet.kisan.presentation.navigation.AppNavGraph
import com.agentic.quartet.kisan.presentation.ui.theme.KisanTheme
import com.agentic.quartet.kisan.utils.UserPreferences

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)
        setContent {
            val navController = rememberNavController()
            KisanTheme {
                AppNavGraph(navController, userPreferences)
            }
        }
    }
}