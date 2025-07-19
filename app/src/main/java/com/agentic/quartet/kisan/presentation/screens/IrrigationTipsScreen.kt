package com.agentic.quartet.kisan.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun IrrigationTipsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    var tips by remember { mutableStateOf<List<String>>(emptyList()) }
    val tipIcons = List(8) { R.drawable.ic_leaf }

    LaunchedEffect(Unit) {
        tips = fetchIrrigationTips()
    }

    AppBackground {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.irrigation_tips),
                style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xFF4CAF50)),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
            Image(
                painter = painterResource(R.drawable.ic_irrigation_tips),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(24.dp))

            tips.forEachIndexed { idx, tip ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .animateContentSize(tween(300))
                        .clickable { expanded = !expanded },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Image(
                            painter = painterResource(tipIcons.getOrElse(idx) { R.drawable.ic_leaf }),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Tip ${idx + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(
                                        0xFF2E7D32
                                    )
                                ),
                                fontSize = 18.sp,
                            )
                            if (expanded) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = tip,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 18.sp,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Proper irrigation saves water and improves yield.\nKeep monitoring soil and crop conditions.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

suspend fun fetchIrrigationTips(): List<String> = withContext(Dispatchers.IO) {
    val url =
        "https://krishijagran.com/agripedia/irrigation-system-know-the-different-types-and-various-methods/"
    return@withContext try {
        val html = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
            .body?.string() ?: return@withContext fallbackTips()
        val doc = Jsoup.parse(html)

        // New selector: find section heading then all <li> under that container
        val section = doc.selectFirst("h2:matchesOwn(Irrigation System).*")?.parent()
            ?: return@withContext fallbackTips()
        val items = section.select("ul li").map { it.text().trim() }.filter(String::isNotBlank)

        if (items.isEmpty()) fallbackTips() else items.take(8)
    } catch (e: Exception) {
        e.printStackTrace()
        fallbackTips()
    }
}

fun fallbackTips() = listOf(
    "Irrigate early morning or evening to reduce evaporation.",
    "Use drip irrigation to conserve water in dry areas.",
    "Check soil moisture before watering again.",
    "Use mulch around plants to retain soil moisture.",
    "Avoid overwatering — it leads to root rot.",
    "For sandy soils, irrigate more frequently with less water.",
    "Harvest rainwater to reduce dependency on irrigation.",
    "Irrigate after fertilization to aid nutrient absorption."
)