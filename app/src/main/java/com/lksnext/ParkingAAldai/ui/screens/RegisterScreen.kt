package com.lksnext.ParkingAAldai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingAAldai.ui.theme.BackgroundBeige
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark
import com.lksnext.ParkingAAldai.ui.viewmodels.RegisterViewModel

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel
) {
    RegisterScreenContent(
        name = viewModel.name.value,
        username = viewModel.username.value,
        email = viewModel.email.value,
        phone = viewModel.phone.value,
        password = viewModel.password.value,
        confirmPassword = viewModel.confirmPassword.value,
        errorMessage = viewModel.errorMessage.value,
        onNameChange = { viewModel.name.value = it },
        onUsernameChange = { viewModel.username.value = it },
        onEmailChange = { viewModel.email.value = it },
        onPhoneChange = { viewModel.phone.value = it },
        onPasswordChange = { viewModel.password.value = it },
        onConfirmPasswordChange = { viewModel.confirmPassword.value = it },
        onRegisterClick = { viewModel.register(onRegisterSuccess) },
        onBackToLogin = onBackToLogin
    )
}

@Composable
fun RegisterScreenContent(
    name: String,
    username: String,
    email: String,
    phone: String,
    password: String,
    confirmPassword: String,
    errorMessage: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBeige
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo / Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Crear Cuenta",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field
            RegisterTextField(
                label = "Nombre",
                value = name,
                onValueChange = { onNameChange(it) },
                placeholder = "Ej: Juan Pérez"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username Field
            RegisterTextField(
                label = "Nombre de usuario",
                value = username,
                onValueChange = { onUsernameChange(it) },
                placeholder = "Ej: juan_perez"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            RegisterTextField(
                label = "Correo electrónico",
                value = email,
                onValueChange = { onEmailChange(it) },
                placeholder = "correo@lksnext.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            RegisterTextField(
                label = "Teléfono (opcional)",
                value = phone,
                onValueChange = { onPhoneChange(it) },
                placeholder = "Ej: 1234567890"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            RegisterTextField(
                label = "Contraseña",
                value = password,
                onValueChange = { onPasswordChange(it) },
                placeholder = "Mínimo 6 caracteres",
                isPassword = true,
                passwordVisible = passwordVisible,
                onVisibilityToggle = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            RegisterTextField(
                label = "Confirmar contraseña",
                value = confirmPassword,
                onValueChange = { onConfirmPasswordChange(it) },
                placeholder = "Repite tu contraseña",
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (errorMessage.isNotEmpty()){
                Text(errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back to Login Link
            TextButton(onClick = onBackToLogin) {
                Text(
                    text = "¿Ya tienes una cuenta? Inicia sesión",
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onVisibilityToggle: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
               text = label,
               fontWeight = FontWeight.SemiBold,
               color = TextDark,
               fontSize = 15.sp,
               modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword && onVisibilityToggle != null) {
                {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = onVisibilityToggle) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RegisterScreenPreview() {
    RegisterScreenContent(
        name = "",
        username = "",
        email = "",
        phone = "",
        password = "",
        confirmPassword = "",
        errorMessage = "",
        onNameChange = { },
        onUsernameChange = { },
        onEmailChange = { },
        onPhoneChange = { },
        onPasswordChange = { },
        onConfirmPasswordChange = { },
        onRegisterClick = { },
        onBackToLogin = { }
    )
}
