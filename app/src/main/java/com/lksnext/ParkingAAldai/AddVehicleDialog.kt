package com.lksnext.ParkingAAldai

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark

@Composable
fun AddVehicleDialog(onDismiss: () -> Unit, onAddVehicle: (String, String, String, SpotType) -> Unit) {
    // Estados para los campos del formulario
    var plate by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SpotType.COMBUSTION) } // Tipo por defecto
    var brand by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(8.dp)

        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text("Añadir Vehículo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Cerrar") }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Matrícula
                FormTextField(label = "Matrícula", value = plate, onValueChange = { plate = it }, placeholder = "Ej: 1234ABC")

                Spacer(modifier = Modifier.height(16.dp))

                // Selector Tipo de Vehículo (La cuadrícula)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Tipo de vehículo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Fila 1
                    Row {
                        TypeSelectorItem(label = "Combustión", icon = Icons.Default.DirectionsCar, isSelected = selectedType == SpotType.COMBUSTION, onClick = { selectedType = SpotType.COMBUSTION }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        TypeSelectorItem(label = "Eléctrico", icon = Icons.Default.ElectricBolt, isSelected = selectedType == SpotType.ELECTRIC, onClick = { selectedType = SpotType.ELECTRIC }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Fila 2
                    Row {
                        TypeSelectorItem(label = "Moto", icon = Icons.AutoMirrored.Filled.DirectionsBike, isSelected = selectedType == SpotType.MOTORCYCLE, onClick = { selectedType = SpotType.MOTORCYCLE }, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(12.dp))
                        TypeSelectorItem(label = "PMR", icon = Icons.AutoMirrored.Filled.Accessible, isSelected = selectedType == SpotType.DISABLED, onClick = { selectedType = SpotType.DISABLED }, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campos Marca y Color
                FormTextField(label = "Marca", value = brand, onValueChange = { brand = it }, placeholder = "Ej: Toyota")
                Spacer(modifier = Modifier.height(16.dp))
                FormTextField(label = "Color", value = color, onValueChange = { color = it }, placeholder = "Ej: Rojo")

                Spacer(modifier = Modifier.height(32.dp))

                // Botón Final
                Button(
                    onClick = {
                        // Validación básica (puedes mejorarla)
                        if (plate.isNotEmpty() && brand.isNotEmpty() && color.isNotEmpty()) {
                            onAddVehicle(plate, brand, color, selectedType)
                            onDismiss() // Cierra el diálogo al añadir
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) { Text("Añadir Vehículo", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES PARA EL DIÁLOGO ---

@Composable
fun FormTextField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}

@Composable
fun TypeSelectorItem(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) OrangePrimary else Color.LightGray),
        color = if (isSelected) Color(0xFFFFF3E0) else Color.White
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) OrangePrimary else Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isSelected) OrangePrimary else Color.Gray)
        }
    }
}