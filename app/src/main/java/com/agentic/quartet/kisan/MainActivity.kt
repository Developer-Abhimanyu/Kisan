package com.agentic.quartet.kisan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import com.agentic.quartet.kisan.presentation.navigation.AppNavGraph
import com.agentic.quartet.kisan.presentation.ui.theme.KisanTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    KisanTheme {
        AppNavGraph(navController = navController)
    }
}