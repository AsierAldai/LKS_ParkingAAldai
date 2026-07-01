package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingAAldai.auth.AuthManager

class LoginViewModel (
    private val authManager: AuthManager
) : ViewModel() {
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var errorMessage = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    fun login(onSuccess: () -> Unit) {
        val emailTrimmed = email.value.trim()

        errorMessage.value = ""

        if (!emailTrimmed.endsWith("@lks.com")) {
            errorMessage.value = "Usa tu correo corporativo @lks.com"
            return
        }

        isLoading.value = true

        authManager.loginWithFirebase(emailTrimmed, password.value) { success, firebaseError ->
            isLoading.value = false
            if (success) {
                errorMessage.value = ""
                onSuccess()
            } else {
                errorMessage.value = firebaseError ?: "Correo o contraseña incorrectos"
            }
        }
    }
}