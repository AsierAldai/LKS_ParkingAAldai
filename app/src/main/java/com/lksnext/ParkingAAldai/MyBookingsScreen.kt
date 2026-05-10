package com.lksnext.ParkingAAldai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyBookingsScreen(
    dao: AppDao,
    profileViewModel: ProfileViewModel
) {
    val userEmail = profileViewModel.email.value
    val reservations by dao.getReservationsByUser(userEmail).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedReservation by remember { mutableStateOf<ReservationEntity?>(null) }
    var showDetails by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }

    val otherReservations by if (selectedReservation != null) {
        dao.getOtherReservationsForSpot(
            selectedReservation!!.spotIndex,
            selectedReservation!!.dateMillis,
            selectedReservation!!.id
        ).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<ReservationEntity>()) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = OrangePrimary)
            Spacer(Modifier.width(8.dp))
            Text(
                "Reservas Activas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF003366)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reservations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes reservas activas", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reservations) { res ->
                    BookingCard(res) {
                        selectedReservation = res
                        showDetails = true
                    }
                }
            }
        }
    }

    // Diálogo de Detalles
    if (showDetails && selectedReservation != null) {
        BookingDetailsDialog(
            reservation = selectedReservation!!,
            onDismiss = { showDetails = false },
            onDelete = {
                scope.launch {
                    dao.deleteReservation(selectedReservation!!)
                    showDetails = false
                    selectedReservation = null
                }
            },
            onEdit = {
                showDetails = false
                showEditDialog = true
            }
        )
    }

    if (showEditDialog && selectedReservation != null) {
        EditReservationDialog(
            reservation = selectedReservation!!,
            otherReservations = otherReservations,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newStart, newEnd ->
                scope.launch {
                    val updated = selectedReservation!!.copy(
                        reservationName = newName,
                        startTime = newStart,
                        endTime = newEnd
                    )
                    dao.updateReservation(updated)
                    showEditDialog = false
                    selectedReservation = null
                }
            }
        )
    }
}

@Composable
fun BookingCard(reservation: ReservationEntity, onClick: () -> Unit) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, OrangePrimary, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9F2)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = reservation.reservationName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF003366)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = sdf.format(Date(reservation.dateMillis)),
                    color = Color(0xFF6688AA),
                    fontSize = 14.sp
                )
            }

            Surface(
                color = OrangePrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    "Activa",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BookingDetailsDialog(
    reservation: ReservationEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val sdfFull = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Detalles de Reserva", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003366))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))


                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(getIconForType(SpotType.valueOf(reservation.spotType)), null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Plaza ${getSpotPrefix(SpotType.valueOf(reservation.spotType))}-${reservation.spotIndex} - ${reservation.spotType}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            color = OrangePrimary,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.TopEnd) // Arriba a la derecha
                        ) {
                            Text(
                                "Activa",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                DetailItem("FECHA", sdfFull.format(Date(reservation.dateMillis)))
                DetailItem("HORARIO", "${reservation.startTime} - ${reservation.endTime}")
                DetailItem("VEHÍCULO", reservation.vehiclePlate)

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, OrangePrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = OrangePrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Editar", color = OrangePrimary)
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text("Cancelar", color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 15.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReservationDialog(
    reservation: ReservationEntity,
    otherReservations: List<ReservationEntity>,
    onDismiss: () -> Unit,
    onSave: (newName: String, newStart: String, newEnd: String) -> Unit
) {
    var name by remember { mutableStateOf(reservation.reservationName) }
    var startTime by remember { mutableStateOf(reservation.startTime) }
    var endTime by remember { mutableStateOf(reservation.endTime) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Lógica de validación de horas
    val validationError = remember(startTime, endTime, otherReservations) {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val start = sdf.parse(startTime)!!
            val end = sdf.parse(endTime)!!
            val limitStart = sdf.parse("08:00")!!
            val limitEnd = sdf.parse("19:00")!!

            val diffHours = (end.time - start.time) / (1000.0 * 60 * 60)

            when {
                start.before(limitStart) || end.after(limitEnd) ->
                    "Horario permitido: 08:00 - 19:00"

                end.before(start) || end == start ->
                    "La hora de fin debe ser posterior a la de inicio"

                diffHours > 9.0 ->
                    "La reserva no puede superar las 9 horas"

                otherReservations.any { other ->
                    val oStart = sdf.parse(other.startTime)!!
                    val oEnd = sdf.parse(other.endTime)!!
                    (start.before(oEnd) && end.after(oStart))
                } -> "Ya existe una reserva en ese horario"

                else -> null
            }
        } catch (e: Exception) {
            "Formato de hora inválido"
        }
    }

    val canSave = validationError == null && name.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Detalles de Reserva", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(Modifier.height(16.dp))

                Text("Nombre de la reserva", fontSize = 13.sp, color = Color.Gray)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Selectores de hora similares a ReservationSheet
                TimeSelectorField("Hora de inicio", startTime) { showStartPicker = true }
                Spacer(Modifier.height(16.dp))
                TimeSelectorField("Hora de finalización", endTime) { showEndPicker = true }

                Text(
                    text = validationError ?: "Horario disponible y correcto",
                    fontSize = 12.sp,
                    color = if (validationError == null) Color.Gray else Color.Red,
                    modifier = Modifier.padding(top = 4.dp),
                    fontWeight = if (validationError != null) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = { onSave(name, startTime, endTime) },
                        enabled = canSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) { Text("Guardar", color = Color.White) }
                }
            }
        }
    }

    if (showStartPicker) {
        val state = rememberTimePickerState(initialHour = startTime.split(":")[0].toInt(), initialMinute = startTime.split(":")[1].toInt(), is24Hour = true)
        TimePickerDialog(onDismissRequest = { showStartPicker = false }, onConfirm = {
            startTime = String.format("%02d:%02d", state.hour, state.minute)
            showStartPicker = false
        }) { TimePicker(state = state) }
    }

    if (showEndPicker) {
        val state = rememberTimePickerState(initialHour = endTime.split(":")[0].toInt(), initialMinute = endTime.split(":")[1].toInt(), is24Hour = true)
        TimePickerDialog(onDismissRequest = { showEndPicker = false }, onConfirm = {
            endTime = String.format("%02d:%02d", state.hour, state.minute) // CORREGIDO: Antes guardaba en startTime
            showEndPicker = false
        }) { TimePicker(state = state) }
    }
}

@Composable
fun TimeSelectorField(label: String, time: String, onClick: () -> Unit) {
    Column {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        OutlinedCard(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(Modifier.padding(12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(time, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}