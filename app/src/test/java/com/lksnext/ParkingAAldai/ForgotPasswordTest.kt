package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.ui.viewmodels.ForgotPasswordViewModel
import org.junit.Assert.*
import org.junit.Test

class ForgotPasswordTest {

    @Test
    fun resetPasswordWithoutEmail_setsErrorAndDOesNotCallFirebase() {
        val auth = FakeAuthDataSource()
        val viewModel = ForgotPasswordViewModel(auth)

        viewModel.emailValue.value = ""

        viewModel.resetPassword()

        assertEquals("Ingresa tu correo electrónico", viewModel.errorMessage.value)
        assertEquals("", viewModel.successMessage.value)
        assertFalse(auth.passwordResetCalled)
    }

    @Test
    fun resetPasswordWithNonCorporateEmail_setsErrorAndDoesNotCallFirebase() {
        val auth = FakeAuthDataSource()
        val viewModel = ForgotPasswordViewModel(auth)

        viewModel.emailValue.value = "user@gmail.com"

        viewModel.resetPassword()

        assertEquals("Usa tu correo corporativo @lksnext.com", viewModel.errorMessage.value)
        assertEquals("", viewModel.successMessage.value)
        assertFalse(auth.passwordResetCalled)
    }

    @Test
    fun resetPasswordSuccess_callsFirebaseAndSetsSuccessMessage() {
        val auth = FakeAuthDataSource().apply {
            passwordResetSuccess = true
        }
        val viewModel = ForgotPasswordViewModel(auth)

        viewModel.emailValue.value = "user@lksnext.com"
        viewModel.resetPassword()

        assertTrue(auth.passwordResetCalled)
        assertEquals("user@lksnext.com", auth.passwordResetEmail)
        assertEquals("", viewModel.errorMessage.value)
        assertEquals(
            "Se ha enviado un enlace de recuperación a tu user@lksnext.com",
            viewModel.successMessage.value
        )
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun resetPasswordFailure_setsFirebaseError() {
        val auth = FakeAuthDataSource().apply {
            passwordResetSuccess = false
            error = "No existe ningún usuario con ese correo"
        }
        val viewModel = ForgotPasswordViewModel(auth)

        viewModel.emailValue.value = "user@lksnext.com"
        viewModel.resetPassword()

        assertTrue(auth.passwordResetCalled)
        assertEquals("", viewModel.successMessage.value)
        assertEquals("No existe ningún usuario con ese correo", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

}