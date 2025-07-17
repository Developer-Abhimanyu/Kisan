package com.agentic.quartet.kisan.presentation.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.agentic.quartet.kisan.presentation.AppBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.speech.tts.TextToSpeech
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.utils.SpeechRecognizerHelper
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.agentic.quartet.kisan.data.remote.GeminiApiService

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun VoiceAgentScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var userQuery by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<Pair<String, String>>() }
    var latestPdfFile by remember { mutableStateOf<File?>(null) }

    val geminiApi = remember { GeminiApiService(apiKey = "") }
    val ttsFailed = stringResource(R.string.tts_init_failed)
    val micDenied = stringResource(R.string.mic_permission_denied)
    val storageDenied = stringResource(R.string.stoarge_permission_denied)
    val voiceAgent = stringResource(R.string.voice_agent)
    val askAnything = stringResource(R.string.ask_anything_about_your_crops_market_soil_irrigation_and_more)
    val mic = stringResource(R.string.mic)
    val sharePdf = stringResource(R.string.share_pdf)

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(context, ttsFailed, Toast.LENGTH_SHORT).show()
            }
        }.apply { language = Locale.US }
    }

    val speechHelper = remember {
        SpeechRecognizerHelper(
            context = context,
            onResult = { result ->
                isListening = true
                isProcessing = true
                userQuery = result
                aiResponse = ""
                scope.launch {
                    delay(100)
                    aiResponse = geminiApi.getPriceAdvice(result)
                    history.add(0, userQuery to aiResponse)
                    if (history.size > 5) history.removeLast()
                    delay(3000)
                    isListening = false
                    isProcessing = false
                }
            },
            onError = {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                isListening = false
            }
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                isListening = true
                speechHelper.startListening()
            } else {
                Toast.makeText(context, micDenied, Toast.LENGTH_SHORT).show()
            }
        }
    )

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) Toast.makeText(context, storageDenied, Toast.LENGTH_SHORT).show()
        }
    )

    fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            storagePermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(voiceAgent, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50)))
            Spacer(modifier = Modifier.height(16.dp))
            Text(askAnything, style = MaterialTheme.typography.bodyMedium.copy(color = Color.White), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp * if (isListening) micPulse else 1f)
                    .clickable(enabled = !isProcessing) {
                        micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4CAF50),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(100.dp)
                ) {
                    Icon(painterResource(R.drawable.ic_mic), contentDescription = mic, tint = Color.White, modifier = Modifier.padding(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isListening) Text(stringResource(R.string.listening), color = Color.White, fontWeight = FontWeight.Medium)
            if (isProcessing) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color.White)
                Text(stringResource(R.string.processing_your_query), color = Color.White)
            }

            AnimatedVisibility(
                visible = aiResponse.isNotBlank(),
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🤖 ${stringResource(R.string.gemini_response)}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(aiResponse, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { tts.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, null) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                            Text("🔊 ${stringResource(R.string.play_response)}", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            requestStoragePermission()
                            latestPdfFile = exportAsPdf(context, aiResponse)
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))) {
                            Text("⬇️ ${stringResource(R.string.download_as_pdf)}", color = Color.White)
                        }
                        latestPdfFile?.let { file ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, sharePdf))
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                                Text("📤 $sharePdf", color = Color.White)
                            }
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("📜 ${stringResource(R.string.last_queries)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                history.take(5).forEach { (q, r) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🗣️ $q", fontWeight = FontWeight.SemiBold)
                            Text("🤖 $r", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("${stringResource(R.string.tip_try_asking)} \"${stringResource(R.string.how_to_treat_leaf_rust_in_wheat)}\"", color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

fun exportAsPdf(context: Context, content: String): File {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint().apply {
        textSize = 14f
        color = android.graphics.Color.BLACK
    }
    val geminiAIResponse = context.getString(R.string.gemini_ai_response)
    val pdfSaved = context.getString(R.string.pdf_saved_to)
    canvas.drawText(geminiAIResponse, 10f, 25f, paint)
    canvas.drawText(content, 10f, 60f, paint)
    document.finishPage(page)

    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val file = File(dir, "Gemini_Response.pdf")
    document.writeTo(FileOutputStream(file))
    document.close()

    Toast.makeText(context, "$pdfSaved ${file.absolutePath}", Toast.LENGTH_LONG).show()
    return file
}