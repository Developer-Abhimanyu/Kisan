package com.agentic.quartet.kisan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.agentic.quartet.kisan.presentation.screens.AuthScreen
import com.agentic.quartet.kisan.presentation.screens.DiseaseDetectionScreen
import com.agentic.quartet.kisan.presentation.screens.GovtSchemeNavigatorScreen
import com.agentic.quartet.kisan.presentation.screens.MarketPriceScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object MarketPrice : Screen("market_price")
    object GovtSchemeNavigator : Screen("govt_scheme_navigator")
    object DiseaseDetection : Screen("disease_detection")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.MarketPrice.route) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onSignedIn = {
                    navController.navigate(Screen.MarketPrice.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MarketPrice.route) {
            MarketPriceScreen(
                onNavigateToGovtSchemes = {
                    navController.navigate(Screen.GovtSchemeNavigator.route)
                },
                onNavigateToDiseaseDetection = {
                    navController.navigate(Screen.DiseaseDetection.route)
                }
            )
        }

        composable(Screen.GovtSchemeNavigator.route) {
            GovtSchemeNavigatorScreen(navController)
        }

        composable(Screen.DiseaseDetection.route) {
            DiseaseDetectionScreen(navController)
        }
    }
}