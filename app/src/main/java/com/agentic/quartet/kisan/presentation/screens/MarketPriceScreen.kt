package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.material.icons.filled.Home
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.agentic.quartet.kisan.data.remote.GeminiApiService
import com.agentic.quartet.kisan.utils.SpeechRecognizerHelper
import androidx.compose.foundation.*
import androidx.compose.ui.text.style.TextAlign
import com.agentic.quartet.kisan.presentation.AppBackground

@Composable
fun MarketPriceScreen() {
    val context = LocalContext.current
    var selectedCrop by remember { mutableStateOf("Tomato") }
    val cropPriceMap = mapOf(
        "Tomato" to listOf(22, 24, 21, 25, 23),
        "Wheat" to listOf(18, 19, 20, 21, 21),
        "Onion" to listOf(35, 34, 33, 36, 38),
        "Rice" to listOf(32, 30, 31, 33, 34)
    )
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val prices = cropPriceMap[selectedCrop] ?: emptyList()

    var advice by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var commodity by remember { mutableStateOf("") }

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { spoken -> commodity = spoken },
            onError = { error -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show() }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) speechHelper.startListening()
        else Toast.makeText(context, "Mic permission denied", Toast.LENGTH_SHORT).show()
    }

    val geminiApi = remember { GeminiApiService(apiKey = "YOUR_API_KEY") }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Market Price Assistant",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = commodity,
                onValueChange = { commodity = it },
                label = { Text("Enter Commodity Name", color = Color(0xFF2E7D32)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Mic", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Speak", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "📈 Crop Market Trend",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2E7D32)
                )
                DropdownMenuCropSelector(
                    options = cropPriceMap.keys.toList(),
                    selected = selectedCrop,
                    onCropSelected = { selectedCrop = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            MarketBarChart(days = days, prices = prices)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    advice =
                        "$selectedCrop prices have shown a steady rise this week. Consider selling on Friday for the best rate."
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(50)
            ) {
                Text("Get AI Price Advice", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = advice.isNotBlank(),
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Insight", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            advice,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tip: Use this data to decide best selling time for your crops.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MarketBarChart(days: List<String>, prices: List<Int>) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 8.dp)
    ) {
        days.zip(prices).forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(value.dp * 2)
                        .background(Color(0xFF81C784), RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
fun DropdownMenuCropSelector(
    options: List<String>,
    selected: String,
    onCropSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Crop", color = Color(0xFF2E7D32)) },
            modifier = Modifier
                .width(160.dp)
                .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it, color = Color(0xFF2E7D32)) },
                    onClick = {
                        onCropSelected(it)
                        expanded = false
                    }
                )
            }
        }
    }
}