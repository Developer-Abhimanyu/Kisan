package com.agentic.quartet.kisan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.agentic.quartet.kisan.presentation.screens.AuthScreen
import com.agentic.quartet.kisan.presentation.screens.DiseaseDetectionScreen
import com.agentic.quartet.kisan.presentation.screens.GovtSchemeNavigatorScreen
import com.agentic.quartet.kisan.presentation.screens.HomeScreen
import com.agentic.quartet.kisan.presentation.screens.MarketPriceScreen
import com.agentic.quartet.kisan.presentation.screens.OnboardingScreen

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object MarketPrice : Screen("market_price")
    object GovtSchemeNavigator : Screen("govt_scheme_navigator")
    object DiseaseDetection : Screen("disease_detection")
    object OnboardingScreen : Screen("onboarding_screen")
    object HomeScreen : Screen("home_screen")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.OnboardingScreen.route) {
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

        composable(Screen.OnboardingScreen.route) {
            OnboardingScreen(onGetStartedClick = {
                navController.navigate(Screen.HomeScreen.route)
            })
        }

        composable(Screen.HomeScreen.route) {
            HomeScreen(
                onNavigateToDiseaseDetection = {
                    navController.navigate(Screen.DiseaseDetection.route)
                },
                onNavigateToMarketPrices = {
                    navController.navigate(Screen.MarketPrice.route)
                },
                onNavigateToGovtSchemes = {
                    navController.navigate(Screen.GovtSchemeNavigator.route)
                },
                onVoiceAgentClick = {
                    navController.navigate(Screen.MarketPrice.route)
                }
            )
        }
    }
}