package com.agentic.quartet.kisan.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MarketPriceResponse(
    val commodity: String,
    val price: Double,
    val unit: String
)