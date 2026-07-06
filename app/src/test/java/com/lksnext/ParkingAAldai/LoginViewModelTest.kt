package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.ui.viewmodels.LoginViewModel
import org.junit.Assert.*
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun loginWithNonCorporateEmail_setsError() {
        val auth = FakeAuthDataSource()
        val viewModel = LoginViewModel(auth)

        viewModel.email.value = "user@gmail.com"
        viewModel.password.value = "123456"

        viewModel.login {}

        assertEquals("Usa tu correo corporativo @lks.com", viewModel.errorMessage.value)
        assertFalse(auth.loginCalled)
    }

    @Test
    fun loginSuccess_callsOnSuccess() {
        val auth = FakeAuthDataSource()
        val viewModel = LoginViewModel(auth)
        var successCalled = false

        viewModel.email.value = "user@lks.com"
        viewModel.password.value = "123456"

        viewModel.login { successCalled = true }

        assertTrue(auth.loginCalled)
        assertTrue(successCalled)
        assertEquals("", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loginFailure_setsError() {
        val auth = FakeAuthDataSource().apply {
            loginSuccess = false
            error = "Credenciales inválidas"
        }
        val viewModel = LoginViewModel(auth)

        viewModel.email.value = "user@lks.com"
        viewModel.password.value = "wrong"

        viewModel.login {}

        assertTrue(auth.loginCalled)
        assertEquals("Credenciales inválidas", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }
}