package com.lksnext.ParkingAAldai

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun registerUser(email: String, pass: String): Boolean {
        if (prefs.contains(email)) return false // El usuario ya existe
        prefs.edit().putString(email, pass).apply()
        return true
    }

    fun loginUser(email: String, pass: String): Boolean {
        val savedPass = prefs.getString(email, null)
        return savedPass == pass
    }
}