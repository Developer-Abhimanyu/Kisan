package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.presentation.viewmodel.MarketPriceViewModel

@Composable
fun MarketPriceScreen(viewModel: MarketPriceViewModel = hiltViewModel()) {
    val marketPrice by viewModel.marketPrice.collectAsState()
    var commodity by remember { mutableStateOf("") }

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
            Button(onClick = { viewModel.fetchMarketPrice(commodity) }) {
                Text("Fetch Market Price")
            }

            marketPrice?.let {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Commodity: ${it.commodity}")
                Text("Price: ₹${it.price} per ${it.unit}")
            }
        }
    }
}