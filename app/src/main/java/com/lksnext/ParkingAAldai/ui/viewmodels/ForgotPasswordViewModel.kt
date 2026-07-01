package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ForgotPasswordViewModel : ViewModel() {

    var selectedMethod = mutableStateOf("email")
    var emailValue = mutableStateOf("")
    var phoneValue = mutableStateOf("")
    var successMessage = mutableStateOf("")
    var errorMessage = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    fun resetPassword() {
        errorMessage.value = ""
        successMessage.value = ""

        if (selectedMethod.value == "email") {
            if (emailValue.value.isBlank()) {
                errorMessage.value = "Ingresa tu correo electrónico"
                return
            }
        } else {
            if (phoneValue.value.isBlank()) {
                errorMessage.value = "Ingresa tu número de teléfono"
                return
            }
        }

        isLoading.value = true
        successMessage.value = if (selectedMethod.value == "email") {
            "Se ha enviado un enlace de recuperación a tu ${emailValue.value}"
        } else {
            "Se ha enviado un código a ${phoneValue.value}"
        }
        isLoading.value = false
    }
}