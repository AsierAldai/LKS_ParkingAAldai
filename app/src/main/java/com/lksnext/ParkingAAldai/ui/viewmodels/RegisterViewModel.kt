package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.lksnext.ParkingAAldai.auth.AuthManager
import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.repository.FirebaseRepository

class RegisterViewModel (
    private val authManager: AuthManager,
    private val repo: FirebaseRepository
) : ViewModel() {
    var name = mutableStateOf("")
    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var phone = mutableStateOf("")
    var password = mutableStateOf("")
    var confirmPassword = mutableStateOf("")
    var errorMessage = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    fun register(onSuccess: () -> Unit) {
        errorMessage.value = ""

        if (name.value.isBlank()) {
            errorMessage.value  = "El nombre no puede estar vacío"
            return
        }

        if (username.value.isBlank()) {
            errorMessage.value  = "El nombre de usuario no puede estar vacío"
            return
        }
        if (!email.value.endsWith("@lks.com")) {
            errorMessage.value  = "Usa tu correo corporativo @lks.com"
            return
        }
        if(password.value.length < 6) {
            errorMessage.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }
        if (password.value != confirmPassword.value) {
            errorMessage.value = "Las contraseñas no coinciden"
            return
        }

        isLoading.value = true

        authManager.registerWithFirebase(email.value, password.value) { success, firebaseError ->
            if (success) {
                viewModelScope.launch {
                    repo.insertUser(
                        UserEntity(
                            email = email.value,
                            name = name.value,
                            username = username.value,
                            phone = phone.value
                        )
                    )
                    isLoading.value = false
                    onSuccess()
                }
            } else {
                isLoading.value = false
                errorMessage.value = firebaseError ?: "Error al registrar el usuario"
            }
        }
    }
}