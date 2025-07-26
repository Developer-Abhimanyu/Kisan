package com.agentic.quartet.kisan.presentation.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agentic.quartet.kisan.R
import com.agentic.quartet.kisan.presentation.AppBackground
import com.agentic.quartet.kisan.utils.ProfileManager
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.util.*

@Composable
fun rememberSpeechRecognizer(
    onResult: (String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        matches?.firstOrNull()?.let { spokenText ->
            onResult(spokenText)
        }
    }

    return {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getCurrentLanguageFromPreferences(context))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }
        launcher.launch(intent)
    }
}

@Composable
fun rememberTextToSpeech(): (String, String) -> Unit {
    val context = LocalContext.current
    val ttsRef = remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        val ttsInstance = TextToSpeech(context, null)
        val langCode = getCurrentLanguageFromPreferences(context)
        val locale = Locale(langCode)
        ttsInstance.language = locale
        ttsRef.value = ttsInstance
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsRef.value?.stop()
            ttsRef.value?.shutdown()
        }
    }

    return { text: String, lang: String ->
        val tts = ttsRef.value
        tts?.language = Locale(lang)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

@Serializable
data class DialogflowResponse(
    @SerialName("queryResult")
    val queryResult: QueryResult? = null
)

@Serializable
data class QueryResult(
    @SerialName("responseMessages")
    val responseMessages: List<ResponseMessage>? = null
)

@Serializable
data class ResponseMessage(
    @SerialName("text")
    val text: TextContent? = null
)

@Serializable
data class TextContent(
    @SerialName("text")
    val text: List<String>? = null
)

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val suggestions: List<Pair<String, String?>>? = null
)

fun saveSelectedLanguage(context: Context, code: String) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("selected_language", code).apply()
}

fun getCurrentLanguageFromPreferences(context: Context): String {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return prefs.getString("selected_language", "en") ?: "en"
}

@Composable
fun ChatBotScreen(onBack: () -> Unit) {
    var userMessage by remember { mutableStateOf(TextFieldValue("")) }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Welcome to Kisan AI Chatbot 👨‍🌾",
                    false
                )
            )
        )
    }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val cxt = LocalContext.current

    val photoFile = File(cxt.cacheDir, "captured_image.jpg")
    val photoUri = FileProvider.getUriForFile(
        cxt,
        "${cxt.packageName}.provider",
        photoFile
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
        }
    }

    val startListening = rememberSpeechRecognizer { spokenText ->
        userMessage = TextFieldValue(spokenText)
    }

    val cameraPermissionState = remember { mutableStateOf(false) }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraPermissionState.value = isGranted
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        }
    }

    val speakOut = rememberTextToSpeech()

    val context = LocalContext.current
    val languages = listOf("English" to "en", "हिंदी" to "hi", "ಕನ್ನಡ" to "kn")
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(getCurrentLanguageFromPreferences(context)) }
    val profile = remember { ProfileManager.loadProfile(context) }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.farmer),
                    contentDescription = "Farmer Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kisan Assistant", color = Color(0xFF4CAF50), fontSize = 22.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(
                                painterResource(R.drawable.language),
                                contentDescription = "Change Language",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(color = Color(0xFF4CAF50))
                        ) {
                            languages.forEach { (label, code) ->
                                DropdownMenuItem(onClick = {
                                    selectedLanguage = code
                                    saveSelectedLanguage(context, code)
                                    expanded = false
                                }, text = {
                                    Text(label, color = Color.White)
                                })
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(chatMessages) { message ->
                    if (message.isUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = message.message,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        Color(0xFF4CAF50),
                                        shape = MaterialTheme.shapes.medium
                                    )
                                    .padding(12.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Column {
                                Text(
                                    text = message.message,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                )
                                message.suggestions?.let { suggestions ->
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        suggestions.forEach { (text, link) ->
                                            SuggestionChip(text = text) {
                                                when {
                                                    link?.contains("https://whatsapp.com/channel") == true -> {
                                                        val intent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse(link)
                                                        )
                                                        context.startActivity(intent)
                                                    }

                                                    link?.startsWith("tel:") == true -> {
                                                        val dialIntent =
                                                            Intent(Intent.ACTION_DIAL).apply {
                                                                data = Uri.parse(link)
                                                            }
                                                        context.startActivity(dialIntent)
                                                    }

                                                    else -> {
                                                        chatMessages =
                                                            chatMessages + ChatMessage(text, true)
                                                        sendMessageToDialogflow(
                                                            city = profile.city,
                                                            context = context,
                                                            userMessage = text
                                                        ) { reply, suggestions ->
                                                            val botText = reply ?: "No response"
                                                            chatMessages =
                                                                chatMessages + ChatMessage(
                                                                    message = botText,
                                                                    isUser = false,
                                                                    suggestions = suggestions
                                                                )
                                                            speakOut(botText, selectedLanguage)
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            selectedImageUri?.let { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Image selected", color = Color.White)
                }
            }

            OutlinedTextField(
                value = userMessage,
                onValueChange = { userMessage = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ask something...", color = Color.White, fontSize = 18.sp) },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF4CAF50),
                    cursorColor = Color(0xFF4CAF50)
                ),
                trailingIcon = {
                    IconButton(onClick = { startListening() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_mic),
                            contentDescription = "Mic",
                            tint = Color.White
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.gallery), color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            cameraLauncher.launch(photoUri)
                        } else {
                            requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.use_camera), color = Color.White, fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        val text = userMessage.text
                        if (text.isNotBlank()) {
                            chatMessages = chatMessages + ChatMessage(text, true)
                            sendMessageToDialogflow(
                                city = profile.city,
                                context = context,
                                userMessage = text
                            ) { reply, suggestions ->
                                val botText = reply ?: "Sorry, no response."
                                chatMessages = chatMessages + ChatMessage(
                                    message = botText,
                                    isUser = false,
                                    suggestions = suggestions
                                )
                                speakOut(botText, selectedLanguage)
                            }
                            userMessage = TextFieldValue("")
                        }
                    },
                ) {
                    Text("Send", fontSize = 16.sp)
                }
            }
        }
    }
}

fun sendMessageToDialogflow(
    city: String,
    context: Context,
    userMessage: String,
    onResult: (String?, List<Pair<String, String?>>?) -> Unit
) {
    val accessToken =
        "ya29.c.c0ASRK0GaBIiFzc6VgGjW_4objgjFo6_rO9wpN6-8-VaFOXKpZPBOtRaeGpuw-XeWLhdud1qIdasn_WXlNgvepaKWbPuJBvRdbbj-h8-jHJPxVrI709ALGlT3xhQQDfWOPSFHsGEKgGLzT4pCXmakLdIeJsHO8zHVtoqQzyONWMlyNX1CwLk1_rfHD6u_pOWoRAdEdhJp_gJq5fF9b15y6KWGcQn8qwf9yoz-JLSFDS9tlUPnaVajAXWDMdyiBxsIoNvZGOpNSrtiTstdYTj_iFxvk4l7q4fzdxvJC55O3996x3Q7rAENerj-kr0p_lmzKKoLsZ23UdKr2HQeHGGqefbceW9xRGHpjnWORDYFvWMk7ce2_Ci7IqikqE385AwYzqcX5nMl3o3c4iRgV2olSZ0McsprVyJR5vb1S71lBlpFMl-oks1h48fs6J4FUoo6nFea9FvhugibJn41plsbQ_VnQuXaZydU0gZ1F7gmiBXYf5cMxQsa9SXcd9ffYR-e1_1nJ631v9Q11kYM4dM-r8x56juyVnumqQxtVI7a_VJklQuBQg28xYZZ2e9mXwUl0qpj0kUyVrFW9gmYoBOnsoURei31mqznWXrbgn10M1VuuUFe01pM4zvat144Fhdkr1td8eyoQRnXiZF9-JcWzrdWOSXjJ5bxdXevt0__O5zpQo-I_eZBJ2-YZ950zrWSBulUVukrrUzurXOuOfnwsBgxW4OJ-OkrB8yrdgsRzVZmjX3hl53WMoUlZXO_dS6vOjjRr_46_ccjMhfUiVSWizItx5gJokQyZay3RY7VrR2vsO69vodBXvogi7xdIc69ayxFYRv3fWMlMqyhB7mB5s1ZkU9BQeww8WkrMU4svX2k_855aUof1VvuW3872SO7088Fqmf94rVu-oUSo9Ov3O6ykio7jUBVU4szZUXdkF9hFlhU-99oQeQvZ46qbF7z5Ba8y0fkvOi-uB5IW_QqW_rR6XgjB7WqnZcs_BoaFn0J4BRpe5bybXo5" // Replace with actual access token if needed
    val sessionUrl =
        "https://global-dialogflow.googleapis.com/v3/projects/forward-alchemy-465709-k7/locations/global/agents/696d1ed8-adef-46da-bbf9-9d7e1d16bdb5/sessions/test-session-001:detectIntent"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val languageCode = getCurrentLanguageFromPreferences(context)

    val requestBody = buildJsonObject {
        putJsonObject("queryInput") {
            putJsonObject("text") {
                put("text", userMessage)
            }
            put("languageCode", languageCode)
        }
        putJsonObject("queryParams") {
            putJsonObject("parameters") {
                put("location", city)
            }
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response: HttpResponse = client.post(sessionUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $accessToken")
                }
            }

            val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val queryResult = responseJson["queryResult"]?.jsonObject

            // 1. Extract the main bot reply (text response)
            val botReply = queryResult?.get("responseMessages")
                ?.jsonArray
                ?.firstOrNull { it.jsonObject.containsKey("text") }
                ?.jsonObject
                ?.get("text")
                ?.jsonObject
                ?.get("text")
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonPrimitive
                ?.contentOrNull

            // 2. Extract chip suggestions from payload.richContent
            val suggestions = mutableListOf<Pair<String, String?>>()
            val payloadResponse = queryResult?.get("responseMessages")
                ?.jsonArray
                ?.firstOrNull { it.jsonObject.containsKey("payload") }
                ?.jsonObject
                ?.get("payload")
                ?.jsonObject

            val richContent = payloadResponse?.get("richContent")?.jsonArray

            richContent?.forEach { outer ->
                outer.jsonArray.forEach { inner ->
                    val type = inner.jsonObject["type"]?.jsonPrimitive?.contentOrNull
                    if (type == "chips") {
                        val options = inner.jsonObject["options"]?.jsonArray
                        options?.forEach { chip ->
                            val text = chip.jsonObject["text"]?.jsonPrimitive?.contentOrNull
                            val link = chip.jsonObject["link"]?.jsonPrimitive?.contentOrNull
                            if (!text.isNullOrEmpty()) {
                                suggestions.add(Pair(text, link))
                            }
                        }
                    }
                }
            }

            onResult(botReply, if (suggestions.isNotEmpty()) suggestions else null)

        } catch (e: Exception) {
            onResult("Error: ${e.message}", null)
        }
    }
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFF4CAF50),
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp
        )
    }
}