package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.lksnext.ParkingAAldai.validation.AuthValidator

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

        AuthValidator.validatePassswordRecovery(
            selectedMethod = selectedMethod.value,
            email = emailValue.value,
            phone = phoneValue.value
        )?.let {
            errorMessage.value = it
            return
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