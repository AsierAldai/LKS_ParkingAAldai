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
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        emailValue = viewModel.emailValue.value,
        errorMessage = viewModel.errorMessage.value,
        successMessage = viewModel.successMessage.value,
        onEmailChange = { viewModel.emailValue.value = it },
        onResetPasswordClick = { viewModel.resetPassword() },
        onBack = onBack
    )
}

@Composable
private fun ForgotPasswordScreenContent(
    emailValue: String,
    errorMessage: String,
    successMessage: String,
    onEmailChange: (String) -> Unit,
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
                    fontSize = 17.sp,
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
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Introduce tu correo corporativo y te enviaremos un enlace para restablecer tu contraseña.",
                    color = TextDark.copy(alpha = 0.7f),
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input Field based on selection
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Correo Electrónico",
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value =emailValue,
                        onValueChange = onEmailChange,
                        placeholder = {
                            Text(
                                text = "ejemplo@lksnext.com",
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
                        fontSize = 19.sp,
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreenContent(
        emailValue = "",
        errorMessage = "",
        successMessage = "",
        onEmailChange = {},
        onResetPasswordClick = {},
        onBack = {}
    )
}
