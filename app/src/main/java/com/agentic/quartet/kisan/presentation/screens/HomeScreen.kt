package com.agentic.quartet.kisan.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.utils.ProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onNavigateToDiseaseDetection: () -> Unit,
    onNavigateToMarketPrices: () -> Unit,
    onNavigateToGovtSchemes: () -> Unit,
    onVoiceAgentClick: () -> Unit,
    onSoilDetectorClick: () -> Unit,
    onCropCalendarClick: () -> Unit,
    onIrrigationTipsClick: () -> Unit,
    onFertilizerGuideClick: () -> Unit
) {
    val context = LocalContext.current
    val weatherCardOffset = remember { Animatable(-200f) }
    val profile = remember { ProfileManager.loadProfile(context) }

    LaunchedEffect(Unit) {
        weatherCardOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
        )
    }
    // Weather state
    var temp by remember { mutableStateOf("--") }
    var humidity by remember { mutableStateOf("--") }
    var condition by remember { mutableStateOf("Loading...") }
    var suggestion by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }
    var geminiAdvice by remember { mutableStateOf("Fetching weather-based advice...") }

    val currentDate: String = LocalDate.now().format(
        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)
    )

    LaunchedEffect(profile.city) {
        fetchWeather(profile.city) { t, h, cond, icon ->
            temp = t
            humidity = h
            condition = cond
            iconUrl = "https://openweathermap.org/img/wn/${icon}@4x.png"
            suggestion = when {
                cond.contains("rain", true) -> "It may rain today. Consider drainage or harvesting early."
                t.toFloatOrNull()?.let { it > 35 } == true -> "It's hot. Irrigate your crops in the evening."
                else -> "Today is a good day to apply pesticides."
            }
            geminiAdvice = buildString {
                appendLine("Current weather in ${profile.city} is $cond with $t°C and $h% humidity.")
                when {
                    cond.contains("rain", true) -> {
                        appendLine("It may rain today. Ensure proper drainage in your field.")
                        appendLine("Avoid pesticide spraying to prevent wash-off.")
                    }
                    t.toFloatOrNull()?.let { it > 35 } == true -> {
                        appendLine("The temperature is quite high. Irrigation should be done in the evening.")
                        appendLine("Mulching can help retain soil moisture.")
                    }
                    else -> {
                        appendLine("Conditions are ideal for routine farming tasks.")
                        appendLine("You may apply fertilizers or pesticides today.")
                    }
                }
            }
        }
    }

    AppBackground {
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
                    location = profile.city,
                    date = currentDate,
                    temperature = temp,
                    humidity = humidity,
                    condition = condition,
                    suggestion = suggestion,
                    iconUrl = iconUrl
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GeminiAdviceCard(
                text = geminiAdvice
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.smart_farming_tools),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                ),
                fontSize = 18.sp,
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
                        stringResource(R.string.disease_detection),
                        R.drawable.ic_disease_detection,
                        onNavigateToDiseaseDetection
                    )
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.market_prices),
                        R.drawable.ic_chart,
                        onNavigateToMarketPrices
                    )
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.govt_schemes),
                        R.drawable.ic_gov,
                        onNavigateToGovtSchemes
                    )
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.voice_agent),
                        R.drawable.ic_mic,
                        onVoiceAgentClick
                    )
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.soil_scanner),
                        R.drawable.ic_soil_scanner
                    ) {
                        onSoilDetectorClick()
                    }
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.crop_calendar),
                        R.drawable.ic_calendar_final
                    ) {
                        onCropCalendarClick()
                    }
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.irrigation_tips),
                        R.drawable.irrigation_tips_final
                    ) {
                        onIrrigationTipsClick()
                    }
                }
                item {
                    HomeFeatureButton(
                        stringResource(R.string.fertilizer_guide),
                        R.drawable.fertilizer_guide_final
                    ) {
                        onFertilizerGuideClick()
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(12.dp).align(Alignment.End),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { /* navigateToChatbot() */ },
                    containerColor = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chatbot),
                        contentDescription = stringResource(R.string.chatbot),
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.tip_home),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
            )
        }
    }
}

private fun fetchWeather(city: String, onResult: (String, String, String, String) -> Unit) {
    val apiKey = "54cb908afa507577edcf811874836703"
    val url =
        "https://api.openweathermap.org/data/2.5/weather?q=${city.trim()}&units=metric&appid=$apiKey"

    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
        try {
            val res = URL(url).readText()
            val obj = JSONObject(res)
            val main = obj.getJSONObject("main")
            val weather = obj.getJSONArray("weather").getJSONObject(0)

            val temp = main.getDouble("temp").toString()
            val humidity = main.getInt("humidity").toString()
            val condition = weather.getString("main")
            val iconCode = weather.getString("icon")

            withContext(Dispatchers.Main) {
                onResult(temp, humidity, condition, iconCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}