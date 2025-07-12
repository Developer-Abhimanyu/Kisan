package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agentic.quartet.kisan.R

@Composable
fun HomeScreen(
    onNavigateToDiseaseDetection: () -> Unit,
    onNavigateToMarketPrices: () -> Unit,
    onNavigateToGovtSchemes: () -> Unit,
    onVoiceAgentClick: () -> Unit
) {
    val context = LocalContext.current
    val weatherCardOffset = remember { Animatable(-200f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        weatherCardOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = weatherCardOffset.value }
        ) {
            WeatherCard(
                location = "Bekasi Timur",
                date = "27 Nov 2023",
                temperature = 33,
                humidity = 76,
                condition = "Cloudy",
                suggestion = "Today is a good day to apply pesticides."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GeminiAdviceCard(
            text = "Based on today’s weather and soil moisture, consider irrigation before sunset for better crop absorption."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Smart Farming Tools",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HomeFeatureButton(
                    "Disease Detection",
                    R.drawable.ic_leaf,
                    onNavigateToDiseaseDetection
                )
            }
            item {
                HomeFeatureButton("Market Prices", R.drawable.ic_chart, onNavigateToMarketPrices)
            }
            item {
                HomeFeatureButton("Govt Schemes", R.drawable.ic_gov, onNavigateToGovtSchemes)
            }
            item {
                HomeFeatureButton("Voice Agent", R.drawable.ic_mic, onVoiceAgentClick)
            }
            /*item {
                HomeFeatureButton("Soil Scanner", R.drawable.ic_soil) {
                    // TODO: Add navigation
                }
            }
            item {
                HomeFeatureButton("Crop Calendar", R.drawable.ic_calendar) {
                    // TODO: Add navigation
                }
            }
            item {
                HomeFeatureButton("Irrigation Tips", R.drawable.ic_water) {
                    // TODO: Add navigation
                }
            }
            item {
                HomeFeatureButton("Fertilizer Guide", R.drawable.ic_fertilizer) {
                    // TODO: Add navigation
                }
            }*/
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "🌱 Tip: Tap a tool above to start your smart farming journey!",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
    }
}