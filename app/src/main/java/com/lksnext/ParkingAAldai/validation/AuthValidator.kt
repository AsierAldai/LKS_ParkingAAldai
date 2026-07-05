package com.lksnext.ParkingAAldai.validation

object AuthValidator {

    fun validateCorporateEmail(email: String): String? {
        val trimmedEmail = email.trim()

        if (!trimmedEmail.endsWith("@lks.com")) {
            return "Usa tu correo corporativo @lks.com"
        }
        return null
    }

    fun validateLogin(email: String): String? {
        return validateCorporateEmail(email)
    }

    fun validateRegister(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        if (name.isBlank()) return "El nombre no puede estar vacío"
        if (username.isBlank()) return "El nombre de usuario no puede estar vacío"

        validateCorporateEmail(email)?.let { return it }

        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"

        if (password != confirmPassword) return "Las contraseñas no coinciden"

        return null
    }

    fun validatePassswordRecovery(
        selectedMethod: String,
        email: String,
        phone: String
    ): String? {
        return if (selectedMethod == "email") {
            if (email.isBlank()) "Ingresa tu correo electrónico" else null
        } else {
            if (phone.isBlank()) "Ingresa tu número de teléfono" else null
        }
    }
}