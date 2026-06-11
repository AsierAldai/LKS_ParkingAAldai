package com.lksnext.ParkingAAldai

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_LOGGED_IN_USER = "current_user_email"
    }

    fun registerWithFirebase(email: String, pass: String, onResulet: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.edit().putString(KEY_LOGGED_IN_USER, email).apply()
                    onResulet(true, null)
                } else {
                    onResulet(false, task.exception?.message)
                }
            }
    }

    fun registerUser(email: String, pass: String): Boolean {
        if (prefs.contains(email)) return false
        prefs.edit().putString(email, pass).apply()
        return true
    }

    fun loginUser(email: String, pass: String): Boolean {
        val savedPass = prefs.getString(email, null)
        if (savedPass != null && savedPass == pass) {
            prefs.edit().putString(KEY_LOGGED_IN_USER, email).apply()
            return true
        }
        return false
    }

    fun getUserEmail(): String? = prefs.getString(KEY_LOGGED_IN_USER, null)

    fun logout(){
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }

    fun updateSession(oldEmail: String, newEmail: String) {
        val password = prefs.getString(oldEmail, "") ?: ""

        prefs.edit().run {
            if (oldEmail != newEmail) {
                remove(oldEmail)
                putString(newEmail, password)
                if (getUserEmail() == oldEmail){
                    putString(KEY_LOGGED_IN_USER, newEmail)
                }
            }
            apply()
        }
    }
}