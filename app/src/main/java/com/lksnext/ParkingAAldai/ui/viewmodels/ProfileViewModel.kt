package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingAAldai.auth.AuthDataSource
import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.data.repository.ParkingRepository
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ParkingRepository,
    private val authManager: AuthDataSource
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
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())

    init {
        loadUserData()
    }

    fun loadUserData() {
        val currentEmail = getCurrentEmail()

        if (currentEmail.isBlank()) {
            email.value = ""
            name.value = "Usuario"
            username.value = ""
            phone.value = ""
            _refreshTrigger.value = System.currentTimeMillis()
            return
        }

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