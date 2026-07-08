package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingAAldai.auth.AuthDataSource
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.repository.ParkingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repo: ParkingRepository,
    private val authManager: AuthDataSource
) : ViewModel() {

    private fun getCurrentEmail() = authManager.getUserEmailWithFirebase() ?: ""

    val notifications: StateFlow<List<NotificationEntity>> =
        repo.getNotificationsByUser(getCurrentEmail())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAllAsRead(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repo.markAllAsRead(getCurrentEmail())
            } finally {
                onComplete()
            }
        }
    }
}