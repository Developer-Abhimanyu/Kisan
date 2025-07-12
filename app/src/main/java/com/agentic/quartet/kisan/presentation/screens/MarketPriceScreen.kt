package com.agentic.quartet.kisan.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.agentic.quartet.kisan.data.remote.GeminiApiService
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.presentation.viewmodel.MarketPriceViewModel
import com.agentic.quartet.kisan.utils.SpeechRecognizerHelper
import kotlinx.coroutines.launch

@Composable
fun MarketPriceScreen(viewModel: MarketPriceViewModel = hiltViewModel(), onNavigateToGovtSchemes: () -> Unit, onNavigateToDiseaseDetection: () -> Unit) {
    var commodity by remember { mutableStateOf("") }
    var marketData by remember { mutableStateOf("") }
    var advice by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { spokenText -> commodity = spokenText },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) speechHelper.startListening()
            else Toast.makeText(context, "Mic permission denied", Toast.LENGTH_SHORT).show()
        }
    )

    val geminiApi = remember { GeminiApiService(apiKey = "YOUR_VERTEX_AI_API_KEY") }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Market Price Assistant",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = commodity,
                onValueChange = { commodity = it },
                label = { Text("Commodity Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        speechHelper.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎙️ Speak Commodity")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.fetchMarketPrice(commodity) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📊 Fetch Market Price")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    marketData = "Tomato - ₹25 per kg, price stable this week." // Simulated
                    scope.launch {
                        advice = geminiApi.getPriceAdvice(marketData)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🧠 Get Price Trend Advice")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Animated advice card
            AnimatedVisibility(visible = advice.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📈 Price Trend Advice",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(advice, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Button(
                onClick = { onNavigateToGovtSchemes() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🏛️ Govt Scheme Navigator")
            }

            Button(
                onClick = { onNavigateToDiseaseDetection() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🩺 Disease Detection (Image)")
            }
        }
    }
}