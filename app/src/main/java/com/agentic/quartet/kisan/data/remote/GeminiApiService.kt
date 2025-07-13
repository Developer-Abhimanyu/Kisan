package com.agentic.quartet.kisan.data.remote

import io.ktor.http.contentType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.engine.cio.*
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import org.json.JSONObject

class GeminiApiService(private val apiKey: String) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
    }

    suspend fun getPriceAdvice(marketData: String): String {
        val response: HttpResponse = client.post(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "contents" to listOf(
                        mapOf(
                            "parts" to listOf(
                                mapOf(
                                    "text" to """
                                    Summarize the following market price data for a small-scale farmer in India in simple English.
                                    Give an actionable recommendation on whether to sell or wait.

                                    Data:
                                    $marketData
                                """.trimIndent()
                                )
                            )
                        )
                    )
                )
            )
        }

        val body = response.bodyAsText()
        return JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    suspend fun getGovernmentSchemeAdvice(userQuestion: String): String {
        val response: HttpResponse = client.post(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"
        ) {
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(
                mapOf(
                    "contents" to listOf(
                        mapOf(
                            "parts" to listOf(
                                mapOf(
                                    "text" to """
                                You are an agricultural government scheme assistant for Indian farmers.

                                Answer the following question using simple, easy-to-understand language:
                                "$userQuestion"

                                If applicable, explain:
                                1. Scheme Name.
                                2. Eligibility.
                                3. Benefits.
                                4. Application Process (brief).

                                Keep it short and easy to understand.
                            """.trimIndent()
                                )
                            )
                        )
                    )
                )
            )
        }

        val body = response.bodyAsText()
        return JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    suspend fun diagnoseDisease(base64Image: String): String {
        val promptText = """
            You are an expert agricultural assistant specialized in Indian crops and farming conditions.

            Please analyze the uploaded plant image and identify any visible diseases, pest infections, or nutrient deficiencies.

            Instructions:
            1. Focus on common Indian crops such as tomato, rice, wheat, cotton, sugarcane, pulses, and vegetables.
            2. Identify the disease or pest name (in simple terms).
            3. Recommend a simple, affordable treatment or remedy suitable for small-scale Indian farmers.
            4. Suggest preventive measures, if possible, using low-cost, organic, or widely available solutions.
            5. Keep the response short, practical, and easy to understand.

            Return the answer in the following format:
            - Disease/Pest Name:
            - Recommended Solution:
            - Preventive Tips:
        """.trimIndent()

        val response: HttpResponse = client.post(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent?key=$apiKey"
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "contents" to listOf(
                        mapOf(
                            "parts" to listOf(
                                mapOf("text" to promptText),
                                mapOf(
                                    "inlineData" to mapOf(
                                        "mimeType" to "image/jpeg",
                                        "data" to base64Image
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }

        val body = response.bodyAsText()
        return JSONObject(body)
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }
}