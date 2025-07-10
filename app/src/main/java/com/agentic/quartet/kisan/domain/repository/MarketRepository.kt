package com.agentic.quartet.kisan.domain.repository

import com.agentic.quartet.kisan.domain.model.MarketPrice

interface MarketRepository {
    suspend fun fetchMarketPrice(commodity: String): MarketPrice
}