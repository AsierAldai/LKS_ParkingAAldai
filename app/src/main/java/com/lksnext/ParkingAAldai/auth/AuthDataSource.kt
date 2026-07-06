package com.lksnext.ParkingAAldai.auth

interface AuthDataSource {
    fun registerWithFirebase(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun loginWithFirebase(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logoutWithFirebase()
    fun getUserEmailWithFirebase(): String?
    fun updateSessionWithFirebase(newEmail: String, currentPassword: String, onResult: (Boolean, String?) -> Unit)

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String?) -> Unit)
}