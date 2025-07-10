package com.agentic.quartet.kisan.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.presentation.viewmodel.MarketPriceViewModel
import com.agentic.quartet.kisan.utils.SpeechRecognizerHelper

@Composable
fun MarketPriceScreen(viewModel: MarketPriceViewModel = hiltViewModel()) {
    var commodity by remember { mutableStateOf("") }
    val context = LocalContext.current

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { spokenText ->
                commodity = spokenText
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                speechHelper.startListening()
            } else {
                Toast.makeText(context, "Mic permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = commodity,
                onValueChange = { commodity = it },
                label = { Text("Commodity Name") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    speechHelper.startListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }) {
                Text("Speak Commodity Name")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.fetchMarketPrice(commodity) }) {
                Text("Fetch Market Price")
            }
        }
    }
}