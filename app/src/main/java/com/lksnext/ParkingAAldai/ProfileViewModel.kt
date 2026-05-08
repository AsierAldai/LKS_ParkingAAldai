package com.lksnext.ParkingAAldai

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val dao: AppDao,
    private val authManager: AuthManager
) : ViewModel() {

    private fun getCurrentEmail() = authManager.getUserEmail() ?: ""

    // Información Personal
    var name = mutableStateOf("Usuario")
    var username = mutableStateOf("")
    var email = mutableStateOf("")
    var phone = mutableStateOf("")

    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    val vehicles: StateFlow<List<VehicleEntity>> = _refreshTrigger
        .flatMapLatest { email -> dao.getVehiclesByUser(getCurrentEmail()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadUserData()
    }
    fun loadUserData() {
        val currentEmail = getCurrentEmail()
        email.value = currentEmail
        _refreshTrigger.value = System.currentTimeMillis()

        viewModelScope.launch {
            val user = dao.getUser(currentEmail)
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
            dao.insertVehicle(
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
            dao.deleteVehicle(vehicle)
        }
    }

    fun updateProfile(newName: String, newUsername: String, newEmail: String, newPhone: String) {
        viewModelScope.launch {
            val oldEmail= getCurrentEmail()
            authManager.updateSession(oldEmail, newEmail, newUsername)
            if (oldEmail != newEmail) {
                dao.updateVehiclesOwnerEmail(oldEmail, newEmail)
                dao.deleteUserByEmail(oldEmail)
            }
            val updatedUser = UserEntity(newEmail, newName, newUsername, newPhone)
            dao.insertUser(updatedUser)
            loadUserData()
        }
    }

    fun logout() {
        authManager.logout()
        name.value = "Usuario"
        email.value = ""
    }
}


data class Vehicle(
    val id: String = java.util.UUID.randomUUID().toString(), // ID único
    val plate: String,
    val type: SpotType,
    val brand: String,
    val color: String
)