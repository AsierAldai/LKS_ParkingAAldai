package com.lksnext.ParkingAAldai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingAAldai.ui.theme.BackgroundBeige
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark
import com.lksnext.ParkingAAldai.ui.viewmodels.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel
) {
    ForgotPasswordScreenContent(
        selectedMethod = viewModel.selectedMethod.value,
        emailValue = viewModel.emailValue.value,
        phoneValue = viewModel.phoneValue.value,
        errorMessage = viewModel.errorMessage.value,
        successMessage = viewModel.successMessage.value,
        onMethodSelected = { viewModel.selectedMethod.value = it },
        onEmailChange = { viewModel.emailValue.value = it },
        onPhoneChange = { viewModel.phoneValue.value = it },
        onResetPasswordClick = { viewModel.resetPassword() },
        onBack = onBack
    )
}

@Composable
private fun ForgotPasswordScreenContent(
    selectedMethod: String,
    emailValue: String,
    phoneValue: String,
    errorMessage: String,
    successMessage: String,
    onMethodSelected: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onResetPasswordClick: () -> Unit,
    onBack: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBeige
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Back Button
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clickable { onBack() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Volver",
                    color = TextDark,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Key Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(OrangePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "Recuperación de Contraseña",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Selecciona tu método de recuperación:",
                    color = TextDark.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Method Card
                RecoveryMethodCard(
                    title = "Correo Electrónico",
                    subtitle = "Recibe un enlace de recuperación",
                    icon = Icons.Default.Email,
                    isSelected = selectedMethod == "email",
                    onClick = { onMethodSelected("email") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Method Card
                RecoveryMethodCard(
                    title = "Teléfono",
                    subtitle = "Recibe un código SMS",
                    icon = Icons.Default.Phone,
                    isSelected = selectedMethod == "phone",
                    onClick = { onMethodSelected("phone") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedMethod == "phone") {
                    // Warning Message
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9E6)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFE082), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Esta opción solo está disponible si has registrado tu cuenta con un número de teléfono o lo has añadido posteriormente.",
                                color = Color(0xFF5D4037),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Input Field based on selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (selectedMethod == "email") "Correo Electrónico" else "Teléfono",
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = if (selectedMethod == "email") emailValue else phoneValue,
                        onValueChange = {
                            if (selectedMethod == "email") onEmailChange(it) else onPhoneChange(
                                it
                            )
                        },
                        placeholder = {
                            Text(
                                text = if (selectedMethod == "email") "ejemplo@correo.com" else "+34 600 000 000",
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Send Button
                Button(
                    onClick = { onResetPasswordClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text(
                        text = "Enviar Enlace de Recuperación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }


                if (errorMessage.isNotEmpty()) {
                    Text(
                        errorMessage,
                        color = Color.Red,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Red, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    )
                }

                if (successMessage.isNotEmpty()) {
                    Text(
                        successMessage,
                        color = Color.Green,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Green, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecoveryMethodCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) OrangePrimary else Color.LightGray
    val iconContainerColor = if (isSelected) Color(0xFFFFF3E0) else Color(0xFFF5F5F5)
    val iconTintColor = if (isSelected) OrangePrimary else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconContainerColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 18.sp
                )
                Text(
                    text = subtitle,
                    color = TextDark.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreenContent(
        selectedMethod = "email",
        emailValue = "",
        phoneValue = "",
        errorMessage = "",
        successMessage = "",
        onMethodSelected = {},
        onEmailChange = {},
        onPhoneChange = {},
        onResetPasswordClick = {},
        onBack = {}
    )
}
