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
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        suggestions.forEach { chip ->
                                            SuggestionChip(text = chip) {
                                                sendMessageToDialogflow(chip) { reply, suggestions ->
                                                    chatMessages = chatMessages + ChatMessage(
                                                        chip,
                                                        true
                                                    ) + ChatMessage(
                                                        reply ?: "No response",
                                                        false,
                                                        suggestions
                                                    )
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
        "ya29.c.c0ASRK0GbOYSR_VdfrHYcMglh7LTMO8K9Z2JO-fdS-d8OKLDxdLNVwA38q4YvkGjk6RCtk1d07C2yn0_AIzWbapPiWBH2MO8jMVYdpE0xbdMwZbUpKewAyJerA-AAKtv_YrQEVi8-MAb22o9Gaan2WX4To7qNeNRCxt5bEbqLFp0MCN7j-RpSdfxgCnLfuKECYDZMoF8Wig_X8NMPck9n7tttl0IHld6iW3HXEiTFokcPn7s2JfdxZpfAFYYMRCcuv_MV2ZDhHiP8OsXKm_1OzCNlcsugirelHpccjOQHvRUVYG1QnXUxZSC6tum7dJBFuycYdn2nbUtJz8DA1TfxFpPmXm_-jl2T8hIkGCAuemvcXRUSMhWT2nl2xH385Kdyt1MUndybivwmvxob_3l_q5Qc07MfJWalp5o1bdBqFgxyQg4Fnm28evZlvcjOed2r4uF-1X1Q9tQVl7eejQ2nmh73bMnjiu7iMmcRSW9l3uFnQVVF--_Ve1ns1rRomhbt9tJketdullaMQzF5arck2lny48vVRpow9taXJlx48ew8Z34W3lSVrgzovOydZuB6g4J6drWgywYfd4Jryfra1Jni6hIFxcdURO94kFlb4gZxlaffXjho7_M__Q6WWWWMq918248r4427_1ih2cUXB2YY4676SBc2JmMJ5QSmO-p63Wthy06kYk29mshgu-Q_WmiR-U5iO8amFxqrMZ7J1eV7zSjBSWZmse5X-qoSuJ1QoZagV811WWUWVjn7Mbumg849k_UdQ4Jw-mmwbZ4lqj7m5I_hw3bsJ29Wg9OiUv9MFWjoOSbwhcMFJty7WYvq66ce2YrszwiWh81wQQzdw_h79Sc2i4fxrv5tshzjZ6IV886zspsSQhOtstpRyYrxJBgxukwlmaJgeW6yYUzSXUMS-QzyFRrBMqX2BRef5icgB357IWcmBsmYfM2lltz4ju3v8mgyBfvZOZwac7jWj3ko0gZc_pRouooUWQzB0O3iluuMRw6Scvfj" // Replace with actual access token if needed
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