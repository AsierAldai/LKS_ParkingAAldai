package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.lksnext.ParkingAAldai.auth.AuthManager
import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.repository.FirebaseRepository
import com.lksnext.ParkingAAldai.validation.AuthValidator

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

        AuthValidator.validateRegister(
            name = name.value,
            username = username.value,
            email = email.value,
            password = password.value,
            confirmPassword = confirmPassword.value
        )?.let {
            errorMessage.value = it
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