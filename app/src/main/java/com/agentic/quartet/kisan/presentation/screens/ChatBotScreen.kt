package com.agentic.quartet.kisan.presentation.screens

import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import kotlinx.serialization.json.JsonPrimitive
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject

@Serializable
data class DialogflowRequest(
    @SerialName("query_input")
    val queryInput: QueryInput
)

@Serializable
data class QueryInput(
    val text: TextInput,
    @SerialName("language_code")
    val languageCode: String
)

@Serializable
data class TextInput(val text: String)

@Serializable
data class DialogflowResponse(
    @SerialName("queryResult")
    val queryResult: QueryResult? = null
)

@Serializable
data class QueryResult(
    @SerialName("fulfillmentText")
    val fulfillmentText: String? = null
)

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
}

@Composable
fun ChatbotScreen() {
    val context = LocalContext.current
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val messages = remember { mutableStateListOf<Pair<Boolean, String>>() }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spokenText =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                input = TextFieldValue(spokenText)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F1F1))
            .padding(16.dp)
    ) {
        Text("AI Chatbot", modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
        ) {
            items(messages.size) { index ->
                val (isUser, msg) = messages[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUser) Color(0xFFDCF8C6) else Color.White
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        LaunchedEffect(messages.size) {
            scrollState.animateScrollToItem(messages.size)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                }
                launcher.launch(intent)
            }) {
                Icon(Icons.Default.Send, contentDescription = "Mic")
            }

            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask something...") }
            )

            IconButton(onClick = {
                val text = input.text.trim()
                if (text.isNotEmpty()) {
                    messages.add(true to text)
                    coroutineScope.launch {
                        val reply = sendMessageToDialogflowCX(text)
                        messages.add(false to (reply ?: "No response from Dialogflow"))
                    }
                    input = TextFieldValue("")
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

suspend fun sendMessageToDialogflowCX(userMessage: String): String {
    val endpoint = "https://global-dialogflow.googleapis.com/v3/projects/forward-alchemy-465709-k7/locations/global/agents/696d1ed8-adef-46da-bbf9-9d7e1d16bdb5/sessions/test-session-001:detectIntent"
    val accessToken = "ya29.c.c0ASRK0GYKgQ-w9HF771gI4j60YBAN2XKtMevAKf4OqxYP0dHmsrEGGnM3g5P-djVtzhYYxm2LZSODW-TQd__B6NIb1JvCm5jGVe2EQYOW_cIKPIG8lsyY29iaJR01INdS7_lwF7k3HCMEuk67lrrvJmF-dcmcU3IhXOkTUFzEiLdE75gVCSCSNx9AGN0EzDCz2ostTgJDHgrSfwkwfqcdialrMTMxqU6kA38LJ7QtoNf6zYgzqmNgizKmYt2H21KhPb7boPrlbI7YqIAptWWvL02xeCoF4fJvmWcoTPz40LSjg5GSwVMm2So0cediammW_29loo6UtTaA0-IQVYCmSS_7HpQbXKQRsXhh-qgdhyhlyk9P_FPLwyIzIQE387DF5z4ROgUcl38_k_yIOBXRlQlQzgg5wc4WSq1vMa3O47VibhlWmw-jf_ZlYcgWeSj9iYeq2k2aZjM_IvB3wk87OgBQJx7eVz2eR04WngZMB5YbjUvr5Ysf858bxbw8OcYV7kB_5xehr3_YQ1kI-X0mlzul960lOlbh9jIz54a59ixb0qavZuaOcqnOz05IM9XxyOWpY3MIUZqlMSsy1412S2bquBnctg5Mz711Z3muxxIOQcmrlk44SUZqIk-rYzQ4V4iblQqkzswMR7W9bZwR6wxw9n9zBarqO0uhpO923jY_2YWgi5ZOqt4_bl0Vnmtd3U8RcfB-42be2XXlBOW6l8XbI3ujuR4yIFc6xl1n6ObXbnk3acZpskYgpR_Vqbhf-x6orbXtF3aRlafgJ0Jf8tycIes--uJgu_fn8-m_5wRbxQ6xxoB_uQh4Ilg5-sX6-q87mZsZmve8xno424Ud6WcQ5RQVJx76Faf54iVdw0l8dYz8pwZrxpQO0RW09ktSvmyJy5c8oOinR1zm3u4vBBbgUxJ0tz3dJh4VU08fBxozwt8l3mW2Wcp4yMBexRV7F2tawZV22OoUWk3hZvas7XwxOJFnJbX7hwZXp41wffuIp88yxSWprgt" // Replace with actual bearer token from service account or OAuth flow

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // CX-compliant request body
    val requestBody = buildJsonObject {
        putJsonObject("queryInput") {
            putJsonObject("text") {
                put("text", JsonPrimitive(userMessage))
            }
            put("languageCode", JsonPrimitive("en")) // Or "hi", "kn", etc.
        }
    }

    return try {
        val response: JsonObject = client.post(endpoint) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
                append(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            setBody(requestBody)
        }.body()

        val responseMessages = response["queryResult"]
            ?.jsonObject?.get("responseMessages")
            ?.jsonArray

        val text = responseMessages?.firstOrNull()
            ?.jsonObject?.get("text")
            ?.jsonObject?.get("text")
            ?.jsonArray?.firstOrNull()
            ?.jsonPrimitive?.contentOrNull

        text ?: "No response from Dialogflow."
    } catch (e: Exception) {
        "Dialogflow error: ${e.message}"
    } finally {
        client.close()
    }
}