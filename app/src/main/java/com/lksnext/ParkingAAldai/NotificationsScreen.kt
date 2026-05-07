package com.lksnext.ParkingAAldai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingAAldai.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item { SectionHeader("No leídas") }
            // Aquí irán las nuevas (fondo ligeramente distinto)
            item { NotificationItem("Espacio para futura notificación", isRead = false) }

            item { SectionHeader("Leídas - Semana pasada") }
            item { NotificationItem("Espacio para notificación antigua", isRead = true) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(16.dp),
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray
    )
}

@Composable
fun NotificationItem(text: String, isRead: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isRead) Color.Transparent else Color(0xFFFFF3E0)) // Tono diferente si no está leída
            .padding(16.dp)
    ) {
        Text(text, color = TextDark)
    }
}
