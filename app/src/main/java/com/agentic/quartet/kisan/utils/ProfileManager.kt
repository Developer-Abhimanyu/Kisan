package com.agentic.quartet.kisan.utils

import android.content.Context
import com.agentic.quartet.kisan.data.model.FarmerProfile

object ProfileManager {
    private const val PREF_NAME = "farmer_profile"

    fun saveProfile(context: Context, profile: FarmerProfile) {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("name", profile.name)
            putString("phone", profile.phone)
            putString("city", profile.city)
            putString("state", profile.state)
            putString("pinCode", profile.pinCode)
            putString("farmingSource", profile.farmingSource)
            apply()
        }
    }

    fun getProfile(context: Context): FarmerProfile? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val name = sharedPref.getString("name", null) ?: return null
        val phone = sharedPref.getString("phone", null) ?: return null
        val city = sharedPref.getString("city", null) ?: return null
        val state = sharedPref.getString("state", null) ?: return null
        val pinCode = sharedPref.getString("pinCode", null) ?: return null
        val farmingSource = sharedPref.getString("farmingSource", null) ?: return null

        return FarmerProfile(name, phone, city, state, pinCode, farmingSource)
    }
}