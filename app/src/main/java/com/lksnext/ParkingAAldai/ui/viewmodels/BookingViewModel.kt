package com.lksnext.ParkingAAldai.ui.viewmodels


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingAAldai.auth.AuthManager
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.data.repository.FirebaseRepository
import com.lksnext.ParkingAAldai.ui.components.getSpotPrefix
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import com.lksnext.ParkingAAldai.ui.screens.normalizeToStartOfDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookingViewModel(
    private val repo: FirebaseRepository,
    private val authManager: AuthManager
) : ViewModel() {
    var reservationName = mutableStateOf("")
    var errorMessage = mutableStateOf("")
    var isLoading = mutableStateOf(false)

    private fun getCurrentEmail() = authManager.getUserEmailWithFirebase() ?: ""

    fun getAllReservationsByDate(dateMillis: Long) =
        repo.getAllReservationsByDate(dateMillis)

    fun getFutureReservationsBySpot(spotIndex: Int, dateMillis: Long) =
        repo.getFutureReservationsBySpot(spotIndex, dateMillis)

    fun createReservation(
        spotIndex: Int,
        spotType: SpotType,
        selectedDateMillis: Long,
        vehicle: VehicleEntity,
        start: String,
        end: String,
        onSuccess: () -> Unit
    ) {
        val validationError = validateReservation(
            spotIndex = spotIndex,
            spotType = spotType,
            selectedDateMillis = selectedDateMillis,
            vehicle = vehicle,
            start = start,
            end = end
        )
        if (validationError != null) {
            errorMessage.value = validationError
            return
        }

        viewModelScope.launch {
            isLoading.value = true

            try {
                val existingReservations = repo
                    .getFutureReservationsBySpot(spotIndex, selectedDateMillis)
                    .first()
                    .filter { it.dateMillis == selectedDateMillis }

                if (existingReservations.any { overlaps(start, end, it.startTime, it.endTime) }) {
                    errorMessage.value = "Esta plaza ya esta ocupada en ese horario"
                    return@launch
                }

                val reservation = ReservationEntity(
                    userEmail = getCurrentEmail(),
                    spotIndex = spotIndex,
                    spotType = spotType.name,
                    dateMillis = selectedDateMillis,
                    startTime = start,
                    endTime = end,
                    vehiclePlate = vehicle.plate,
                    reservationName = reservationName.value.ifBlank { "Reserva en plaza $spotIndex" }
                )

                repo.insertReservation(reservation)

                repo.insertNotification(
                    NotificationEntity(
                        userEmail = getCurrentEmail(),
                        title = "Tu reserva en la plaza ${getSpotPrefix(spotType)}-$spotIndex ha sido confirmada",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )

                errorMessage.value = ""
                onSuccess()
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun validateReservation(
        spotIndex: Int,
        spotType: SpotType,
        selectedDateMillis: Long,
        vehicle: VehicleEntity,
        start: String,
        end: String
    ): String? {
        if (spotIndex < 0) return "Plaza inválida"

        val todayMillis = System.currentTimeMillis().normalizeToStartOfDay()
        val maxDateMillis = todayMillis + (7L * 24 * 60 * 60 * 1000L)

        if (selectedDateMillis !in todayMillis..maxDateMillis) {
            return "Solo puedes reservar entre hoy y los próximos 7 días"
        }

        val startMinutes = timeToMinutesOrNull(start) ?: return "Formato de hora inválido"
        val endMinutes = timeToMinutesOrNull(end) ?: return "Formato de hora inválido"

        val limitStart = 8 * 60
        val limitEnd = 19 * 60

        if (startMinutes < limitStart || endMinutes > limitEnd) {
            return "Solo puedes reservar entre las 08:00 y las 19:00"
        }

        if (endMinutes <= startMinutes) {
            return "La hora de fin debe ser mayor que la de inicio"
        }

        if (endMinutes - startMinutes > 9 * 60) {
            return "La reserva no puede superar las 9 horas"
        }

        if (!isVehicleCompatible(vehicle, spotType)) {
            return "El vehículo no es compatible con el tipo de plaza"
        }

        return null
    }

    private fun overlaps(
        newStart: String,
        newEnd: String,
        existingStart: String,
        existingEnd: String
    ): Boolean {
        val start = timeToMinutesOrNull(newStart) ?: return true
        val end = timeToMinutesOrNull(newEnd) ?: return true
        val otherStart = timeToMinutesOrNull(existingStart) ?: return true
        val otherEnd = timeToMinutesOrNull(existingEnd) ?: return true

        return start < otherEnd && end > otherStart
    }

    private fun isVehicleCompatible(vehicle: VehicleEntity, spotType: SpotType): Boolean {
        return when (spotType) {
            SpotType.MOTORCYCLE -> vehicle.type == SpotType.MOTORCYCLE.name
            SpotType.DISABLED -> vehicle.type == SpotType.DISABLED.name
            SpotType.COMBUSTION,
            SpotType.ELECTRIC -> vehicle.type == SpotType.COMBUSTION.name || vehicle.type == SpotType.ELECTRIC.name
        }
    }

    private fun timeToMinutesOrNull(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null

        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null

        if (hour !in 0..23 || minute !in 0..59) return null

        return hour * 60 + minute
    }
}