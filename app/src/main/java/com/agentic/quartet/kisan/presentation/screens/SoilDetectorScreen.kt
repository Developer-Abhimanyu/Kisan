package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SoilDetectorScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var resultVisible by remember { mutableStateOf(false) }

    //TODO need to add to strings
    val soilData = remember {
        mutableStateOf(
            SoilData(
                moisture = "Moderate (30%)",
                ph = "Slightly Acidic (6.0)",
                fertility = "Nitrogen Deficient",
                suggestion = "Apply Urea and Organic Compost"
            )
        )
    }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.soil_health_detector),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true
                        resultVisible = false
                        scope.launch {
                            delay(2000)
                            isLoading = false
                            resultVisible = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.scan_soil_health), color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Text(stringResource(R.string.scanning), color = Color.White)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                AnimatedVisibility(
                    visible = resultVisible,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Column {
                        ResultCard(title = "🌊 ${stringResource(R.string.humidity)}", value = soilData.value.moisture)
                        ResultCard(title = "⚖️ ${stringResource(R.string.ph_evel)}", value = soilData.value.ph)
                        ResultCard(title = "🧪 ${stringResource(R.string.fertility)}", value = soilData.value.fertility)
                        ResultCard(title = "💡 ${stringResource(R.string.suggestion)}", value = soilData.value.suggestion)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.soil_health_improves_with_crop_rotation_and_organic_matter),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ResultCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.DarkGray)
            )
        }
    }
}

data class SoilData(
    val moisture: String,
    val ph: String,
    val fertility: String,
    val suggestion: String
)