package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.ui.viewmodels.MyBookingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MyBookingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun bookings_containsCurrentUserReservations() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val reservation = ReservationEntity(
            id = 1,
            userEmail = "user@lksnext.com",
            spotIndex = 3,
            dateMillis = System.currentTimeMillis()
        )
        repo.reservations.add(reservation)

        val viewModel = MyBookingsViewModel(repo, FakeAuthDataSource("user@lksnext.com"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.bookings.collect {}
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(reservation), viewModel.bookings.value)
    }

    @Test
    fun cancelBooking_deletesReservation() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val reservation = ReservationEntity(
            id = 1,
            userEmail = "user@lksnext.com"
        )
        val viewModel = MyBookingsViewModel(repo, FakeAuthDataSource())

        viewModel.cancelBooking(reservation)
        testScheduler.advanceUntilIdle()

        assertEquals(reservation, repo.deletedReservation)
    }

    @Test
    fun editBooking_updatesReservationFields() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val reservation = ReservationEntity(
            id = 1,
            reservationName = "Old",
            startTime = "08:00",
            endTime = "10:00"
        )
        val viewModel = MyBookingsViewModel(repo, FakeAuthDataSource())

        viewModel.editBooking(
            reservation = reservation,
            newName = "New",
            newStartTime = "09:00",
            newEndTime = "11:00"
        )
        testScheduler.advanceUntilIdle()

        assertEquals("New", repo.updatedReservation?.reservationName)
        assertEquals("09:00", repo.updatedReservation?.startTime)
        assertEquals("11:00", repo.updatedReservation?.endTime)
    }

    @Test
    fun getOtherReservationsForSpot_excludesGivenId() = runTest {
        val repo = FakeParkingRepository()
        repo.reservations.add(
            ReservationEntity(
                id = 1,
                spotIndex = 3,
                dateMillis = 1000L
            )
        )
        repo.reservations.add(
            ReservationEntity(
                id = 2,
                spotIndex = 3,
                dateMillis = 1000L
            )
        )
        val viewModel = MyBookingsViewModel(repo, FakeAuthDataSource())

        val result = viewModel.getOtherReservationsForSpot(
            spotIndex = 3,
            dateMillis = 1000L,
            excludedId = 1
        ).first()

        assertEquals(1, result.size)
        assertEquals(2, result.first().id)
    }
}