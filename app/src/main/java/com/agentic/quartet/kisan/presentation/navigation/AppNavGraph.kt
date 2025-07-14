package com.agentic.quartet.kisan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.agentic.quartet.kisan.presentation.screens.CropCalendarScreen
import com.agentic.quartet.kisan.presentation.screens.CropDetailScreen
import com.agentic.quartet.kisan.presentation.screens.DiseaseDetectionScreen
import com.agentic.quartet.kisan.presentation.screens.FertilizerGuideScreen
import com.agentic.quartet.kisan.presentation.screens.GovtSchemesScreen
import com.agentic.quartet.kisan.presentation.screens.HomeScreen
import com.agentic.quartet.kisan.presentation.screens.IrrigationTipsScreen
import com.agentic.quartet.kisan.presentation.screens.MarketPriceScreen
import com.agentic.quartet.kisan.presentation.screens.OnboardingScreen
import com.agentic.quartet.kisan.presentation.screens.SignUpScreen
import com.agentic.quartet.kisan.presentation.screens.SoilDetectorScreen
import com.agentic.quartet.kisan.presentation.screens.VoiceAgentScreen

sealed class Screen(val route: String) {
    object MarketPrice : Screen("market_price")
    object GovtSchemeNavigator : Screen("govt_scheme_navigator")
    object DiseaseDetection : Screen("disease_detection")
    object OnboardingScreen : Screen("onboarding_screen")
    object SignUp : Screen("signup_screen")
    object HomeScreen : Screen("home_screen")
    object CropCalendar : Screen("crop_calendar")
    object CropDetail : Screen("crop_detail/{monthIndex}") {
        fun createRoute(monthIndex: Int) = "crop_detail/$monthIndex"
    }

    object FertilizerGuide : Screen("fertilizer_guide")
    object IrrigationTips : Screen("irrigation_tips")
    object SoilDetector : Screen("soil_detector")
    object VoiceAgent : Screen("voice_agent")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.OnboardingScreen.route) {
        composable(Screen.OnboardingScreen.route) {
            OnboardingScreen(onGetStartedClick = {
                navController.navigate(Screen.HomeScreen.route)
            })
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpComplete = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.OnboardingScreen.route) { inclusive = true }
                    }
                }
            )
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
                    navController.navigate(Screen.VoiceAgent.route)
                },
                onSoilDetectorClick = {
                    navController.navigate(Screen.SoilDetector.route)
                },
                onCropCalendarClick = {
                    navController.navigate(Screen.CropCalendar.route)
                },
                onIrrigationTipsClick = {
                    navController.navigate(Screen.IrrigationTips.route)
                },
                onFertilizerGuideClick = {
                    navController.navigate(Screen.FertilizerGuide.route)
                }
            )
        }

        composable(Screen.DiseaseDetection.route) {
            DiseaseDetectionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MarketPrice.route) {
            MarketPriceScreen()
        }

        composable(Screen.GovtSchemeNavigator.route) {
            GovtSchemesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SoilDetector.route) {
            SoilDetectorScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.CropCalendar.route) {
            CropCalendarScreen(onMonthClick = { idx ->
                navController.navigate(Screen.CropDetail.createRoute(idx))
            })
        }

        composable(
            route = Screen.CropDetail.route,
            arguments = listOf(navArgument("monthIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val idx = backStackEntry.arguments!!.getInt("monthIndex")
            CropDetailScreen(monthIndex = idx, onBack = { navController.popBackStack() })
        }

        composable(Screen.IrrigationTips.route) {
            IrrigationTipsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.FertilizerGuide.route) {
            FertilizerGuideScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.VoiceAgent.route) {
            VoiceAgentScreen(onBack = { navController.popBackStack() })
        }
    }
}