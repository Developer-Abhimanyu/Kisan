package com.agentic.quartet.kisan.domain.usecase

import com.agentic.quartet.kisan.domain.model.MarketPrice
import com.agentic.quartet.kisan.domain.repository.MarketRepository

class GetMarketPriceUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(commodity: String): MarketPrice {
        return repository.fetchMarketPrice(commodity)
    }
}