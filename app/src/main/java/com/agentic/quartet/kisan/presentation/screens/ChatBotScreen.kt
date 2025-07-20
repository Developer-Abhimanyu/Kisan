package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
    val suggestions: List<String>? = null // ✅ Add this line
)

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
                Text("Kisan Assistant", color = Color(0xFF4CAF50))
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
                                color = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.White, shape = MaterialTheme.shapes.medium)
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
                                    color = Color.Black,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                )
                                message.suggestions?.let { suggestions ->
                                    LazyRow(
                                        modifier = Modifier
                                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                            .fillMaxWidth()
                                    ) {
                                        items(suggestions) { chip ->
                                            SuggestionChip(text = chip) {
                                                userMessage = TextFieldValue(chip)
                                            }
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
                label = { Text("Ask something...", color = Color(0xFF4CAF50)) },
                textStyle = TextStyle(color = Color(0xFF4CAF50)),
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
                        sendMessageToDialogflow(text) { reply, suggestions ->
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

fun sendMessageToDialogflow(userMessage: String, onResult: (String?, List<String>?) -> Unit) {
    val accessToken =
        "ya29.c.c0ASRK0Ga7W91o79dnidbOF7ovK9mib5WOJSWLlfFAF6u5zIbI24G4BSrLGFtfLlDdYtOE-Hz2zPfvMxVUmX98Mh7SePMQU5uCgF23YRJuXjel-LKiXshOShUgL5KDCAk1YQV5WZByVhKdTaIWgvEAtb97VexbEyfNwOgoAX8u06FADLmHCp48G8XcJNaWVhg_42px5MwUi6r0Gx67sFlJ-TWrFZzgvoDP8jVbErgKdjDn2kdZhX4iNcJXWJpcbuokzKbT7GFTBV-0NtZyBBF99YV2n1Nz78K21lHO7q66FugtR-LwRa7gyOjX0qHKtUU_rjWbPhnmUYTV0eGvANfVIjzQlM22Mmz_pIvf-_jeBH5VaeYhSBXLuPzyN385DM4uWbM7zXOs_RMMV06WermMyZ7qt4OeYvhsz04sSvoRnZlvRQBVs8dkkd7c_okJ1wptbxbfkfWt4Mgj_oJF9o7n91_r2qOUJjF7gFiQUQ64jzoq66jjoyeJVw9vSapi_X7Uhr-3z8bqFdqpRRdmslnVsUX-B5tnJzdXmVrSm_0Biotdw13gy0Ys0qenUqu3jmOnjveq7rseem0hjewZhpfijSqgofeMmmuyjqZobX66SvSszX_1MjSMSy14f2XFM_vrZVxz2MVgh4s7iofRuZb3sv8F4R0jvJw-j2k7fIOcoFMRMJeSS7mZx5ZFy1rI8VQOYwVIJo9UdOvbI7riO8J1mOsB5qW66JpJbZMJykXj4-WBB0oMmuzgk7pW-bRYop8xZywJt46BemmaU9z6oBxoSZmSF7Q_sz2jgR_n2qkSJZ4txfW5t-gVd_vlluURu0lYSRjUUx4WYVV8se4dB91RqzqV_vyOZa5U05Xwb3oMIV9w7wkY-fmY6Wus0IU64OQ_BWcs6i1y4alx7suar7RS2OSYsOgZvfI0IIvB7aUJOZyaZqYgRMsu9QQOXg2XnJ9RfRIarpw6etFXVrelj35Bpz1isnR15wYd-8uzRop7rbdbYBJaaqBFg7O" // Replace with actual access token if needed
    val sessionUrl =
        "https://global-dialogflow.googleapis.com/v3/projects/forward-alchemy-465709-k7/locations/global/agents/696d1ed8-adef-46da-bbf9-9d7e1d16bdb5/sessions/test-session-001:detectIntent"

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    val requestBody = buildJsonObject {
        putJsonObject("queryInput") {
            putJsonObject("text") {
                put("text", userMessage)
            }
            put("languageCode", "en") // Change dynamically if needed
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
        color = Color(0xFF81C784),
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