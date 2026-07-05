package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingAAldai.auth.AuthDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.repository.ParkingRepository

class MyBookingsViewModel(
    private val repo: ParkingRepository,
    private val authManager: AuthDataSource
) : ViewModel() {

    private fun getCurrentEmail() = authManager.getUserEmailWithFirebase() ?: ""
    private val minDateMillis = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)

    val bookings: StateFlow<List<ReservationEntity>> =
        repo.getReservationsByUser(getCurrentEmail(), minDateMillis)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun cancelBooking(reservation: ReservationEntity) {
        viewModelScope.launch {
            repo.deleteReservation(reservation)
        }
    }

    fun editBooking(reservation: ReservationEntity, newName: String, newStartTime: String, newEndTime: String) {
        viewModelScope.launch {
            repo.updateReservation(
                reservation.copy(
                    reservationName = newName,
                    startTime = newStartTime,
                    endTime = newEndTime
                )
            )
        }
    }

    fun getOtherReservationsForSpot(spotIndex: Int, dateMillis: Long, excludedId: Int) =
        repo.getOtherReservationsForSpot(spotIndex, dateMillis, excludedId)
}