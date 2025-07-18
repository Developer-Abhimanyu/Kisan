package com.agentic.quartet.kisan.utils

import android.content.Context
import com.agentic.quartet.kisan.data.model.FarmerProfile

object ProfileManager {
    private const val PROFILE = "profile"

    fun saveProfile(context: Context, profile: FarmerProfile) {
        context.getSharedPreferences(PROFILE, Context.MODE_PRIVATE).edit()
            .putString("name", profile.name)
            .putString("phone", profile.phone)
            .putString("city", profile.city)
            .putString("state", profile.state)
            .putString("pinCode", profile.pinCode)
            .putString("farmingSource", profile.farmingSource)
            .apply()
    }

    fun loadProfile(context: Context): FarmerProfile {
        val prefs = context.getSharedPreferences(PROFILE, Context.MODE_PRIVATE)
        return FarmerProfile(
            name = prefs.getString("name", "") ?: "",
            phone = prefs.getString("phone", "") ?: "",
            city = prefs.getString("city", "") ?: "",
            state = prefs.getString("state", "") ?: "",
            pinCode = prefs.getString("pinCode", "") ?: "",
            farmingSource = prefs.getString("farmingSource", "") ?: ""
        )
    }
}