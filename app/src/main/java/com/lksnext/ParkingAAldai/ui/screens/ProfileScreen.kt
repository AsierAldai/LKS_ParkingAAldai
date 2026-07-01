package com.lksnext.ParkingAAldai.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lksnext.ParkingAAldai.ui.components.AddVehicleDialog
import com.lksnext.ParkingAAldai.ui.viewmodels.ProfileViewModel
import com.lksnext.ParkingAAldai.data.models.VehicleEntity
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark

@Composable
fun ProfileScreen(onNavigate: (String) -> Unit, viewModel: ProfileViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }

    // Estados para controlar los diálogos
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA)) // Color de fondo beige muy suave
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SECCIÓN FOTO Y NOMBRE ---
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(60.dp))
            }
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(32.dp).border(2.dp, OrangePrimary, CircleShape),
                tonalElevation = 2.dp
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar Foto", tint = OrangePrimary, modifier = Modifier.padding(6.dp))
            }
        }
        Text(text = viewModel.name.value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(top = 16.dp))
        Text(text = "@${viewModel.username.value}", fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // --- TARJETA INFORMACIÓN PERSONAL ---
        InfoCard(
            title = "Información Personal",
            actionLabel = "Editar",
            onAction = { showEditProfileDialog = true }
        ) {
            ReadOnlyInfoField(label = "Nombre", value = viewModel.name.value)
            ReadOnlyInfoField(label = "Nombre de usuario", value = viewModel.username.value)
            ReadOnlyInfoField(label = "Correo electrónico", value = viewModel.email.value)
            ReadOnlyInfoField(label = "Teléfono", value = viewModel.phone.value)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN MIS VEHÍCULOS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mis Vehículos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            FloatingActionButton(
                onClick = { showAddVehicleDialog = true },
                containerColor = OrangePrimary,
                contentColor = Color.White,
                modifier = Modifier.size(40.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Vehículo")
            }
        }

        if (vehicles.isEmpty()) {
            EmptyVehiclesState(onAddClick = { showAddVehicleDialog = true })
        } else {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                vehicles.forEach { vehicle ->
                    VehicleItem(vehicle = vehicle, onDelete = { viewModel.deleteVehicle(vehicle) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN CERRAR SESIÓN ---
        Button(
            onClick = { viewModel.logout()
                onNavigate("login") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- DIÁLOGOS (No se ven por defecto) ---
    if (showAddVehicleDialog) {
        AddVehicleDialog(
            onDismiss = { showAddVehicleDialog = false },
            onAddVehicle = { plate, brand, color, type ->
                viewModel.addVehicle(plate, brand, color, type)
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = viewModel.name.value,
            currentUsername = viewModel.username.value,
            currentEmail = viewModel.email.value,
            currentPhone = viewModel.phone.value,
            currentPassword = "",
            onDismiss = { showEditProfileDialog = false },
            onSave = { n, u, e, p, pass ->
                viewModel.updateProfile(n,u,e,p, pass)
                showEditProfileDialog = false
            }
        )
    }
}


@Composable
fun InfoCard(title: String, actionLabel: String, onAction: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                TextButton(onClick = onAction) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(actionLabel)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ReadOnlyInfoField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 16.sp, color = TextDark, modifier = Modifier.padding(top = 2.dp))
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Composable
fun EmptyVehiclesState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(60.dp))
        Text("No tienes vehículos registrados", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
        Text("Añade un vehículo para poder hacer reservas", color = Color.Gray.copy(alpha = 0.7f), fontSize = 12.sp)
        Button(
            onClick = onAddClick,
            modifier = Modifier.padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
        ) { Text("Añadir Vehículo") }
    }
}

@Composable
fun VehicleItem(vehicle: VehicleEntity, onDelete: () -> Unit) {
    // Definimos los iconos por tipo (reutilizando SpotType de BookingScreen)
    val icon = when (vehicle.type) {
        "ELECTRIC" -> Icons.Default.ElectricBolt
        "MOTORCYCLE" -> Icons.AutoMirrored.Filled.DirectionsBike
        "DISABLED" -> Icons.AutoMirrored.Filled.Accessible
        else -> Icons.Default.DirectionsCar // Combustion o genérico
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFFFFF3E0), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = OrangePrimary) }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.plate, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                Text("${vehicle.brand} - ${vehicle.color}", color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentUsername: String,
    currentEmail: String,
    currentPhone: String,
    currentPassword: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    // Estados locales para editar los campos
    var name by remember { mutableStateOf(currentName) }
    var username by remember { mutableStateOf(currentUsername) }
    var email by remember { mutableStateOf(currentEmail) }
    var phone by remember { mutableStateOf(currentPhone) }
    var pass by remember { mutableStateOf(currentPassword)}

    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ){
                    focusManager.clearFocus()
                },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ){
                        focusManager.clearFocus()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editar Perfil",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                        )
                        Row {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onSave(name, username, email, phone, pass)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF00C853
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Guardar")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF607D8B
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cancelar")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    EditField(label = "Nombre", value = name, onValueChange = { name = it })
                    EditField(
                        label = "Nombre de usuario",
                        value = username,
                        onValueChange = { username = it })
                    EditField(
                        label = "Correo electrónico",
                        value = email,
                        onValueChange = { email = it })
                    EditField(
                        label = "Teléfono",
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = "Ej: +34 612 345 678"
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    EditField(
                        label = "Contraseña actual (Requerido para guardar cambios)",
                        value = pass,
                        onValueChange = { pass = it },
                        placeholder = "Introduce tu contraseña actual"
                    )
                }
            }
        }
    }
}

@Composable
fun EditField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}