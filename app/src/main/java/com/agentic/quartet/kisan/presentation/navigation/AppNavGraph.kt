package com.agentic.quartet.kisan.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.agentic.quartet.kisan.presentation.screens.MarketPriceScreen

sealed class Screen(val route: String) {
    object MarketPrice : Screen("market_price")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.MarketPrice.route
    ) {
        composable(route = Screen.MarketPrice.route) {
            MarketPriceScreen()
        }
    }
}