package com.lksnext.ParkingAAldai

import com.lksnext.ParkingAAldai.auth.AuthDataSource
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.data.repository.ParkingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthDataSource(
    var currentEmail: String? = "user@lksnext.com"
) : AuthDataSource {

    var loginSuccess = true
    var registerSuccess = true
    var updateSuccess = true
    var error: String? = null

    var loginCalled = false
    var registerCalled = false
    var logoutCalled = false
    var updateSessionCalled = false
    var passwordResetSuccess = true
    var passwordResetCalled = false
    var passwordResetEmail: String? = null

    override fun registerWithFirebase(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        registerCalled = true
        if (registerSuccess) currentEmail = email
        onResult(registerSuccess, error)
    }

    override fun loginWithFirebase(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        loginCalled = true
        if (loginSuccess) currentEmail = email
        onResult(loginSuccess, error)
    }

    override fun logoutWithFirebase() {
        logoutCalled = true
        currentEmail = null
    }

    override fun getUserEmailWithFirebase(): String? = currentEmail

    override fun updateSessionWithFirebase(
        newEmail: String,
        currentPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        updateSessionCalled = true
        if (updateSuccess) currentEmail = newEmail
        onResult(updateSuccess, error)
    }

    override fun sendPasswordResetEmail(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        passwordResetCalled = true
        passwordResetEmail = email
        onResult(passwordResetSuccess, error)
    }
}

class FakeParkingRepository : ParkingRepository {
    var users = mutableMapOf<String, UserEntity>()
    var vehicles = mutableListOf<VehicleEntity>()
    val reservations = mutableListOf<ReservationEntity>()
    val notifications = mutableListOf<NotificationEntity>()

    var deletedReservation: ReservationEntity? = null
    var updatedReservation: ReservationEntity? = null
    var deletedVehicle: VehicleEntity? = null
    var deletedUserEmail: String? = null
    var updatedVehiclesOwnerEmail: Pair<String, String>? = null
    var markedAsReadEmail: String? = null

    override suspend fun insertUser(user: UserEntity) {
        users[user.email] = user
    }

    override suspend fun getUser(email: String): UserEntity? = users[email]

    override suspend fun deleteUserByEmail(email: String) {
        deletedUserEmail = email
        users.remove(email)
    }

    override suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicles.add(vehicle)
    }

    override fun getVehiclesByUser(email: String): Flow<List<VehicleEntity>> {
        return flowOf(vehicles.filter { it.ownerEmail == email })
    }

    override suspend fun deleteVehicle(vehicle: VehicleEntity) {
        deletedVehicle = vehicle
        vehicles.remove(vehicle)
    }

    override suspend fun updateVehiclesOwnerEmail(oldEmail: String, newEmail: String) {
        updatedVehiclesOwnerEmail = oldEmail to newEmail
    }

    override suspend fun insertReservation(reservation: ReservationEntity) {
        reservations.add(reservation)
    }

    override fun getReservationsByUser(
        email: String,
        winDateMillis: Long
    ): Flow<List<ReservationEntity>> {
        return flowOf(reservations.filter { it.userEmail == email && it.dateMillis >= winDateMillis })
    }

    override fun getAllReservationsByDate(date: Long): Flow<List<ReservationEntity>> {
        return flowOf(reservations.filter { it.dateMillis == date })
    }

    override fun getFutureReservationsBySpot(
        spotIndex: Int,
        minDateMillis: Long
    ): Flow<List<ReservationEntity>> {
        return flowOf(reservations.filter { it.spotIndex == spotIndex && it.dateMillis >= minDateMillis })
    }

    override fun getOtherReservationsForSpot(
        spotIndex: Int,
        dateMillis: Long,
        excludeId: Int
    ): Flow<List<ReservationEntity>> {
        return flowOf(
            reservations.filter {
                it.spotIndex == spotIndex &&
                        it.dateMillis == dateMillis &&
                        it.id != excludeId
            }
        )
    }

    override suspend fun deleteReservation(reservation: ReservationEntity) {
        deletedReservation = reservation
        reservations.remove(reservation)
    }

    override suspend fun updateReservation(reservation: ReservationEntity) {
        updatedReservation = reservation
    }

    override suspend fun insertNotification(notification: NotificationEntity) {
        notifications.add(notification)
    }

    override fun getNotificationsByUser(email: String): Flow<List<NotificationEntity>> {
        return flowOf(notifications.filter { it.userEmail == email })
    }

    override suspend fun markAllAsRead(email: String) {
        markedAsReadEmail = email
        notifications.replaceAll { notification ->
            if (notification.userEmail == email) notification.copy(isRead = true) else notification
        }
    }

    override fun getUnreadCount(email: String): Flow<Int> {
        return flowOf(notifications.count { it.userEmail == email && !it.isRead })
    }
}