package com.lksnext.ParkingAAldai.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.screens.SpotType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationSheet(
    selectedSpotIndex: Int,
    spotType: SpotType,
    selectedDateMillis: Long,
    userVehicles: List<VehicleEntity>,
    futureReservations: List<ReservationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (vehicle: VehicleEntity, start: String, end: String) -> Unit,
) {
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("17:00") }
    var selectedVehicle by remember { mutableStateOf<VehicleEntity?>(null) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val isTimeValid = remember(startTime, endTime) {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = sdf.parse(startTime)!!
            val end = sdf.parse(endTime)!!
            val limitStart = sdf.parse("08:00")!!
            val limitEnd = sdf.parse("19:00")!!

            val diffHours = (end.time - start.time) / (1000.0 * 60 * 60)

            val withinRange = !start.before(limitStart) && !end.after(limitEnd)
            val durationOk = diffHours in 0.1..9.0

            withinRange && durationOk
        } catch (e: Exception) { false }
    }

    val compatibleVehicles = userVehicles.filter {
        when (spotType) {
            SpotType.MOTORCYCLE -> it.type == SpotType.MOTORCYCLE.name
            SpotType.DISABLED -> it.type == SpotType.DISABLED.name
            SpotType.COMBUSTION, SpotType.ELECTRIC ->
                it.type == SpotType.COMBUSTION.name || it.type == SpotType.ELECTRIC.name
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault()) }

    val myTimePickerColors = TimePickerDefaults.colors(
        clockDialColor = Color(0xFFF0F0F0),
        clockDialSelectedContentColor = Color.White,
        clockDialUnselectedContentColor = Color.Black,
        selectorColor = OrangePrimary,
        periodSelectorSelectedContainerColor = OrangePrimary,
        containerColor = Color.White,
        timeSelectorSelectedContainerColor = OrangePrimary.copy(alpha = 0.2f),
        timeSelectorSelectedContentColor = OrangePrimary,
        timeSelectorUnselectedContainerColor = Color(0xFFF0F0F0),
        timeSelectorUnselectedContentColor = Color.Black
    )

    val isSpotOccupiedByTime = remember(startTime, endTime, futureReservations) {
        val newStart = timeToMinutes(startTime)
        val newEnd = timeToMinutes(endTime)

        // Solo comprobamos las reservas que coinciden exactamente con selectedDateMillis
        futureReservations.filter { it.dateMillis == selectedDateMillis }.any { res ->
            val existingStart = timeToMinutes(res.startTime)
            val existingEnd = timeToMinutes(res.endTime)

            // Lógica de solapamiento: (StartA < EndB) y (EndA > StartB)
            newStart < existingEnd && newEnd > existingStart
        }
    }

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp).verticalScroll(rememberScrollState())) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Nueva Reserva", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
        }


        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)), modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(color = OrangePrimary, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(40.dp)) {
                    Icon(getIconForType(spotType), null, tint = Color.White, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("${getSpotPrefix(spotType)}-$selectedSpotIndex - ${spotType.name}", fontWeight = FontWeight.Bold)
            }
        }


        Text("Fecha", fontWeight = FontWeight.Bold)
        OutlinedTextField(value = dateFormatter.format(Date(selectedDateMillis)), onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            OutlinedCard(
                onClick = { showStartPicker = true },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Desde", fontSize = 12.sp, color = Color.Gray)
                    Text(startTime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedCard(
                onClick = { showEndPicker = true },
                modifier = Modifier.weight(1f),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Hasta", fontSize = 12.sp, color = Color.Gray)
                    Text(endTime, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (!isTimeValid) {
            Text(
                "⚠️ Horario de 08:00 a 19:00 (Máximo 9 horas)",
                color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)
            )
        }

        Text("Vehículo", fontWeight = FontWeight.Bold)
        if (compatibleVehicles.isEmpty()) {
            Text("⚠️ No tienes vehículos compatibles. Puedes registrar un vehículo en tu perfil.", color = Color.Red, fontSize = 12.sp)
        } else {
            compatibleVehicles.forEach { vehicle ->
                Row(Modifier.fillMaxWidth().clickable { selectedVehicle = vehicle }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (selectedVehicle == vehicle), onClick = { selectedVehicle = vehicle })
                    Text("${vehicle.brand} (${vehicle.plate})")
                }
            }
        }


        HorizontalDivider(Modifier.padding(vertical = 16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Reservas futuras", fontWeight = FontWeight.Bold)
        }

        if (futureReservations.isEmpty()) {
            Text("No hay reservas futuras para esta plaza", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        } else {
            val (reservationsToday, reservationsOtherDays) = remember(futureReservations, selectedDateMillis) {
                futureReservations.partition { it.dateMillis == selectedDateMillis }
            }

            // --- SECCIÓN HOY ---
            if (reservationsToday.isNotEmpty()) {
                Text("Hoy", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = OrangePrimary)
                reservationsToday.forEach { res ->
                    Text("• ${res.startTime} - ${res.endTime} (${res.vehiclePlate})", fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
            }

            // --- SECCIÓN OTROS DÍAS ---
            if (reservationsOtherDays.isNotEmpty()) {
                Text("Próximos días", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Gray)
                val shortDateFmt = SimpleDateFormat("dd/MM", LocalLocale.current.platformLocale)
                reservationsOtherDays.forEach { res ->
                    val dateLabel = shortDateFmt.format(Date(res.dateMillis))
                    Text("• $dateLabel: ${res.startTime} - ${res.endTime}", fontSize = 14.sp)
                }
            }

            if (futureReservations.isEmpty()) {
                Text("No hay reservas futuras para esta plaza", color = Color.Gray, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(24.dp))


        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { selectedVehicle?.let { onConfirm(it, startTime, endTime) } },
                enabled = selectedVehicle != null && isTimeValid && !isSpotOccupiedByTime,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("Reservar", color = Color.White)
            }
        }
        if (isSpotOccupiedByTime) {
            Text("⚠️ Esta plaza ya está ocupada en ese horario.", color = Color.Red, fontSize = 12.sp)
        }
        if (showStartPicker) {
            val state = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
            TimePickerDialog(
                onDismissRequest = { showStartPicker = false },
                onConfirm = {
                    startTime = String.format("%02d:%02d", state.hour, state.minute)
                    showStartPicker = false
                }
            ) { TimePicker(state = state,
                colors = myTimePickerColors)}
        }
        if (showEndPicker) {
            val state = rememberTimePickerState(initialHour = 17, initialMinute = 0, is24Hour = true)
            TimePickerDialog(
                onDismissRequest = { showEndPicker = false },
                onConfirm = {
                    endTime = String.format("%02d:%02d", state.hour, state.minute)
                    showEndPicker = false
                }
            ) { TimePicker(state = state,
                colors = myTimePickerColors)}
        }
    }
}

// Funciones auxiliares para iconos y nombres de plaza
fun getIconForType(type: SpotType): ImageVector {
    return when (type) {
        SpotType.COMBUSTION -> Icons.Default.DirectionsCar
        SpotType.ELECTRIC -> Icons.Default.ElectricBolt
        SpotType.MOTORCYCLE -> Icons.AutoMirrored.Filled.DirectionsBike
        SpotType.DISABLED -> Icons.AutoMirrored.Filled.Accessible
    }
}

fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

fun getSpotPrefix(type: SpotType): String {
    return when (type) {
        SpotType.MOTORCYCLE -> "M"
        SpotType.DISABLED -> "PMR"
        else -> "C"
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    // Forzamos el esquema de colores claro solo para el contenido de este diálogo
    MaterialTheme(colorScheme = lightColorScheme()) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("OK", color = OrangePrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            text = {
                // El contenido (TimePicker) heredará el lightColorScheme definido arriba
                content()
            },
            containerColor = Color.White // Fondo del diálogo siempre blanco
        )
    }
}