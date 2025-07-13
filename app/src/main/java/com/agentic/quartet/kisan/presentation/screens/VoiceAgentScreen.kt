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
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.agentic.quartet.kisan.data.remote.GeminiApiService

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

    val geminiApi = remember { GeminiApiService(apiKey = "AIzaSyBqs-vJieMml-dXi9O0hA_NVFlsGGbfoNU") }

    val tts = remember {
        TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Toast.makeText(context, "TTS init failed", Toast.LENGTH_SHORT).show()
            }
        }.apply {
            language = Locale.US
        }
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
                    try {
                        val response = geminiApi.getPriceAdvice(result)
                        aiResponse = response
                        history.add(userQuery to response)
                        if (history.size > 5) history.removeAt(history.lastIndex)
                    } catch (e: Exception) {
                        aiResponse = "⚠️ Failed to get response. Check internet or API key."
                        e.printStackTrace()
                    } finally {
                        delay(3000)
                        isListening = false
                        isProcessing = false
                    }
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
                Toast.makeText(context, "Mic permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) Toast.makeText(context, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    )

    val infiniteTransition = rememberInfiniteTransition()
    val micPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        )
    )

    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Agent",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ask anything about your crops, market, soil, irrigation and more!",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(120.dp * if (isListening) micPulse else 1f)
                    .clickable(enabled = !isProcessing) {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
                    Icon(
                        painterResource(R.drawable.ic_mic),
                        contentDescription = "Mic",
                        tint = Color.White,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isListening) {
                Text("Listening...", color = Color.White, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = isProcessing, enter = fadeIn(), exit = fadeOut()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processing your query...", color = Color.White, fontSize = 14.sp)
                }
            }

            AnimatedVisibility(
                visible = aiResponse.isNotBlank(),
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🤖 Gemini Response", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(aiResponse, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { tts.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, null) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text("🔊 Play Response", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                requestStoragePermission()
                                latestPdfFile = exportAsPdf(context, aiResponse)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                        ) {
                            Text("⬇️ Download as PDF", color = Color.White)
                        }
                        latestPdfFile?.let { file ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share PDF"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Text("📤 Share PDF", color = Color.White)
                            }
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("📜 Last 5 Queries", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                history.take(5).forEach { (q, r) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🗣️ $q", fontWeight = FontWeight.SemiBold)
                            Text("🤖 $r", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Tip: You can say things like:\n\"What crop is best for July?\"\n\"How to treat leaf rust in wheat?\"",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                textAlign = TextAlign.Center
            )
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
        color = AndroidColor.BLACK
    }
    canvas.drawText("Gemini AI Response:", 10f, 25f, paint)
    canvas.drawText(content, 10f, 60f, paint)
    document.finishPage(page)

    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    val file = File(dir, "Gemini_Response.pdf")
    document.writeTo(FileOutputStream(file))
    document.close()

    Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    return file
}