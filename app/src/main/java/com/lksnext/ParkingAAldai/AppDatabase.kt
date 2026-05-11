package com.lksnext.ParkingAAldai

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Tabla de Usuarios
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val username: String,
    val phone: String
)

// Tabla de Vehículos
@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerEmail: String, // Clave para vincular al usuario actual
    val plate: String,
    val brand: String,
    val color: String,
    val type: String
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val spotIndex: Int,
    val spotType: String,
    val dateMillis: Long,
    val startTime: String,
    val endTime: String,
    val vehiclePlate: String,
    val reservationName: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

// Interfaz de acceso a datos (DAO)
@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUser(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Query("SELECT * FROM vehicles WHERE ownerEmail = :email")
    fun getVehiclesByUser(email: String): Flow<List<VehicleEntity>>

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("UPDATE vehicles SET ownerEmail = :newEmail WHERE ownerEmail = :oldEmail")
    suspend fun updateVehiclesOwnerEmail(oldEmail: String, newEmail: String)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)

    @Query("SELECT * FROM reservations WHERE userEmail = :email AND dateMillis >= :minDateMillis ORDER BY dateMillis DESC")
    fun getReservationsByUser(email: String, minDateMillis: Long): kotlinx.coroutines.flow.Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE spotIndex = :spotIndex AND dateMillis = :dateMillis")
    fun getReservationsBySpotAndDate(spotIndex: Int, dateMillis: Long): kotlinx.coroutines.flow.Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("SELECT * FROM reservations WHERE dateMillis = :date")
    fun getAllReservationsByDate(date: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE spotIndex = :spotIndex AND dateMillis >= :minDateMillis ORDER BY dateMillis ASC, startTime ASC")
    fun getFutureReservationsBySpot(spotIndex: Int, minDateMillis: Long): Flow<List<ReservationEntity>>

    @Delete
    suspend fun deleteReservation(reservation: ReservationEntity)

    @Update
    suspend fun updateReservation(reservation: ReservationEntity)

    @Query("""SELECT * FROM reservations WHERE spotIndex = :spotIndex AND dateMillis = :dateMillis AND id != :excludeId""")
    fun getOtherReservationsForSpot(spotIndex: Int, dateMillis: Long, excludeId: Int): Flow<List<ReservationEntity>>

    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getNotificationsByUser(email: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email AND isRead = 0")
    suspend fun markAllAsRead(email: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE userEmail = :email AND isRead = 0")
    fun getUnreadCount(email: String): Flow<Int>
}

// Clase de la Base de Datos (Singleton)
@Database(
    entities = [UserEntity::class, VehicleEntity::class, ReservationEntity::class, NotificationEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parking_database"
                ).fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}