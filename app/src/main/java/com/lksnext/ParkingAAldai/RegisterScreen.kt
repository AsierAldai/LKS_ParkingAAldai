package com.lksnext.ParkingAAldai

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDirections
import com.lksnext.ParkingAAldai.ui.theme.BackgroundBeige
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import com.lksnext.ParkingAAldai.ui.theme.TextDark
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onBackToLogin: () -> Unit, authManager: AuthManager, dao: AppDao) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("")}
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name Field
            RegisterTextField(
                label = "Nombre",
                value = name,
                onValueChange = { name = it },
                placeholder = "Ej: Juan Pérez"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username Field
            RegisterTextField(
                label = "Nombre de usuario",
                value = username,
                onValueChange = { username = it },
                placeholder = "Ej: juan_perez"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            RegisterTextField(
                label = "Correo electrónico",
                value = email,
                onValueChange = { email = it },
                placeholder = "correo@lks.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            RegisterTextField(
                label = "Teléfono (opcional)",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Ej: 1234567890"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            RegisterTextField(
                label = "Contraseña",
                value = password,
                onValueChange = { password = it },
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
                onValueChange = { confirmPassword = it },
                placeholder = "Repite tu contraseña",
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    val emailTrimmed = email.trim()
                    if (emailTrimmed.isEmpty() || password.isEmpty()) {
                        errorMessage = "Completa todos los campos"
                    } else if (!emailTrimmed.endsWith("@lks.com")){
                        errorMessage = "El correo debe terminar en @lks.com"
                    } else if (password != confirmPassword){
                        errorMessage = "Las contraseñas no coinciden"
                    } else{
                        val success = authManager.registerUser(emailTrimmed, password)
                        authManager.registerWithFirebase(emailTrimmed, password) { success, error ->
                            if (success) {
                                scope.launch {
                                    val newUser = UserEntity(
                                        email = emailTrimmed,
                                        name = name,
                                        username = username,
                                        phone = phone
                                    )
                                    dao.insertUser(newUser)
                                    onBackToLogin()
                                }
                            } else {
                                errorMessage = "Este correo ya está registrado"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 18.sp,
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = AppDatabase.getDatabase(context)
    RegisterScreen(onBackToLogin = {}, authManager = AuthManager(context), dao = db.appDao())
}
