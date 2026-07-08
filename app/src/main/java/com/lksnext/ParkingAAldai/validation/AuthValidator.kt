package com.lksnext.ParkingAAldai.validation

object AuthValidator {

    fun validateCorporateEmail(email: String): String? {
        val trimmedEmail = email.trim()

        if (trimmedEmail.isBlank()) {
            return "Ingresa tu correo electrónico"
        }

        if (!trimmedEmail.endsWith("@lksnext.com")) {
            return "Usa tu correo corporativo @lksnext.com"
        }
        return null
    }

    fun validateLogin(email: String, password: String): String? {
        validateCorporateEmail(email)?.let { return it }

        if (password.isBlank()) {
            return "Ingresa tu contraseña"
        }

        return null
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
}