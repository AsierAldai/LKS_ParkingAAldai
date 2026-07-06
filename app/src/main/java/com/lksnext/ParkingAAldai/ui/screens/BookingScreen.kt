package com.lksnext.ParkingAAldai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.lksnext.ParkingAAldai.ui.viewmodels.ProfileViewModel
import com.lksnext.ParkingAAldai.data.models.ReservationEntity
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.notifications.NotificationHelper
import com.lksnext.ParkingAAldai.ui.components.ReservationSheet
import com.lksnext.ParkingAAldai.ui.components.getSpotPrefix
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark
import com.lksnext.ParkingAAldai.ui.viewmodels.BookingViewModel
import com.lksnext.ParkingAAldai.workers.NotificationWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.random.Random

enum class SpotType {
    COMBUSTION, ELECTRIC, MOTORCYCLE, DISABLED
}

data class ParkingSpotData(
    val type: SpotType
)

// Función para normalizar la fecha a las 00:00:00 UTC para consistencia entre usuarios
fun Long.normalizeToStartOfDay(): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onNavigate: (String) -> Unit,
    viewModel: BookingViewModel,
    profileViewModel: ProfileViewModel,
) {
    val userVehicle by profileViewModel.vehicles.collectAsState(initial = emptyList())

    BookingScreenContent(
        onNavigate = onNavigate,
        userVehicles = userVehicle,
        getAllReservationsByDate = viewModel::getAllReservationsByDate,
        getFutureReservationsBySpot = viewModel::getFutureReservationsBySpot,
        onCreateReservation = viewModel::createReservation,
        errorMessage = viewModel.errorMessage.value,
        isLoading = viewModel.isLoading.value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingScreenContent(
    onNavigate: (String) -> Unit,
    userVehicles: List<VehicleEntity>,
    getAllReservationsByDate: (Long) -> Flow<List<ReservationEntity>>,
    getFutureReservationsBySpot: (Int, Long) -> Flow<List<ReservationEntity>>,
    onCreateReservation: (
            spotIndex: Int,
            spotType: SpotType,
            selectedDateMillis: Long,
            vehicle: VehicleEntity,
            start: String,
            end: String,
            onSuccess: () -> Unit
    ) -> Unit,
    errorMessage: String,
    isLoading: Boolean
) {
    val context = LocalContext.current

    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var showFilters by remember { mutableStateOf(false) }
    
    // Normalizamos la fecha actual al arrancar para que coincida con lo que otros guarden
    var selectedDateMillis by remember { 
        mutableLongStateOf(System.currentTimeMillis().normalizeToStartOfDay()) 
    }

    val todayMillis = remember { System.currentTimeMillis().normalizeToStartOfDay() }
    val maxDateMillis = remember { todayMillis + (7 * 24 * 60 * 60 * 1000L) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val selectedDateFormatted = dateFormatter.format(Date(selectedDateMillis))

    // Obtenemos TODAS las reservas del día seleccionado para el mapa
    val allReservationsToday by produceState<List<ReservationEntity>>(initialValue = emptyList(), selectedDateMillis) {
        getAllReservationsByDate(selectedDateMillis).collect { value = it}
    }

    // Estados de los filtros
    var filterCombustion by remember { mutableStateOf(false) }
    var filterElectric by remember { mutableStateOf(false) }
    var filterPmr by remember { mutableStateOf(false) }
    var filterMoto by remember { mutableStateOf(false) }
    var filterFree by remember { mutableStateOf(false) }
    var filterOccupied by remember { mutableStateOf(false) }

    var selectedSpotIndex by remember { mutableStateOf<Int?>(null) }

    // Obtenemos reservas futuras de la plaza (incluyendo otros días para mostrar información)
    val futureReservations by produceState<List<ReservationEntity>>(initialValue = emptyList(), selectedSpotIndex, selectedDateMillis) {
        val index = selectedSpotIndex
        if (index != null) {
            getFutureReservationsBySpot(index, selectedDateMillis).collect { value = it }
        } else {
            value = emptyList()
        }
    }

    val spots = remember {
        val random = Random(42) // Semilla fija para que las plazas no cambien de posición al reiniciar o cambiar cuenta
        val types = mutableListOf<SpotType>()
        repeat(3) { types.add(SpotType.DISABLED) }
        repeat(64) { types.add(SpotType.COMBUSTION) }
        repeat(18) { types.add(SpotType.ELECTRIC) }
        repeat(15) { types.add(SpotType.MOTORCYCLE) }
        
        val subList = types.subList(3, 100)
        subList.shuffle(random)

        types.map { ParkingSpotData(type = it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE5E7EB)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- CABECERA (Filtros, Entrada, Fecha) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { showFilters = !showFilters }
                                    .border(1.dp, OrangePrimary, CircleShape),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.FilterAlt,
                                        contentDescription = "Filter",
                                        tint = OrangePrimary
                                    )
                                }
                            }

                            if (showFilters) {
                                Popup(
                                    alignment = Alignment.TopStart,
                                    offset = IntOffset(0, 130),
                                    onDismissRequest = { showFilters = false }
                                ) {
                                    FilterPopover(
                                        combustion = filterCombustion,
                                        onCombustionChange = { filterCombustion = it },
                                        electric = filterElectric,
                                        onElectricChange = { filterElectric = it },
                                        pmr = filterPmr,
                                        onPmrChange = { filterPmr = it },
                                        moto = filterMoto,
                                        onMotoChange = { filterMoto = it },
                                        free = filterFree,
                                        onFreeChange = { filterFree = it },
                                        occupied = filterOccupied,
                                        onOccupiedChange = { filterOccupied = it }
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    "ENTRADA ",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            modifier = Modifier
                                .height(44.dp)
                                .clickable { showDatePicker = true },
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    selectedDateFormatted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- MAPA ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp)
                            .clipToBounds()
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures(passThrough = false) { _, pan, zoom, _ ->
                                    zoomLevel = (zoomLevel * zoom).coerceIn(1.0f, 5.0f)
                                    offset += pan
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .wrapContentSize(unbounded = true) 
                                .graphicsLayer(
                                    scaleX = zoomLevel,
                                    scaleY = zoomLevel,
                                    translationX = offset.x,
                                    translationY = offset.y
                                )
                        ) {
                            ParkingLayout(
                                spots = spots,
                                combustion = filterCombustion,
                                electric = filterElectric,
                                pmr = filterPmr,
                                moto = filterMoto,
                                free = filterFree,
                                occupied = filterOccupied,
                                reservations = allReservationsToday,
                                onSpotClick = { index -> selectedSpotIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                " SALIDA",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }

                // CONTROLES ZOOM
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 24.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .width(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { if (zoomLevel < 5.0f) zoomLevel += 0.5f }) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }
                    IconButton(onClick = {
                        if (zoomLevel > 1.0f) zoomLevel -= 0.5f
                        if (zoomLevel < 1.0f) {
                            offset = Offset.Zero
                            zoomLevel = 1.0f
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val dateValidator = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in todayMillis..maxDateMillis
                }
                override fun isSelectableYear(year: Int): Boolean {
                    return year >= Calendar.getInstance().get(Calendar.YEAR)
                }
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = dateValidator
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { 
                        selectedDateMillis = it.normalizeToStartOfDay() 
                    }
                    showDatePicker = false
                }) { Text("OK", color = OrangePrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Color.Gray) }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = TextDark,
                    headlineContentColor = TextDark,
                    selectedDayContainerColor = OrangePrimary,
                    selectedDayContentColor = Color.White,
                    todayContentColor = OrangePrimary,
                    todayDateBorderColor = OrangePrimary,
                    dayContentColor = TextDark,
                    weekdayContentColor = Color.Gray
                )
            )
        }
    }

    if (selectedSpotIndex != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedSpotIndex = null },
            containerColor = Color.White
        ) {
            ReservationSheet(
                selectedSpotIndex = selectedSpotIndex!!,
                spotType = spots[selectedSpotIndex!!].type,
                selectedDateMillis = selectedDateMillis,
                userVehicles = userVehicles,
                futureReservations = futureReservations,
                onDismiss = { selectedSpotIndex = null },
                onConfirm = { vehicle, start, end ->
                    val spotIndex = selectedSpotIndex ?: return@ReservationSheet
                    val spotType = spots[spotIndex].type
                    val prefix = getSpotPrefix(spotType)

                    onCreateReservation(
                        spotIndex,
                        spotType,
                        selectedDateMillis,
                        vehicle,
                        start,
                        end
                    ) {
                        NotificationHelper.showNotification(
                            context = context,
                            title = "Reserva confirmada",
                            body = "Tu reserva en la plaza $prefix-$spotIndex ha sido confirmada"
                        )

                        val workManager = WorkManager.getInstance(context)

                        val endParts = end.split(":")
                        val calEnd = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, endParts[0].toInt())
                            set(Calendar.MINUTE, endParts[1].toInt())
                            set(Calendar.SECOND, 0)
                        }

                        val millisUntilEnd = calEnd.timeInMillis - System.currentTimeMillis()

                        fun scheduleAlert(minsBefore: Int) {
                            val delay = millisUntilEnd - (minsBefore * 60 * 1000L)
                            if (delay > 0) {
                                val data = workDataOf(
                                    "email" to vehicle.ownerEmail,
                                    "title" to "Tu reserva en la plaza $prefix-$spotIndex termina en $minsBefore minutos"
                                )

                                val request = OneTimeWorkRequestBuilder<NotificationWorker>()
                                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                                    .setInputData(data)
                                    .build()

                                workManager.enqueue(request)
                            }
                        }
                        scheduleAlert(30)
                        scheduleAlert(15)
                        selectedSpotIndex = null
                    }
                }
            )
        }

        if (errorMessage.isNotEmpty()) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color.Red
            ) {
                Text(errorMessage, color = Color.White)
            }
        }
    }
}

@Composable
fun FilterPopover(
    combustion: Boolean, onCombustionChange: (Boolean) -> Unit,
    electric: Boolean, onElectricChange: (Boolean) -> Unit,
    pmr: Boolean, onPmrChange: (Boolean) -> Unit,
    moto: Boolean, onMotoChange: (Boolean) -> Unit,
    free: Boolean, onFreeChange: (Boolean) -> Unit,
    occupied: Boolean, onOccupiedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.width(220.dp).padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filtros", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Tipo de plaza", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
            FilterItem("Combustión", Icons.Default.DirectionsCar, combustion, onCombustionChange)
            FilterItem("Eléctrico", Icons.Default.ElectricBolt, electric, onElectricChange)
            FilterItem("PMR", Icons.AutoMirrored.Filled.Accessible, pmr, onPmrChange)
            FilterItem("Moto", Icons.AutoMirrored.Filled.DirectionsBike, moto, onMotoChange)
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Disponibilidad", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
            FilterItemSimple("Plazas libres", free, onFreeChange)
            FilterItemSimple("Plazas ocupadas", occupied, onOccupiedChange)
        }
    }
}

@Composable
fun FilterItem(label: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary),
            modifier = Modifier.scale(0.8f).size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = TextDark)
    }
}

@Composable
fun FilterItemSimple(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = OrangePrimary),
            modifier = Modifier.scale(0.8f).size(24.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = TextDark)
    }
}

@Composable
fun ParkingLayout(
    spots: List<ParkingSpotData>,
    combustion: Boolean,
    electric: Boolean,
    pmr: Boolean,
    moto: Boolean,
    free: Boolean,
    occupied: Boolean,
    reservations: List<ReservationEntity>,
    onSpotClick: (Int) -> Unit
) {
    val noFiltersActive = !combustion && !electric && !pmr && !moto && !free && !occupied
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.requiredWidth(450.dp) 
    ) {
        for (rowIndex in 0..4) {
            Row(horizontalArrangement = Arrangement.Center) {
                ParkingBlock(startIndex = rowIndex * 20, spots = spots, combustion, electric, pmr, moto, free, occupied, noFiltersActive, reservations, onSpotClick = onSpotClick)
                Spacer(modifier = Modifier.width(32.dp))
                ParkingBlock(startIndex = rowIndex * 20 + 10, spots = spots, combustion, electric, pmr, moto, free, occupied, noFiltersActive, reservations, onSpotClick = onSpotClick)
            }
            if (rowIndex < 4) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ParkingBlock(
    startIndex: Int,
    spots: List<ParkingSpotData>,
    combustion: Boolean,
    electric: Boolean,
    pmr: Boolean,
    moto: Boolean,
    free: Boolean,
    occupied: Boolean,
    allVisible: Boolean,
    reservations: List<ReservationEntity>,
    onSpotClick: (Int) -> Unit) {
    Column {
        for (subRow in 0..1) {
            Row {
                for (col in 0..4) {
                    val index = startIndex + subRow * 5 + col
                    val spotData = spots[index]
                    val isOccupied = reservations.any { it.spotIndex == index }
                    val matchesType = when(spotData.type) {
                        SpotType.COMBUSTION -> combustion
                        SpotType.ELECTRIC -> electric
                        SpotType.DISABLED -> pmr
                        SpotType.MOTORCYCLE -> moto
                    }
                    val matchesAvailability = when {
                        free && occupied -> true
                        free -> !isOccupied
                        occupied -> isOccupied
                        else -> true
                    }

                    val typeFilterActive = combustion || electric || pmr || moto
                    val availFilterActive = free || occupied

                    val finalVisibility = allVisible || (
                            (!typeFilterActive || matchesType) &&
                                    (!availFilterActive || matchesAvailability)
                            )

                    ParkingSpot(type = spotData.type, isVisible = finalVisibility, isOccupied = isOccupied, onClick = { onSpotClick(index) })
                    if (col < 4) Spacer(modifier = Modifier.width(4.dp))
                }
            }
            if (subRow == 0) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun ParkingSpot(
    type: SpotType,
    isVisible: Boolean,
    isOccupied: Boolean,
    onClick: () -> Unit) {
    val icon = when (type) {
        SpotType.COMBUSTION -> Icons.Default.DirectionsCar
        SpotType.ELECTRIC -> Icons.Default.ElectricBolt
        SpotType.MOTORCYCLE -> Icons.AutoMirrored.Filled.DirectionsBike
        SpotType.DISABLED -> Icons.AutoMirrored.Filled.Accessible
    }

    Surface(
        modifier = Modifier
            .requiredSize(width = 36.dp, height = 28.dp)
            .clickable{onClick()},
        shape = RoundedCornerShape(2.dp),
        color = when {
            !isVisible -> Color.LightGray.copy(alpha = 0.2f)
            isOccupied -> Color(0xFFFFD700) 
            else -> Color(0xFF2ECC71)
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isVisible) Color.White else Color.Transparent,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

suspend fun PointerInputScope.detectTransformGestures(
    passThrough: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation) * PI.toFloat() * centroidSize / 180f
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop || rotationMotion > touchSlop || panMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f || rotationChange != 0f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange, rotationChange)
                    }
                    // Si estamos haciendo zoom (más de 1 dedo), consumimos el evento para que los botones no se pulsen
                    if (event.changes.size > 1) {
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    BookingScreenContent(
        onNavigate =  {},
        userVehicles =  emptyList(),
        getAllReservationsByDate = { flowOf(emptyList()) },
        getFutureReservationsBySpot = { _, _ -> flowOf(emptyList()) },
        onCreateReservation = { _, _, _, _, _, _, onSuccess -> onSuccess() },
        errorMessage = "",
        isLoading = false
    )
}
