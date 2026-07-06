package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.screens.SpotType
import com.lksnext.ParkingAAldai.ui.screens.normalizeToStartOfDay
import com.lksnext.ParkingAAldai.ui.viewmodels.BookingViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class BookingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = System.currentTimeMillis().normalizeToStartOfDay()

    private val vehicle = VehicleEntity(
        ownerEmail = "user@lks.com",
        plate = "1234ABC",
        brand = "Toyota",
        type = SpotType.COMBUSTION.name
    )

    @Test
    fun createReservationWithInvalidSpot_setsError() {
        val viewModel = BookingViewModel(FakeParkingRepository(), FakeAuthDataSource())

        viewModel.createReservation(
            spotIndex = -1,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = vehicle,
            start = "08:00",
            end = "10:00",
            onSuccess = {}
        )

        assertEquals("Plaza inválida", viewModel.errorMessage.value)
    }

    @Test
    fun createReservationWithOverlap_setsError() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        repo.reservations.add(
            ReservationEntity(
                userEmail = "other@lks.com",
                spotIndex = 3,
                spotType = SpotType.COMBUSTION.name,
                dateMillis = today,
                vehiclePlate = "0000OLD",
                startTime = "09:00",
                endTime = "11:00"
            )
        )
        val viewModel = BookingViewModel(repo, FakeAuthDataSource())

        viewModel.createReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = vehicle,
            start = "10:00",
            end = "12:00",
            onSuccess = {}
        )
        testScheduler.advanceUntilIdle()

        assertEquals("Esta plaza ya está ocupada en ese horario", viewModel.errorMessage.value)
        assertEquals(1, repo.reservations.size)
    }

    @Test
    fun createReservationSuccess_insertsReservationAndNotification() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val viewModel = BookingViewModel(repo, FakeAuthDataSource("user@lks.com"))
        var successCalled = false

        viewModel.reservationName.value = "Mi reserva"

        viewModel.createReservation(
            spotIndex = 3,
            spotType = SpotType.COMBUSTION,
            selectedDateMillis = today,
            vehicle = vehicle,
            start = "08:00",
            end = "10:00",
            onSuccess = { successCalled = true }
        )
        testScheduler.advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
        assertEquals(1, repo.reservations.size)
        assertEquals("Mi reserva", repo.reservations.first().reservationName)
        assertEquals(1, repo.notifications.size)
    }
}