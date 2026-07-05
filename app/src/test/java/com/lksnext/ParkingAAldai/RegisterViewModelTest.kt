package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.ui.viewmodels.RegisterViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun registerWithInvalidData_setsErrorAndDoesNotCalFirebase() {
        val auth = FakeAuthDataSource()
        val repo = FakeParkingRepository()
        val viewModel = RegisterViewModel(auth, repo)

        viewModel.name.value = ""
        viewModel.username.value = "user"
        viewModel.email.value = "user@lks.com"
        viewModel.password.value = "123456"
        viewModel.confirmPassword.value = "123456"

        viewModel.register {}

        assertEquals("El nombre no puede estar vacío", viewModel.errorMessage.value)
        assertFalse(auth.registerCalled)
    }

    @Test
    fun registerSuccess_insertsUserAndCallsOnSuccess() = runTest(mainDispatcherRule.testDispatcher) {
        val auth = FakeAuthDataSource()
        val repo = FakeParkingRepository()
        val viewModel = RegisterViewModel(auth, repo)
        var successCalled = false

        viewModel.name.value = "Asier"
        viewModel.username.value = "asier"
        viewModel.email.value = "asier@lks.com"
        viewModel.phone.value = "666777888"
        viewModel.password.value = "123456"
        viewModel.confirmPassword.value = "123456"

        viewModel.register { successCalled = true }
        testScheduler.advanceUntilIdle()

        assertTrue(auth.registerCalled)
        assertTrue(successCalled)
        assertFalse(viewModel.isLoading.value)
        assertEquals("Asier", repo.users["asier@lks.com"]?.name)
    }

    @Test
    fun registerFailure_setsFirebaseError() {
        val auth = FakeAuthDataSource().apply {
            registerSuccess = false
            error = "Firebase error"
        }
        val repo = FakeParkingRepository()
        val viewModel = RegisterViewModel(auth, repo)

        viewModel.name.value = "Asier"
        viewModel.username.value = "asier"
        viewModel.email.value = "asier@lks.com"
        viewModel.password.value = "123456"
        viewModel.confirmPassword.value = "123456"

        viewModel.register {}

        assertEquals("Firebase error", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }
}