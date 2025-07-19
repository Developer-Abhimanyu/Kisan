package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground

@Composable
fun FertilizerGuideScreen(onBack: () -> Unit) {
    val cropList = listOf("Wheat", "Rice", "Sugarcane")
    var selectedCrop by remember { mutableStateOf(cropList.first()) }
    var dropdownVisible by remember { mutableStateOf(false) }

    val cropFertilizerMap = mapOf(
        "Wheat" to listOf(
            FertilizerInfo(
                name = "Urea",
                usage = "High nitrogen for leaf growth",
                dosage = "50 kg/acre",
                timing = "Apply during tillering stage",
                //icon = painterResource(R.drawable.ic_fertilizer)
                icon = painterResource(R.drawable.ic_leaf)
            ),
            FertilizerInfo(
                name = "DAP",
                usage = "Promotes root development",
                dosage = "40 kg/acre",
                timing = "Apply at sowing time",
                //icon = painterResource(R.drawable.ic_soil)
                icon = painterResource(R.drawable.ic_leaf)
            )
        ),
        "Rice" to listOf(
            FertilizerInfo(
                name = "Urea",
                usage = "Enhances tillering",
                dosage = "45 kg/acre",
                timing = "Apply 20 days after transplanting",
                //icon = painterResource(R.drawable.ic_fertilizer)
                icon = painterResource(R.drawable.ic_leaf)
            ),
            FertilizerInfo(
                name = "MOP",
                usage = "Improves yield and drought tolerance",
                dosage = "25 kg/acre",
                timing = "Apply before flowering stage",
                icon = painterResource(R.drawable.ic_leaf)
            )
        ),
        "Sugarcane" to listOf(
            FertilizerInfo(
                name = "Urea",
                usage = "Boosts early growth",
                dosage = "60 kg/acre",
                timing = "Apply 30–45 days after planting",
                //icon = painterResource(R.drawable.ic_fertilizer)
                icon = painterResource(R.drawable.ic_leaf)
            ),
            FertilizerInfo(
                name = "Compost",
                usage = "Enhances soil moisture and texture",
                dosage = "500 kg/acre",
                timing = "Apply 2–3 weeks before sowing",
                //icon = painterResource(R.drawable.ic_water)
                icon = painterResource(R.drawable.ic_leaf)
            )
        )
    )

    /*val cropIcons = mapOf(
        "Wheat" to painterResource(R.drawable.ic_wheat),
        "Rice" to painterResource(R.drawable.ic_rice),
        "Sugarcane" to painterResource(R.drawable.ic_sugarcane)
    )*/

    val cropIcons = mapOf(
        "Wheat" to painterResource(R.drawable.ic_leaf),
        "Rice" to painterResource(R.drawable.ic_leaf),
        "Sugarcane" to painterResource(R.drawable.ic_leaf)
    )

    val fertilizers = cropFertilizerMap[selectedCrop] ?: emptyList()

    val scrollState = rememberScrollState()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.fertilizer_guide),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.select_crop),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF2E7D32),
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(visible = true, enter = fadeIn(animationSpec = tween(600))) {
                CropDropdown(
                    cropList,
                    selectedCrop,
                    onCropSelected = { selectedCrop = it },
                    expanded = dropdownVisible,
                    onExpandedChange = { dropdownVisible = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = fertilizers.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                Column {
                    fertilizers.forEachIndexed { index, item ->
                        ExpandableFertilizerCard(fertilizer = item, index = index)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.tip_fertilizer),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDropdown(
    options: List<String>,
    selected: String,
    onCropSelected: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val cropIcons = mapOf(
        "Wheat" to painterResource(R.drawable.ic_leaf),
        "Rice" to painterResource(R.drawable.ic_leaf),
        "Sugarcane" to painterResource(R.drawable.ic_leaf)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.crop), color = Color(0xFF2E7D32)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { crop ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            cropIcons[crop]?.let {
                                Image(
                                    painter = it,
                                    contentDescription = crop,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 8.dp)
                                )
                            }
                            Text(crop, color = Color(0xFF2E7D32), fontSize = 18.sp,)
                        }
                    },
                    onClick = {
                        onCropSelected(crop)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

data class FertilizerInfo(
    val name: String,
    val usage: String,
    val dosage: String,
    val timing: String,
    val icon: Painter
)

@Composable
fun ExpandableFertilizerCard(fertilizer: FertilizerInfo, index: Int) {
    var expanded by remember { mutableStateOf(index == 0) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(animationSpec = tween(300))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = fertilizer.icon,
                contentDescription = fertilizer.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF4CAF50))
                    .padding(6.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = fertilizer.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF2E7D32),
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))

                AnimatedVisibility(visible = expanded, enter = expandVertically()) {
                    Column {
                        Text("🧩 Usage: ${fertilizer.usage}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32), fontSize = 18.sp,)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📏 Dosage: ${fertilizer.dosage}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32), fontSize = 18.sp,)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("⏰ Timing: ${fertilizer.timing}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32), fontSize = 18.sp,)
                    }
                }
            }
        }
    }
}