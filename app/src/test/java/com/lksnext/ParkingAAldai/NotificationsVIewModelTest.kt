package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.ui.viewmodels.NotificationsViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NotificationsVIewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun notification_containsCurrentUserNotifications() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val notification = NotificationEntity(
            userEmail = "user@lksnext.com",
            title = "Reserva Confirmada"
        )
        repo.notifications.add(notification)

        val viewModel = NotificationsViewModel(repo, FakeAuthDataSource("user@lksnext.com"))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notifications.collect {}
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(notification), viewModel.notifications.value)
    }

    @Test
    fun markAllAsRead_callsRepositoryWithCurrentEmail() = runTest(mainDispatcherRule.testDispatcher) {
        val repo = FakeParkingRepository()
        val viewModel = NotificationsViewModel(repo, FakeAuthDataSource("user@lksnext.com"))

        viewModel.markAllAsRead()
        testScheduler.advanceUntilIdle()

        assertEquals("user@lksnext.com", repo.markedAsReadEmail)
    }
}