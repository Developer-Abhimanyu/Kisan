package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import java.util.*

@Composable
fun CropCalendarScreen(
    onMonthClick: (Int) -> Unit
) {
    val months = listOf(
        stringResource(R.string.january), stringResource(R.string.february), stringResource(R.string.march), stringResource(R.string.april), stringResource(R.string.may), stringResource(R.string.june),
        stringResource(R.string.july), stringResource(R.string.august), stringResource(R.string.september), stringResource(R.string.october), stringResource(R.string.november), stringResource(R.string.december)
    )

    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val animatedScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        animatedScale.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.crop_calendar),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(months) { index, month ->
                    val isCurrent = index == currentMonth
                    Card(
                        onClick = { onMonthClick(index) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) Color(0xFF4CAF50) else Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                if (isCurrent) {
                                    scaleX = animatedScale.value
                                    scaleY = animatedScale.value
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = month,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isCurrent) Color.White else Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🌾 ${stringResource(R.string.sowing_millet)}\n💧 ${
                                    stringResource(
                                        R.string.january
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCurrent) Color.White else Color(0xFF2E7D32)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.tap_on_a_month_to_explore_seasonal_crop_guidance),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}