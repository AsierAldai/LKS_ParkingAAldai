package com.lksnext.ParkingAAldai.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class AuthManager(context: Context) : AuthDataSource {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_LOGGED_IN_USER = "current_user_email"
    }

    override fun registerWithFirebase(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Ingresa tu correo electrónico")
            return
        }
        if (password.isBlank()) {
            onResult(false, "Ingresa tu contraseña")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.edit().putString(KEY_LOGGED_IN_USER, email).apply()
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    override fun loginWithFirebase(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Ingresa tu correo electrónico")
            return
        }
        if (password.isBlank()) {
            onResult(false, "Ingresa tu contraseña")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    prefs.edit().putString(KEY_LOGGED_IN_USER, email).apply()
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    override fun logoutWithFirebase(){
        auth.signOut()
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }

    override fun getUserEmailWithFirebase() : String?{
        return auth.currentUser?.email ?: prefs.getString(KEY_LOGGED_IN_USER, null)
    }

    override fun updateSessionWithFirebase(newEmail: String, currentPassword: String, onResult: (Boolean, String?) -> Unit) {
        if (newEmail.isBlank()) {
            onResult(false, "Ingresa tu correo electrónico")
            return
        }
        if (currentPassword.isBlank()) {
            onResult(false, "Ingresa tu contraseña actual")
            return
        }

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

    override fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (email.isBlank()) {
            onResult(false, "Ingresa tu correo electrónico")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
}