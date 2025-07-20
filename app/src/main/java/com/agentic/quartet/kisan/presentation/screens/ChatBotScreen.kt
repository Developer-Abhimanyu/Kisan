package com.agentic.quartet.kisan.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
    val suggestions: List<String>? = null
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

    val context = LocalContext.current
    val languages = listOf("English" to "en", "हिंदी" to "hi", "ಕನ್ನಡ" to "kn")
    var expanded by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(getCurrentLanguageFromPreferences(context)) }

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
                                        suggestions.forEach { chip ->
                                            SuggestionChip(text = chip) {
                                                when {
                                                    chip.contains("wa.me") || chip.contains("WhatsApp") -> {
                                                        val intent = Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://whatsapp.com/channel/0029VavDqAOFHWq4qE4pc13G")
                                                        )
                                                        context.startActivity(intent)
                                                    }

                                                    chip.contains("Helpline") -> {
                                                        val digits = "1800-180-1551"
                                                        if (digits != null) {
                                                            val dialIntent =
                                                                Intent(Intent.ACTION_DIAL).apply {
                                                                    data = Uri.parse("tel:$digits")
                                                                }
                                                            context.startActivity(dialIntent)
                                                        }
                                                    }

                                                    else -> {
                                                        chatMessages =
                                                            chatMessages + ChatMessage(chip, true)
                                                        sendMessageToDialogflow(
                                                            context,
                                                            chip
                                                        ) { reply, suggestions ->
                                                            chatMessages =
                                                                chatMessages + ChatMessage(
                                                                    reply ?: "No response",
                                                                    false,
                                                                    suggestions
                                                                )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp)) // add spacing between chips
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val text = userMessage.text
                    if (text.isNotBlank()) {
                        chatMessages = chatMessages + ChatMessage("$text", true)
                        sendMessageToDialogflow(context, text) { reply, suggestions ->
                            chatMessages = chatMessages + ChatMessage(
                                reply ?: "Sorry, no response.",
                                false,
                                suggestions
                            )
                        }
                        userMessage = TextFieldValue("")
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Send", fontSize = 18.sp)
            }
        }
    }
}

fun sendMessageToDialogflow(
    context: Context,
    userMessage: String,
    onResult: (String?, List<String>?) -> Unit
) {
    val accessToken =
        "ya29.c.c0ASRK0GZSUN4ap-rN5z17sJTpf6CR7YvcRrQ2Qr1AqXAN4pDxB0_-GLzPvqcyJ8S-lJwq_qr5nzQLw3IycC2OpM2o1uXOtt7IUmRon1KQMme4NlDAYu97bI6_XFvVXsXjEL35JaDQMiXZqC1kdg2fY7bQs_I6lo4SpvtUSV84PjJ4wWeMQ0ZaTztXaxyUrkvFjDZr9lDfKNJ8mpOTxeUkdB8TgJMA_d8ieunkqpHPIDxAGQCw1fSl9MNnoD4I7REgymbwN0fXs-wm5lz9XTzY9xMqiRTOQTSbeKuAnNgPd3nYZ7W5I1Onl4JOO460UtGOV8nhIc_Pfpx-o01ot3ROXtr8FDlZBUn52c8rN_gBULTFauvzfHlhKJ1OrwN387AcjojFxIquS5qMd4fSu76dgdezdSnx4Zv2cIRs1s6hi0SpgJ_vs0cqlue0lvQ7qMMg4jVVtojhk_ep4SSfyIse10grauZ5fd2kMJUndgFBWFjeqy3VMlUm67hJBIRYdYmB993kgi1iZfj_5cR-kt5926zozXrQd-YX6aZ_kV-d9Bl_pfIR92eVUZhd0o1w2tFFg-rgIMoBFx4imQuF6qvXUISQwca5-RwUbY90OwwmtWWOjO32FUk6mXM3MaqIq1-j9F7yZZraImYfmSOFfQYBntU0m-qZ6is0g6vnp78xl3uZ-8iqw60zOW5oO_pxcp1oqUh7aeolQy4Vrrt-XUdjX0jMXp8Zs_9aSwdzO_xm0bgiUUQU1ReBXyoqZqRrzfz9-1mclaIXZy9lg8JB4uMgmF8JO3oqxx2uW72cdZxzJ0-roF6utk4WOgwYB7jmUIfzSl8gnq9VogRvWmlaaapMYxnmxWJWzOo3Q1eMlBOXhtvUpVIyaOsF6Jy-p8avgxM7knqgIhF7Yz2Xq7RBlQq2Og0l8p4g6O9B4Q2aOsxyeoMf1bnSVqr-j1173ls49Bgu1sB9worrSdjguldjZvWvR66YxtMXzcwl8Y_MXURcfcJsY_2JjWb-6ZV" // Replace with actual access token if needed
    val sessionUrl =
        "https://global-dialogflow.googleapis.com/v3/projects/forward-alchemy-465709-k7/locations/global/agents/696d1ed8-adef-46da-bbf9-9d7e1d16bdb5/sessions/test-session-001:detectIntent"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val languageCode = getCurrentLanguageFromPreferences(context)

    Log.i("ChatBotScreen", "Using language code: $languageCode")

    val requestBody = buildJsonObject {
        putJsonObject("queryInput") {
            putJsonObject("text") {
                put("text", userMessage)
            }
            put("languageCode", languageCode) // Change dynamically if needed
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
            val suggestions = mutableListOf<String>()
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
                            if (!text.isNullOrEmpty()) {
                                suggestions.add(text)
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