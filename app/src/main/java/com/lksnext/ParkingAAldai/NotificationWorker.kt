package com.lksnext.ParkingAAldai

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val email = inputData.getString("email") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()

        val dao = AppDatabase.getDatabase(applicationContext).appDao()

        dao.insertNotification(
            NotificationEntity(
                userEmail = email,
                title = title,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
        return Result.success()
    }
}