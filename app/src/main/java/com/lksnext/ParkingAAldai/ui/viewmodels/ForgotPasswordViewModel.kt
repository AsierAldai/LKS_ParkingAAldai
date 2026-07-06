package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingAAldai.auth.AuthDataSource
import com.lksnext.ParkingAAldai.validation.AuthValidator

class ForgotPasswordViewModel(
    private val authManager: AuthDataSource
) : ViewModel() {

    var emailValue = mutableStateOf("")
    var successMessage = mutableStateOf("")
    var errorMessage = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    fun resetPassword() {
        errorMessage.value = ""
        successMessage.value = ""

        val email = emailValue.value.trim()

        if (email.isBlank()) {
            errorMessage.value = "Ingresa tu correo electrónico"
            return
        }

        AuthValidator.validateCorporateEmail(email)?.let {
            errorMessage.value = it
            return
        }

        isLoading.value = true

        authManager.sendPasswordResetEmail(email) { success, firebaseError ->
            isLoading.value = false
            if (success) {
                successMessage.value = "Se ha enviado un enlace de recuperación a tu $email"
            } else {
                errorMessage.value = firebaseError ?: "No se pudo enviar el enlaec de recuperación"
            }
        }
    }
}