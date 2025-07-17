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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.agentic.quartet.kisan.R

@Composable
fun GovtSchemesScreen(onBack: () -> Unit) {
    val schemes = listOf(
        Scheme(
            "PM-KISAN",
            "Provides ₹6,000 per year to small and marginal farmers in three equal installments.",
            R.drawable.ic_leaf,
            "https://pmkisan.gov.in"
        ),
        Scheme(
            "Soil Health Card",
            "Provides info on nutrient status of soil for better fertilizer application.",
            R.drawable.ic_leaf,
            "https://soilhealth.dac.gov.in"
        ),
        Scheme(
            "PMFBY",
            "Crop insurance scheme covering losses due to natural calamities.",
            R.drawable.ic_leaf,
            "https://pmfby.gov.in"
        ),
        Scheme(
            "eNAM",
            "Online agri-market platform for transparent price discovery and trade.",
            R.drawable.ic_chart,
            "https://enam.gov.in"
        ),
        Scheme(
            "Agri Infra Fund",
            "Loan support for building post-harvest and agri infra facilities.",
            R.drawable.ic_leaf,
            "https://agrinfra.dac.gov.in"
        )
    )

    /*val schemes = listOf(
        Scheme("PM-KISAN", "Provides ₹6,000 per year to small and marginal farmers in three equal installments.", R.drawable.ic_money),
        Scheme("Soil Health Card", "Provides info on nutrient status of soil for better fertilizer application.", R.drawable.ic_soil),
        Scheme("PMFBY", "Crop insurance scheme covering losses due to natural calamities.", R.drawable.ic_insurance),
        Scheme("eNAM", "Online agri-market platform for transparent price discovery and trade.", R.drawable.ic_chart),
        Scheme("Agri Infra Fund", "Loan support for building post-harvest and agri infra facilities.", R.drawable.ic_infra)
    )*/

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Spacer(modifier = Modifier.height(16.dp))

        schemes.forEachIndexed { index, scheme ->
            var expanded by remember { mutableStateOf(index == 0) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { expanded = !expanded }
                    .animateContentSize(animationSpec = tween(300)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = scheme.icon),
                        contentDescription = scheme.title,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 12.dp)
                    )

                    Column {
                        Text(
                            text = scheme.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(
                                    0xFF2E7D32
                                )
                            ),
                            fontWeight = FontWeight.SemiBold
                        )
                        AnimatedVisibility(visible = expanded, enter = expandVertically()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = scheme.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF2E7D32)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            val intent =
                                                Intent(Intent.ACTION_VIEW, Uri.parse(scheme.url))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(
                                                0xFF4CAF50
                                            )
                                        ),
                                    ) {
                                        Text(stringResource(R.string.learn_more), color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.visit_krishi_kendra),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

data class Scheme(
    val title: String,
    val description: String,
    val icon: Int,
    val url: String
)