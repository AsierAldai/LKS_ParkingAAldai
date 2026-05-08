package com.lksnext.ParkingAAldai

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

    val compatibleVehicles = userVehicles.filter {
        when (spotType) {
            SpotType.MOTORCYCLE -> it.type == SpotType.MOTORCYCLE.name
            SpotType.DISABLED -> it.type == SpotType.DISABLED.name
            SpotType.COMBUSTION, SpotType.ELECTRIC ->
                it.type == SpotType.COMBUSTION.name || it.type == SpotType.ELECTRIC.name
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale.getDefault()) }

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

        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Desde") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("Hasta") }, modifier = Modifier.weight(1f))
        }


        Text("Vehículo", fontWeight = FontWeight.Bold)
        if (compatibleVehicles.isEmpty()) {
            Text("⚠️ No tienes vehículos compatibles.", color = Color.Red, fontSize = 12.sp)
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
            futureReservations.forEach { res ->
                Text("• ${res.startTime} - ${res.endTime} (${res.vehiclePlate})", fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(24.dp))


        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { selectedVehicle?.let { onConfirm(it, startTime, endTime) } }, enabled = selectedVehicle != null, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                Text("Reservar", color = Color.White)
            }
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

fun getSpotPrefix(type: SpotType): String {
    return when (type) {
        SpotType.MOTORCYCLE -> "M"
        SpotType.DISABLED -> "PMR"
        else -> "C"
    }
}