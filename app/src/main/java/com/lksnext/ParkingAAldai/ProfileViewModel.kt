package com.lksnext.ParkingAAldai

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val repository: FirebaseRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private fun getCurrentEmail() = authManager.getUserEmailWithFirebase() ?: ""

    // Información Personal
    var name = mutableStateOf("Usuario")
    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var phone = mutableStateOf("")

    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    val vehicles: StateFlow<List<VehicleEntity>> = _refreshTrigger
        .flatMapLatest { _ -> repository.getVehiclesByUser(getCurrentEmail()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadUserData()
    }

    fun loadUserData() {
        val currentEmail = getCurrentEmail()
        email.value = currentEmail
        _refreshTrigger.value = System.currentTimeMillis()

        viewModelScope.launch {
            val user = repository.getUser(currentEmail)
            if (user != null) {
                name.value = user.name
                username.value = user.username
                phone.value = user.phone
            } else {
                // Reset si no hay usuario
                name.value = "Usuario"
                username.value = ""
                phone.value = ""
            }
        }
    }

    fun addVehicle(plate: String, brand: String, color: String, type: SpotType) {
        viewModelScope.launch {
            repository.insertVehicle(
                VehicleEntity(
                    ownerEmail = getCurrentEmail(),
                    plate = plate,
                    brand = brand,
                    color = color,
                    type = type.name
                )
            )
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }

    var errorMessage = mutableStateOf("")

    fun updateProfile(newName: String, newUsername: String, newEmail: String, newPhone: String, currentPassword: String) {
        val oldEmail = getCurrentEmail()
        authManager.updateSessionWithFirebase(newEmail, currentPassword) { success, firebaseError ->
            if (success) {
                errorMessage.value = ""

                viewModelScope.launch {
                    if (oldEmail != newEmail) {
                        repository.updateVehiclesOwnerEmail(oldEmail, newEmail)
                        repository.deleteUserByEmail(oldEmail)
                    }
                    val updatedUser = UserEntity(newEmail, newName, newUsername, newPhone)
                    repository.insertUser(updatedUser)
                    loadUserData()
                }
            } else {
                errorMessage.value = firebaseError ?: "Error al actualizar el perfil."
            }
        }
    }

    fun logout() {
        authManager.logoutWithFirebase()
        name.value = "Usuario"
        email.value = ""
    }
}