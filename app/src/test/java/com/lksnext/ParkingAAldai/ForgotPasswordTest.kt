package com.lksnext.ParkingAAldai

import androidx.compose.ui.text.font.Font
import com.lksnext.ParkingAAldai.ui.viewmodels.ForgotPasswordViewModel
import org.junit.Assert.*
import org.junit.Test

class ForgotPasswordTest {

    @Test
    fun resetPasswordByPhoneWithoutPhone_setsError() {
        val viewModel = ForgotPasswordViewModel()

        viewModel.selectedMethod.value = "phone"
        viewModel.phoneValue.value = ""

        viewModel.resetPassword()

        assertEquals("Ingresa tu número de teléfono", viewModel.errorMessage.value)
        assertEquals("", viewModel.successMessage.value)
    }

    @Test
    fun resetPasswordByEmailWithValue_setsSuccessMessage() {
        val viewModel = ForgotPasswordViewModel()

        viewModel.selectedMethod.value = "email"
        viewModel.emailValue.value = "user@lks.com"

        viewModel.resetPassword()

        assertEquals("", viewModel.errorMessage.value)
        assertEquals(
            "Se ha enviado un enlace de recuperación a tu user@lks.com",
            viewModel.successMessage.value
        )
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun resetPasswordByPhoneWithValue_setsSuccessMessage() {
        val viewModel = ForgotPasswordViewModel()

        viewModel.selectedMethod.value = "phone"
        viewModel.phoneValue.value = "666777888"

        viewModel.resetPassword()

        assertEquals("", viewModel.errorMessage.value)
        assertEquals(
            "Se ha enviado un código a 666777888",
            viewModel.successMessage.value
        )
        assertFalse(viewModel.isLoading.value)
    }
}