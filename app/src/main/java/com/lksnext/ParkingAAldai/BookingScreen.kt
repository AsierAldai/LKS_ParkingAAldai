package com.lksnext.ParkingAAldai

import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark
import java.text.SimpleDateFormat
import java.util.*

enum class SpotType {
    COMBUSTION, ELECTRIC, MOTORCYCLE, DISABLED
}

data class ParkingSpotData(
    val type: SpotType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(onNavigate: (String) -> Unit) {

    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    var showFilters by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var showDatePicker by remember { mutableStateOf(false) }


    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val selectedDateFormatted = dateFormatter.format(Date(selectedDateMillis))

    // Estados de los filtros: ahora empiezan en FALSE (desactivados)
    var filterCombustion by remember { mutableStateOf(false) }
    var filterElectric by remember { mutableStateOf(false) }
    var filterPmr by remember { mutableStateOf(false) }
    var filterMoto by remember { mutableStateOf(false) }
    var filterFree by remember { mutableStateOf(false) }
    var filterOccupied by remember { mutableStateOf(false) }

    val spots = remember {
        val types = mutableListOf<SpotType>()
        // 1. Añadimos primero las 3 de PMR para que estén juntas y al principio (cerca de salida/entrada)
        repeat(3) { types.add(SpotType.DISABLED) }
        // 2. El resto según proporciones exactas
        repeat(64) { types.add(SpotType.COMBUSTION) }
        repeat(18) { types.add(SpotType.ELECTRIC) }
        repeat(15) { types.add(SpotType.MOTORCYCLE) }
        // Mezclamos solo a partir del índice 3 para que las PMR no se muevan de su sitio
        val subList = types.subList(3, 100)
        subList.shuffle()

        types.map { ParkingSpotData(type = it) }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)) // Tu color de fondo
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
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
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

                        // Indicador ENTRADA
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

                        // Selector FECHA
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
                            .height(400.dp)
                            .clipToBounds()
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 3.0f)
                                    offset += pan
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
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
                                moto = filterMoto
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SALIDA
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
                    IconButton(onClick = { if (zoomLevel < 3.0f) zoomLevel += 0.2f }) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }
                    Text(
                        "${(zoomLevel * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        if (zoomLevel > 0.5f) zoomLevel -= 0.2f
                        if (zoomLevel < 1.0f && zoomLevel > 0.9f) offset = Offset.Zero
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK", color = OrangePrimary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = Color.Gray) }
            }
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
            Text("Si no marcas nada, se ve todo", fontSize = 11.sp, color = Color.Gray)
            
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
fun ParkingLayout(spots: List<ParkingSpotData>, combustion: Boolean, electric: Boolean, pmr: Boolean, moto: Boolean) {
    val noFiltersActive = !combustion && !electric && !pmr && !moto
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (rowIndex in 0..4) {
            Row(horizontalArrangement = Arrangement.Center) {
                ParkingBlock(startIndex = rowIndex * 20, spots = spots, combustion, electric, pmr, moto, noFiltersActive)
                Spacer(modifier = Modifier.width(20.dp))
                ParkingBlock(startIndex = rowIndex * 20 + 10, spots = spots, combustion, electric, pmr, moto, noFiltersActive)
            }
            if (rowIndex < 4) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ParkingBlock(startIndex: Int, spots: List<ParkingSpotData>, combustion: Boolean, electric: Boolean, pmr: Boolean, moto: Boolean, allVisible: Boolean) {
    Column {
        for (subRow in 0..1) {
            Row {
                for (col in 0..4) {
                    val index = startIndex + subRow * 5 + col
                    val spotData = spots[index]
                    val isVisible = allVisible || when(spotData.type) {
                        SpotType.COMBUSTION -> combustion
                        SpotType.ELECTRIC -> electric
                        SpotType.DISABLED -> pmr
                        SpotType.MOTORCYCLE -> moto
                    }
                    ParkingSpot(type = spotData.type, isVisible = isVisible)
                    if (col < 4) Spacer(modifier = Modifier.width(2.dp))
                }
            }
            if (subRow == 0) Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
fun ParkingSpot(type: SpotType, isVisible: Boolean) {
    val icon = when (type) {
        SpotType.COMBUSTION -> Icons.Default.DirectionsCar
        SpotType.ELECTRIC -> Icons.Default.ElectricBolt
        SpotType.MOTORCYCLE -> Icons.AutoMirrored.Filled.DirectionsBike
        SpotType.DISABLED -> Icons.AutoMirrored.Filled.Accessible
    }

    Surface(
        modifier = Modifier.size(width = 32.dp, height = 24.dp),
        shape = RoundedCornerShape(2.dp),
        color = if (isVisible) Color(0xFF2ECC71) else Color.LightGray.copy(alpha = 0.2f)
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

@Preview(showBackground = true)
@Composable
fun BookingScreenPreview() {
    BookingScreen(onNavigate = {})
}
