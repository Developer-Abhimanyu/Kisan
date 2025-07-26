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
import androidx.compose.foundation.lazy.rememberLazyListState
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
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isInitialized by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val ttsEngine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            } else {
                Log.e("TTS", "Initialization failed with status $status")
            }
        }
        tts = ttsEngine

        onDispose {
            ttsEngine.stop()
            ttsEngine.shutdown()
        }
    }

    return remember {
        { text: String, languageCode: String ->
            if (isInitialized && tts != null) {
                val result = tts!!.setLanguage(Locale(languageCode))
                if (result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    Log.e("TTS", "Language $languageCode not supported or missing data.")
                }
            } else {
                Log.e("TTS", "TextToSpeech not initialized yet.")
            }
        }
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
    val listState = rememberLazyListState()
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
            val base64 = uriToBase64(cxt, uri)
            base64?.let {
                chatMessages = chatMessages + ChatMessage("📷 Image sent", isUser = true)

            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
            val base64 = uriToBase64(cxt, photoUri)
            base64?.let {
                chatMessages = chatMessages + ChatMessage("📷 Image sent", isUser = true)
            }
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

    speakOut("Welcome to Kisan AI Chatbot 👨‍🌾", selectedLanguage)

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
                state = listState,
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

            LaunchedEffect(chatMessages.size) {
                listState.animateScrollToItem(chatMessages.size - 1)
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
        "ya29.c.c0ASRK0Gahz1wEp6I0A5WzENx6HAgMHKTHcLNfW4nFNUGTD1vv9eFEUhemeq2Bps-2-ninlH0cHwfd-uFEVo6OzSB3es8G0k4IMm81fzChaTgljTCbvlFVfqMTA79WGCwgwi1d4KZoKKtms-U26OHYlqL9N6N5WKQSzhGpi8b_Qmx9HPg-PD9oOUXgqmBhB1SxDzTcgC_HfO0mL3ZRPYZmLglpN0JxtAqij8rszD3q49RhGRlh00LrSargWrEw1AdTcCXi2EspGtS8UXWcjK0zjkEeWKyeVwRKrxgD0clMIl693l1Mhi_DJzcGqdhtCywOxwki_tW23n6RZDxzcrR8jvE0j3ZOrCCHcRVwxWPXSrkTeLvCk-eBVgNiL385PQlo3bziuw_dnYMMZOUY020jqWj5718bxcl029wy1lISyyMJzQ2dcsMU_Y980QpS6QRneq5yOFOu50witJk4bjOx-asZoiSe9X-q-iV7ebhFUFcwz7Y6Qi2WovmUnBY5nOZ96Jltpbf6jtznJ0hMgzJeIjUZQoo7O62tc7SiVFXnZSic018ams19ZFaxfY47iS6437Um5zuWIiIXYXW9JpwIV66Odjsadzb5uod7oIzet6e01fu3eXSBO3X9phF8uhh_79hos8RSQ859Zhu_d80pr0tWZQ8drwI4-Ysq6lQwe5MWhxV4ho9tVp78ORw8pj7u5eqy1zje4zXzar94IUWIkkflBJtzfguQbttmdQjWz6xrZnokehftXdoM2eM1Si72I1xXIYiRu84hp7gMh4vI7bzfW68Myfyckqnm_nU_iYp5F1a_23sIw6pRgcynOB2FSZ4euBa3j5ZBfXsx5yUeJUoWjmB5X7esvfuWg3Y4ZnvSBcxtJXhdh4tueWj7akl4SoWl9l76opqQe58vz52nadX4R3F-vFg5BiykIcmq6_eJBX8gfoRBeelQ6ZkqXj3Xx9xv_dOeQao16la1uiJ_YRFybBMnfy_1M0dSu2OZvtlqczV2fOV1pq8"
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

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}