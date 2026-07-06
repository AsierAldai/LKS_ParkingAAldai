package com.lksnext.ParkingAAldai.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.repository.FirebaseRepository
import com.lksnext.ParkingAAldai.notifications.NotificationHelper

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val email = inputData.getString("email") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()

        val repo = FirebaseRepository()

        try {
            repo.insertNotification(
                NotificationEntity(
                    userEmail = email,
                    title = title,
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            )

            NotificationHelper.showNotification(
                context = applicationContext,
                title = "LKS Parking",
                body = title
            )

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}