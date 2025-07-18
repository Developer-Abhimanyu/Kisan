package com.agentic.quartet.kisan.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FarmerProfile(
    val name: String = "",
    val phone: String = "",
    val city: String = "",
    val state: String = "",
    val pinCode: String = "",
    val farmingSource: String = ""
)