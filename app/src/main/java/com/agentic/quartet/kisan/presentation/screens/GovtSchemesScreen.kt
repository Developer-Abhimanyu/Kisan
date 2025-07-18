package com.agentic.quartet.kisan.presentation.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class Scheme(
    val title: String,
    val icon: Int,
    val url: String,
    val intro: String
)

@Composable
fun GovtSchemesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var schemes by remember { mutableStateOf<List<Scheme>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Fetch intros from Wikipedia API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val pmkIntro = fetchWikipediaIntro("Pradhan Mantri Kisan Samman Nidhi")
            val pmfIntro = fetchWikipediaIntro("Pradhan Mantri Fasal Bima Yojana")
            val soilCardIntro = fetchWikipediaIntro("Soil Health Card Scheme")
            val enamIntro = fetchWikipediaIntro("National Agriculture Market")
            val agriInfraIntro = fetchWikipediaIntro("Agriculture Infrastructure Fund")
            val rashtriyaKrishiVikasYojanaIntro = fetchWikipediaIntro("Rashtriya Krishi Vikas Yojana")
            val paramparagatIntro = fetchWikipediaIntro("Paramparagat Krishi Vikas Yojana")
            val atmaIntro = fetchWikipediaIntro("Support to State Extension Programs for Extension Reforms (ATMA)")

            schemes = listOf(
                Scheme("PM-KISAN", R.drawable.ic_leaf, "https://pmkisan.gov.in", pmkIntro),
                Scheme("PMFBY", R.drawable.ic_leaf, "https://pmfby.gov.in", pmfIntro),
                Scheme("Soil Health Card", R.drawable.ic_leaf, "https://soilhealth.dac.gov.in", soilCardIntro),
                Scheme("eNAM", R.drawable.ic_chart, "https://enam.gov.in", enamIntro),
                Scheme("Agri Infra Fund", R.drawable.ic_leaf, "https://agrinfra.dac.gov.in", agriInfraIntro),
                Scheme("RKVY", R.drawable.ic_leaf, "https://rkvy.nic.in", rashtriyaKrishiVikasYojanaIntro),
                Scheme("PKVY", R.drawable.ic_leaf, "https://pgsindia-ncof.gov.in", paramparagatIntro),
                Scheme("ATMA", R.drawable.ic_leaf, "https://dackkms.gov.in", atmaIntro)
            )
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.government_schemes),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            schemes.forEachIndexed { index, scheme ->
                var expanded by remember { mutableStateOf(index == 0) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { expanded = !expanded }
                        .animateContentSize(tween(300)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Image(
                            painter = painterResource(scheme.icon),
                            contentDescription = scheme.title,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                scheme.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(
                                        0xFF2E7D32
                                    )
                                ),
                                fontWeight = FontWeight.SemiBold
                            )
                            AnimatedVisibility(visible = expanded, enter = expandVertically()) {
                                Column(Modifier.padding(top = 8.dp)) {
                                    Text(scheme.intro, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse(scheme.url))
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFF4CAF50
                                            )
                                        ),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text(
                                            stringResource(R.string.learn_more),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.visit_krishi_kendra),
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

suspend fun fetchWikipediaIntro(pageTitle: String): String = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + pageTitle.replace(" ", "_")
        val json = JSONObject(URL(url).readText())
        json.optString("extract")
    } catch (e: Exception) {
        "Description not available."
    }
}