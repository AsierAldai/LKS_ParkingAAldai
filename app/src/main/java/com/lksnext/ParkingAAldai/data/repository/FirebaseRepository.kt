package com.lksnext.ParkingAAldai.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.UserEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository : ParkingRepository {
    private val db = FirebaseFirestore.getInstance()

    private val usersRef = db.collection("users")
    private val vehiclesRef = db.collection("vehicles")
    private val reservationsRef = db.collection("reservations")
    private val notificationsRef = db.collection("notifications")

    private companion object {
        private const val TAG = "FirebaseRepository"
    }

    override suspend fun insertUser(user: UserEntity) {
        usersRef.document(user.email).set(user).await()
    }

    override suspend fun getUser(email: String): UserEntity? {
        val snapshot = usersRef.document(email).get().await()
        return snapshot.toObject(UserEntity::class.java)
    }

    override suspend fun deleteUserByEmail(email: String) {
        usersRef.document(email).delete().await()
    }

    override suspend fun insertVehicle(vehicle: VehicleEntity) {
        val docRef = if (vehicle.id == 0) vehiclesRef.document() else vehiclesRef.document(vehicle.id.toString())
        val finalVehicle = vehicle.copy(id = docRef.id.hashCode())
        docRef.set(finalVehicle).await()
    }

    override fun getVehiclesByUser(email: String): Flow<List<VehicleEntity>> = callbackFlow {
        val listener = vehiclesRef.whereEqualTo("ownerEmail", email)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(VehicleEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun deleteVehicle(vehicle: VehicleEntity) {
        val snapshot = vehiclesRef.whereEqualTo("id", vehicle.id).get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    override suspend fun updateVehiclesOwnerEmail(oldEmail: String, newEmail: String) {
        val snapshot = vehiclesRef.whereEqualTo("ownerEmail", oldEmail).get().await()
        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "ownerEmail", newEmail)
            }
        }.await()
    }

    override suspend fun insertReservation(reservation: ReservationEntity) {
        val docRef = if (reservation.id == 0) reservationsRef.document() else reservationsRef.document(reservation.id.toString())
        val finalReservation = reservation.copy(id = docRef.id.hashCode())
        docRef.set(finalReservation).await()
    }

    override fun getReservationsByUser(email: String, winDateMillis: Long): Flow<List<ReservationEntity>> =
        callbackFlow {
            val listener = reservationsRef
                .whereEqualTo("userEmail", email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error loading reservations for $email", error)
                        this.trySendBlocking(emptyList())
                        return@addSnapshotListener
                    }

                    val list = snapshot?.toObjects(ReservationEntity::class.java)
                        ?.filter { it.dateMillis >= winDateMillis }
                        ?.sortedWith(
                            compareByDescending<ReservationEntity> { it.dateMillis }
                                .thenBy { it.startTime }
                        )
                        ?: emptyList()
                    this.trySendBlocking(list)
                }
            awaitClose { listener.remove() }
        }

    override fun getAllReservationsByDate(date: Long): Flow<List<ReservationEntity>> = callbackFlow {
        val listener = reservationsRef
            .whereEqualTo("dateMillis", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading reservations for date $date", error)
                    this.trySendBlocking(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot?.toObjects(ReservationEntity::class.java) ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    override fun getFutureReservationsBySpot(spotIndex: Int, minDateMillis: Long): Flow<List<ReservationEntity>> =
        callbackFlow {
            val listener = reservationsRef
                .whereEqualTo("spotIndex", spotIndex)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error loading future reservations for spot $spotIndex", error)
                        this.trySendBlocking(emptyList())
                        return@addSnapshotListener
                    }

                    val list = snapshot?.toObjects(ReservationEntity::class.java)
                        ?.filter { it.dateMillis >= minDateMillis }
                        ?.sortedWith(compareBy<ReservationEntity> { it.dateMillis }.thenBy { it.startTime })
                        ?: emptyList()
                    this.trySendBlocking(list)
                }
            awaitClose { listener.remove() }
        }

    override fun getOtherReservationsForSpot(spotIndex: Int, dateMillis: Long, excludeId: Int): Flow<List<ReservationEntity>> =
        callbackFlow {
            val listener = reservationsRef
                .whereEqualTo("spotIndex", spotIndex)
                .whereEqualTo("dateMillis", dateMillis)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e(TAG, "Error loading other reservations for spot $spotIndex", error)
                        this.trySendBlocking(emptyList())
                        return@addSnapshotListener
                    }

                    val list = snapshots?.toObjects(ReservationEntity::class.java)
                        ?.filter { it.id != excludeId } ?: emptyList()
                    this.trySendBlocking(list)
                }
            awaitClose { listener.remove() }
        }

    override suspend fun deleteReservation(reservation: ReservationEntity) {
        val snapshot = reservationsRef.whereEqualTo("id", reservation.id).get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    override suspend fun updateReservation(reservation: ReservationEntity) {
        val snapshot = reservationsRef.whereEqualTo("id", reservation.id).get().await()
        for (doc in snapshot.documents) {
            doc.reference.set(reservation).await()
        }
    }

    override suspend fun insertNotification(notification: NotificationEntity) {
        val docRef = notificationsRef.document()
        val finalNotification = notification.copy(id = docRef.id.hashCode())
        docRef.set(finalNotification.toFirestoreMap()).await()
    }

    override fun getNotificationsByUser(email: String): Flow<List<NotificationEntity>> = callbackFlow {
        val listener = notificationsRef
            .whereEqualTo("userEmail", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading notifications for $email", error)
                    this.trySendBlocking(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot?.documents
                    ?.map { it.toNotificationEntity() }
                    ?.sortedByDescending { it.timestamp }
                    ?: emptyList()
                this.trySendBlocking(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun markAllAsRead(email: String) {
        val snapshot = notificationsRef
            .whereEqualTo("userEmail", email)
            .get().await()

        db.runBatch { batch ->
            for (doc in snapshot.documents) {
                if (!doc.isNotificationRead()) {
                    batch.update(
                        doc.reference,
                        mapOf(
                            "isRead" to true,
                            "read" to true
                        )
                    )
                }
            }
        }.await()
    }

    override fun getUnreadCount(email: String): Flow<Int> = callbackFlow {
        val listener = notificationsRef
            .whereEqualTo("userEmail", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading unread count for $email", error)
                    this.trySendBlocking(0)
                    return@addSnapshotListener
                }

                val size = snapshot?.documents
                    ?.count { !it.isNotificationRead() } ?: 0
                this.trySendBlocking(size)
            }
        awaitClose { listener.remove() }
    }

    private fun NotificationEntity.toFirestoreMap(): Map<String, Any> =
        mapOf(
            "id" to id,
            "userEmail" to userEmail,
            "title" to title,
            "timestamp" to timestamp,
            "isRead" to isRead
        )

    private fun DocumentSnapshot.toNotificationEntity(): NotificationEntity =
        NotificationEntity(
            id = getLong("id")?.toInt() ?: id.hashCode(),
            userEmail = getString("userEmail").orEmpty(),
            title = getString("title").orEmpty(),
            timestamp = getLong("timestamp") ?: 0L,
            isRead = isNotificationRead()
        )

    private fun DocumentSnapshot.isNotificationRead(): Boolean =
        getBoolean("isRead") ?: getBoolean("read") ?: false
}