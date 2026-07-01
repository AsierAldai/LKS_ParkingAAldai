package com.lksnext.ParkingAAldai.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lksnext.ParkingAAldai.auth.AuthManager
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repo: FirebaseRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private fun getCurrentEmail() = authManager.getUserEmailWithFirebase() ?: ""

    val notifications: StateFlow<List<NotificationEntity>> =
        repo.getNotificationsByUser(getCurrentEmail())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAllAsRead() {
        viewModelScope.launch {
            repo.markAllAsRead(getCurrentEmail())
        }
    }
}