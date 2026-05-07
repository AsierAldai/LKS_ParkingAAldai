package com.lksnext.ParkingAAldai

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


class ProfileViewModel : ViewModel() {
    // Información Personal
    var name = mutableStateOf("Asier")
    var username = mutableStateOf("Asier05")
    var email = mutableStateOf("a***@empresa.com")
    var phone = mutableStateOf("No especificado")


    val vehicles = mutableStateListOf<Vehicle>()

    fun addVehicle(v: Vehicle) {
        vehicles.add(v)
    }

    fun deleteVehicle(v: Vehicle) {
        vehicles.remove(v)
    }
}

data class Vehicle(
    val id: String = java.util.UUID.randomUUID().toString(), // ID único
    val plate: String,
    val type: SpotType,
    val brand: String,
    val color: String
)