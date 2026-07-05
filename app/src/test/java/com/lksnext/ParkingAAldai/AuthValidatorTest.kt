package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.validation.AuthValidator
import org.junit.Assert.*
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun corporateEmail_withSpaces_isValid() {
        val result = AuthValidator.validateCorporateEmail(" user@lks.com")
        assertNull(result)
    }

    @Test
    fun nonCorporateEmail_returnsError() {
        val result = AuthValidator.validateCorporateEmail("user@gmail.com")
        assertEquals("Usa tu correo corporativo @lks.com", result)
    }

    @Test
    fun validRegister_returnsNoError() {
        val result = AuthValidator.validateRegister(
            name = "Asier",
            username = "asier",
            email = "asier@lks.com",
            password = "123456",
            confirmPassword = "123456"
        )
        assertNull(result)
    }

    @Test
    fun registerWithBlankName_returnsError() {
        val result = AuthValidator.validateRegister(
            name = "",
            username = "asier",
            email = "asier@lks.com",
            password = "123456",
            confirmPassword = "123456"
        )
        assertEquals("El nombre no puede estar vacío", result)
    }

    @Test
    fun registerWithBlankUsername_returnsError() {
        val result = AuthValidator.validateRegister(
            name = "Asier",
            username = "",
            email = "asier@lks.com",
            password = "123456",
            confirmPassword = "123456"
        )
        assertEquals("El nombre de usuario no puede estar vacío", result)
    }

    @Test
    fun registerWIthShortPassword_returnsError() {
        val result = AuthValidator.validateRegister(
            name = "Asier",
            username = "asier",
            email = "asier@lks.com",
            password = "12345",
            confirmPassword = "12345"
        )
        assertEquals("La contraseña debe tener al menos 6 caracteres", result)
    }

    @Test
    fun registerWithDifferentePasswords_returnsError() {
        val result = AuthValidator.validateRegister(
            name = "Asier",
            username = "asier",
            email = "asier@lks.com",
            password = "123456",
            confirmPassword = "abcdef"
        )
        assertEquals("Las contraseñas no coinciden", result)
    }

    @Test
    fun passwordRecoveryByPhoneWithoutPhone_returnsError() {
        val result = AuthValidator.validatePassswordRecovery(
            selectedMethod = "phone",
            email = "",
            phone = ""
        )
        assertEquals("Ingresa tu número de teléfono", result)
    }
}