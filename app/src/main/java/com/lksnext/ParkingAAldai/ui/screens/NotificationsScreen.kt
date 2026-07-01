package com.lksnext.ParkingAAldai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.lksnext.ParkingAAldai.data.models.NotificationEntity
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.viewmodels.NotificationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBack: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState(initial = emptyList())

    // Marcar como leídas al salir
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.markAllAsRead()
    }

    NotificationsScreenContent(
        notifications = notifications,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreenContent(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold, color = Color(0xFF003366)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No tienes notificaciones", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.White)
            ) {
                val now = System.currentTimeMillis()
                val oneWeekMillis = 7 * 24 * 60 * 60 * 1000L
                val oneMonthMillis = 30 * 24 * 60 * 60 * 1000L

                // Agrupamos las notificaciones por tiempo
                val unread = notifications.filter { !it.isRead }

                val readThisWeek = notifications.filter {
                    it.isRead && (now - it.timestamp) <= oneWeekMillis
                }

                val readThisMonth = notifications.filter {
                    it.isRead && (now - it.timestamp) > oneWeekMillis && (now - it.timestamp) <= oneMonthMillis
                }

                if (unread.isNotEmpty()) {
                    item { SectionHeader("NO LEÍDAS") }
                    items(unread) { NotificationItem(it, isNew = true) }
                }

                if (readThisWeek.isNotEmpty()){
                    item { SectionHeader("ESTA SEMANA") }
                    items(readThisWeek) { NotificationItem(it, isNew = false) }
                }

                if (readThisMonth.isNotEmpty()){
                    item { SectionHeader("ESTE MES") }
                    items(readThisMonth) { NotificationItem(it, isNew = false) }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationEntity, isNew: Boolean) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isNew) Color(0xFFFFF4E5) else Color.Transparent) // Fondo naranja claro si es nueva
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isNew) Modifier.border(0.5.dp, OrangePrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).padding(12.dp)
                    else Modifier
                )
        ) {
            Column {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    color = Color(0xFF003366),
                    fontWeight = if (isNew) FontWeight.Medium else FontWeight.Normal
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = sdf.format(Date(notification.timestamp)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
        if (!isNew) HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA)) // Gris muy clarito para separar secciones
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp // Un poco de espacio entre letras para estilo "subtítulo"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreenContent(
        notifications = emptyList(),
        onBack = {}
    )
}