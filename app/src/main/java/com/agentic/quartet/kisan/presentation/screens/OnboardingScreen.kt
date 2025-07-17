package com.agentic.quartet.kisan.presentation.screens

import android.app.Activity
import android.os.Build
import android.os.LocaleList
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.utils.LanguagePreferenceManager
import com.agentic.quartet.kisan.utils.LocaleHelper.setAppLocale
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun OnboardingScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    selectedLangCode: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val languagePref = remember { LanguagePreferenceManager(context) }

    val languages = mapOf("en" to "English", "kn" to "ಕನ್ನಡ", "hi" to "हिंदी")
    var expanded by remember { mutableStateOf(false) }
    var selectedLangCode by remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        val savedLang = languagePref.getSavedLanguage() // safe call inside coroutine
        if (savedLang != null) {
            selectedLangCode = savedLang
            setAppLocale(savedLang, context)
        }
    }

    fun updateLocale(localeCode: String) {
        val locale = Locale(localeCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocales(LocaleList(locale))
        resources.updateConfiguration(config, resources.displayMetrics)

        (context as? Activity)?.recreate()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Image(
                    painter = painterResource(id = R.drawable.ic_icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(languages[selectedLangCode] ?: "Language", color = Color(0xFF4CAF50))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    expanded = false
                                    selectedLangCode = code
                                    scope.launch {
                                        languagePref.saveLanguage(code)
                                        updateLocale(code)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))) {
                        append(stringResource(R.string.the_new_era))
                    }
                    withStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))) {
                        append("\n")
                        append(stringResource(R.string.agriculture))
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.growing_resources_thriving_futures),
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF4CAF50)),
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.leaf),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onSignUpClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.sign_up), color = Color.White)
                    }

                    Button(
                        onClick = onSignInClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.sign_in), color = Color.White)
                    }
                }

                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.ai_powered_insights_for_modern_agriculture),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}