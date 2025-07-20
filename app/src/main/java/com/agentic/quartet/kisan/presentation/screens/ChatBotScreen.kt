package com.agentic.quartet.kisan.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

@Composable
fun ChatBotScreen(onBack: () -> Unit) {
    var userMessage by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf("Welcome to Kisan AI Chatbot 👨‍🌾")) }

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
                items(messages) { msg ->
                    val isUser = msg.startsWith("👤")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Text(
                            text = msg.removePrefix("👤 ").removePrefix("🤖 "),
                            color = Color(0xFF4CAF50),
                            modifier = Modifier
                                .padding(8.dp)
                                .background(
                                    Color.White,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(12.dp)
                        )
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
                        messages = messages + "👤 $text"
                        sendMessageToDialogflow(text) { response ->
                            response?.let {
                                messages = messages + "🤖 $it"
                            } ?: run {
                                messages = messages + "🤖 Sorry, no response."
                            }
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

fun sendMessageToDialogflow(userMessage: String, onResult: (String?) -> Unit) {
    val accessToken =
        "ya29.c.c0ASRK0GYKgQ-w9HF771gI4j60YBAN2XKtMevAKf4OqxYP0dHmsrEGGnM3g5P-djVtzhYYxm2LZSODW-TQd__B6NIb1JvCm5jGVe2EQYOW_cIKPIG8lsyY29iaJR01INdS7_lwF7k3HCMEuk67lrrvJmF-dcmcU3IhXOkTUFzEiLdE75gVCSCSNx9AGN0EzDCz2ostTgJDHgrSfwkwfqcdialrMTMxqU6kA38LJ7QtoNf6zYgzqmNgizKmYt2H21KhPb7boPrlbI7YqIAptWWvL02xeCoF4fJvmWcoTPz40LSjg5GSwVMm2So0cediammW_29loo6UtTaA0-IQVYCmSS_7HpQbXKQRsXhh-qgdhyhlyk9P_FPLwyIzIQE387DF5z4ROgUcl38_k_yIOBXRlQlQzgg5wc4WSq1vMa3O47VibhlWmw-jf_ZlYcgWeSj9iYeq2k2aZjM_IvB3wk87OgBQJx7eVz2eR04WngZMB5YbjUvr5Ysf858bxbw8OcYV7kB_5xehr3_YQ1kI-X0mlzul960lOlbh9jIz54a59ixb0qavZuaOcqnOz05IM9XxyOWpY3MIUZqlMSsy1412S2bquBnctg5Mz711Z3muxxIOQcmrlk44SUZqIk-rYzQ4V4iblQqkzswMR7W9bZwR6wxw9n9zBarqO0uhpO923jY_2YWgi5ZOqt4_bl0Vnmtd3U8RcfB-42be2XXlBOW6l8XbI3ujuR4yIFc6xl1n6ObXbnk3acZpskYgpR_Vqbhf-x6orbXtF3aRlafgJ0Jf8tycIes--uJgu_fn8-m_5wRbxQ6xxoB_uQh4Ilg5-sX6-q87mZsZmve8xno424Ud6WcQ5RQVJx76Faf54iVdw0l8dYz8pwZrxpQO0RW09ktSvmyJy5c8oOinR1zm3u4vBBbgUxJ0tz3dJh4VU08fBxozwt8l3mW2Wcp4yMBexRV7F2tawZV22OoUWk3hZvas7XwxOJFnJbX7hwZXp41wffuIp88yxSWprgt" // Replace with actual access token if needed
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
                put("text", JsonPrimitive(userMessage))
            }
            put("languageCode", JsonPrimitive("en")) // or "hi", "kn" based on your app language
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

            val json = Json.parseToJsonElement(response.bodyAsText())
            val botReply = json
                .jsonObject["queryResult"]
                ?.jsonObject?.get("responseMessages")
                ?.jsonArray?.getOrNull(0)
                ?.jsonObject?.get("text")
                ?.jsonObject?.get("text")
                ?.jsonArray?.getOrNull(0)
                ?.jsonPrimitive?.contentOrNull

            onResult(botReply)

        } catch (e: Exception) {
            onResult("Error: ${e.message}")
        }
    }
}