package com.lksnext.ParkingAAldai

import android.app.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()

    private val usersRef = db.collection("users")
    private val vehiclesRef = db.collection("vehicles")
    private val reservationsRef = db.collection("reservations")
    private val notificationsRef = db.collection("notifications")


    suspend fun insertUser(user: UserEntity) {
        usersRef.document(user.email).set(user).await()
    }

    suspend fun getUser(email: String): UserEntity? {
        val snapshot = usersRef.document(email).get().await()
        return snapshot.toObject(UserEntity::class.java)
    }

    suspend fun deleteUserByEmail(email: String) {
        usersRef.document(email).delete().await()
    }

    suspend fun insertVehicle(vehicle: VehicleEntity) {
        val docRef = if (vehicle.id == 0) vehiclesRef.document() else vehiclesRef.document(vehicle.id.toString())
        val finalVehicle = vehicle.copy(id = docRef.id.hashCode())
        docRef.set(finalVehicle).await()
    }

    fun getVehiclesByUser(email: String): Flow<List<VehicleEntity>> = callbackFlow {
        val listener = vehiclesRef.whereEqualTo("ownerEmail", email)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(VehicleEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) {
        val snapshot = vehiclesRef.whereEqualTo("id", vehicle.id).get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    suspend fun updateVehiclesOwnerEmail(oldEmail: String, newEmail: String) {
        val snapshot = vehiclesRef.whereEqualTo("ownerEmail", oldEmail).get().await()
        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "ownerEmail", newEmail)
            }
        }.await()
    }

    suspend fun insertReservation(reservation: ReservationEntity) {
        val docRef = if (reservation.id == 0) reservationsRef.document() else reservationsRef.document(reservation.id.toString())
        val finalReservation = reservation.copy(id = docRef.id.hashCode())
        docRef.set(finalReservation).await()
    }

    fun getReservationsByUser(email: String, minDateMillis: Long): Flow<List<ReservationEntity>> = callbackFlow {
        val listener = reservationsRef
            .whereEqualTo("userEmail", email)
            .whereGreaterThanOrEqualTo("dateMillis", minDateMillis)
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ReservationEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    fun getReservationsBySpotAndDate(spotIndex: Int, dateMillis: Long): Flow<List<ReservationEntity>> = callbackFlow {
        val listener = reservationsRef
            .whereEqualTo("spotIndex", spotIndex)
            .whereEqualTo("dateMillis", dateMillis)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ReservationEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAllReservationsByDate(date: Long): Flow<List<ReservationEntity>> = callbackFlow {
        val listener = reservationsRef
            .whereEqualTo("dateMillis", date)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ReservationEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    fun getFutureReservationsBySpot(spotIndex: Int, minDateMillis: Long): Flow<List<ReservationEntity>> = callbackFlow {
        val listener = reservationsRef
            .whereEqualTo("spotIndex", spotIndex)
            .whereGreaterThanOrEqualTo("dateMillis", minDateMillis)
            .orderBy("dateMillis", Query.Direction.ASCENDING)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(ReservationEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    fun getOtherReservationsForSpot(spotIndex: Int, dateMillis: Long, excludeId: Int): Flow<List<ReservationEntity>> = callbackFlow {
        val listener = reservationsRef
            .whereEqualTo("spotIndex", spotIndex)
            .whereEqualTo("dateMillis", dateMillis)
            .addSnapshotListener { snapshots, _ ->
                val list = snapshots?.toObjects(ReservationEntity::class.java)?.filter { it.id != excludeId } ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun deleteReservation(reservation: ReservationEntity) {
        val snapshot = reservationsRef.whereEqualTo("id", reservation.id).get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    suspend fun updateReservation(reservation: ReservationEntity) {
        val snapshot = reservationsRef.whereEqualTo("id", reservation.id).get().await()
        for (doc in snapshot.documents) {
            doc.reference.set(reservation).await()
        }
    }

    suspend fun insertNotification(notification: NotificationEntity) {
        val docRef = notificationsRef.document()
        val finalNotification = notification.copy(id = docRef.id.hashCode())
        docRef.set(finalNotification).await()
    }

    fun getNotificationsByUser(email: String): Flow<List<NotificationEntity>> = callbackFlow {
        val listener = notificationsRef
            .whereEqualTo("userEmail", email)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(NotificationEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose{ listener.remove() }
    }

    suspend fun markAllAsRead(email: String) {
        val snapshot = notificationsRef
            .whereEqualTo("userEmail", email)
            .whereEqualTo("isRead", false)
            .get().await()

        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
        }.await()
    }

    fun getUnreadCount(email: String): Flow<Int> = callbackFlow {
        val listener = notificationsRef
            .whereEqualTo("userEmail", email)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                val size = snapshot?.size() ?: 0
                this.trySendBlocking(size)
            }
        awaitClose{ listener.remove() }
    }
}