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
}

// Clase de la Base de Datos (Singleton)
@Database(entities = [UserEntity::class, VehicleEntity::class], version = 1)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}