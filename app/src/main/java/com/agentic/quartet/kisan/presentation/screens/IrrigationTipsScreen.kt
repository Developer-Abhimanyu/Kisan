package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.agentic.quartet.kisan.R

@Composable
fun IrrigationTipsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    // TODO need to add in strings
    val tips = listOf(
        "Irrigate early morning or evening to reduce evaporation.",
        "Use drip irrigation to conserve water in dry areas.",
        "Check soil moisture before watering again.",
        "Use mulch around plants to retain soil moisture.",
        "Avoid overwatering — it leads to root rot.",
        "For sandy soils, irrigate more frequently with less water.",
        "Rainwater harvesting can reduce irrigation dependency.",
        "Always irrigate after fertilizer application to aid absorption."
    )

    /*val tipIcons = listOf(
        R.drawable.ic_clock,
        R.drawable.ic_drip,
        R.drawable.ic_soil_moisture,
        R.drawable.ic_mulch,
        R.drawable.ic_overwatering,
        R.drawable.ic_sandy_soil,
        R.drawable.ic_rainwater,
        R.drawable.ic_fertilizer
    )*/

    val tipIcons = listOf(
        R.drawable.ic_leaf,
        R.drawable.ic_leaf,
        R.drawable.ic_leaf,
        R.drawable.ic_leaf,
        R.drawable.ic_leaf,
        R.drawable.ic_leaf,
        R.drawable.ic_leaf,
        R.drawable.ic_leaf
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.irrigation_tips),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            //painter = painterResource(R.drawable.ic_irrigation_bg),
            painter = painterResource(R.drawable.ic_leaf),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        tips.forEachIndexed { index, tip ->
            var expanded by remember { mutableStateOf(index == 0) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .animateContentSize(animationSpec = tween(300))
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(tipIcons[index]),
                        contentDescription = "Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 12.dp)
                    )

                    Column {
                        Text(
                            text = "Tip ${index + 1}",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFF2E7D32))
                        )
                        AnimatedVisibility(visible = expanded, enter = expandVertically()) {
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Proper irrigation saves water and improves yield.\nKeep monitoring soil and crop conditions.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}