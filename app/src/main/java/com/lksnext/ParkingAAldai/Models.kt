package com.lksnext.ParkingAAldai

data class UserEntity(
    val email: String = "",
    val name: String = "",
    val username: String = "",
    val phone: String = ""
)

data class VehicleEntity(
    val id: Int = 0,
    val ownerEmail: String = "",
    val plate: String = "",
    val brand: String = "",
    val color: String = "",
    val type: String = ""
)

data class ReservationEntity(
    val id: Int = 0,
    val userEmail: String = "",
    val spotIndex: Int = 0,
    val spotType: String = "",
    val dateMillis: Long = 0L,
    val startTime: String = "",
    val endTime: String = "",
    val vehiclePlate: String = "",
    val reservationName: String = ""
)

data class NotificationEntity(
    val id: Int = 0,
    val userEmail: String = "",
    val title: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
