package com.lksnext.ParkingAAldai

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN_USER = "current_user_email"
        private const val PREFIX_USER_MAP = "user_map_"
    }

    fun registerUser(email: String, username: String, pass: String): Boolean {
        if (prefs.contains(email) || prefs.contains(PREFIX_USER_MAP + username)) return false // El usuario ya existe
        prefs.edit().apply{
            putString(email, pass)
            putString(PREFIX_USER_MAP + username, email)
            apply()
        }
        return true
    }

    fun loginUser(identifier: String, pass: String): Boolean {
        val emailMapping = prefs.getString(PREFIX_USER_MAP + identifier, null)

        val targetEmail = emailMapping ?: identifier

        val savedPass = prefs.getString(targetEmail, null)

        if (savedPass != null && savedPass == pass) {
            prefs.edit().putString(KEY_LOGGED_IN_USER, targetEmail).apply()
            return true
        }
        return false
    }

    fun getUserEmail(): String? = prefs.getString(KEY_LOGGED_IN_USER, null)

    fun logout(){
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }
}