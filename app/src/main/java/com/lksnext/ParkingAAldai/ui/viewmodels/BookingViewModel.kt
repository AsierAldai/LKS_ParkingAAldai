package com.lksnext.ParkingAAldai.ui.viewmodels


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingAAldai.auth.AuthDataSource
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.data.repository.ParkingRepository
import com.lksnext.ParkingAAldai.ui.components.getSpotPrefix
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import com.lksnext.ParkingAAldai.ui.screens.normalizeToStartOfDay
import com.lksnext.ParkingAAldai.validation.BookingValidator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookingViewModel(
    private val repo: ParkingRepository,
    private val authManager: AuthDataSource
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
        val validationError = BookingValidator.validateReservation(
            spotIndex = spotIndex,
            spotType = spotType,
            selectedDateMillis = selectedDateMillis,
            vehicle = vehicle,
            start = start,
            end = end,
            todayMillis = System.currentTimeMillis().normalizeToStartOfDay()
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

                if (BookingValidator.hasOverlap(start, end, existingReservations)) {
                    errorMessage.value = "Esta plaza ya está ocupada en ese horario"
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
}