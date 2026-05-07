package com.lksnext.ParkingAAldai

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.lksnext.ParkingAAldai.ui.theme.AppTheme

class MainActivity : androidx.activity.ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
                /*
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onNavigateToRegister = { currentScreen = "register" },
                        onNavigateToForgotPassword = { currentScreen = "forgot_password" },
                        onLoginSuccess = { currentScreen = "booking" }
                    )
                    "register" -> RegisterScreen(
                        onBackToLogin = { currentScreen = "login" }
                    )
                    "forgot_password" -> ForgotPasswordScreen(
                        onBack = { currentScreen = "login" }
                    )
                    "booking" -> BookingScreen(
                        onNavigate = { currentScreen = it }
                    )
                    "my_bookings" -> MyBookingsScreen(
                        onNavigate = { currentScreen = it }
                    )
                    "profile" -> ProfileScreen(
                        onNavigate = { currentScreen = it }
                    )
                    "notifications" -> NotificationsScreen(
                        onBack = { currentScreen = "booking" }
                    )
                }
                */
            }
        }
    }
}
