package com.agentic.quartet.kisan.data.repository

import com.agentic.quartet.kisan.data.remote.MarketApiService
import com.agentic.quartet.kisan.domain.model.MarketPrice
import com.agentic.quartet.kisan.domain.repository.MarketRepository


class MarketRepositoryImpl(
    private val apiService: MarketApiService
) : MarketRepository {
    override suspend fun fetchMarketPrice(commodity: String): MarketPrice {
        val response = apiService.getMarketPrice(commodity)
        return MarketPrice(
            commodity = response.commodity,
            price = response.price,
            unit = response.unit
        )
    }
}