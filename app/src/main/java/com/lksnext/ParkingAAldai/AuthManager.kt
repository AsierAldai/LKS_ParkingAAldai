package com.lksnext.ParkingAAldai

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_LOGGED_IN_USER = "current_user_email"
    }

    fun registerWithFirebase(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.edit().putString(KEY_LOGGED_IN_USER, email).apply()
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginWithFirebase(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.edit().putString(KEY_LOGGED_IN_USER, email).apply()
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun logoutWithFirebase(){
        auth.signOut()
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }

    fun getUserEmailWithFirebase() : String?{
        return auth.currentUser?.email ?: prefs.getString(KEY_LOGGED_IN_USER, null)
    }

    fun updateSessionWithFirebase(newEmail: String, currentPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email


        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.verifyBeforeUpdateEmail(newEmail)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    prefs.edit().putString(KEY_LOGGED_IN_USER, newEmail).apply()
                                    onResult(true, null)
                                } else {
                                    onResult(false, updateTask.exception?.message)
                                }
                            }
                    } else {
                        onResult(false, "Contraseña actual incorrecta. No se pudo reautenticar.")
                    }
            }
        } else {
            onResult(false, "No hay sesión activa.")
        }
    }
}