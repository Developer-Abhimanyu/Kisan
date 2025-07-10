package com.agentic.quartet.kisan.data.remote

import com.agentic.quartet.kisan.data.model.MarketPriceResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class MarketApiService(private val client: HttpClient) {

    suspend fun getMarketPrice(commodity: String): MarketPriceResponse {
        val response: HttpResponse = client.get("https://mock-api.com/market-price/$commodity")
        return response.body()
    }
}