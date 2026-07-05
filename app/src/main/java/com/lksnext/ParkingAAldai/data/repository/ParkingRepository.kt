package com.lksnext.ParkingAAldai.data.repository

import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import kotlinx.coroutines.flow.Flow

interface ParkingRepository {
    suspend fun insertUser(user: UserEntity)
    suspend fun getUser(email: String): UserEntity?
    suspend fun deleteUserByEmail(email: String)

    suspend fun insertVehicle(vehicle: VehicleEntity)
    fun getVehiclesByUser(email: String): Flow<List<VehicleEntity>>
    suspend fun deleteVehicle(vehicle: VehicleEntity)
    suspend fun updateVehiclesOwnerEmail(oldEmail: String, newEmail: String)

    suspend fun insertReservation(reservation: ReservationEntity)
    fun getReservationsByUser(email: String, winDateMillis: Long): Flow<List<ReservationEntity>>
    fun getAllReservationsByDate(date: Long): Flow<List<ReservationEntity>>
    fun getFutureReservationsBySpot(spotIndex: Int, minDateMillis: Long): Flow<List<ReservationEntity>>
    fun getOtherReservationsForSpot(spotIndex: Int, dateMillis: Long, excludeId: Int): Flow<List<ReservationEntity>>
    suspend fun deleteReservation(reservation: ReservationEntity)
    suspend fun updateReservation(reservation: ReservationEntity)

    suspend fun insertNotification(notification: NotificationEntity)
    fun getNotificationsByUser(email: String): Flow<List<NotificationEntity>>
    suspend fun markAllAsRead(email: String)
    fun getUnreadCount(email: String): Flow<Int>
}