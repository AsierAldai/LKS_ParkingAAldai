package com.lksnext.ParkingAAldai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { AuthManager(context) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definimos qué pantallas NO deben mostrar la barra inferior (Login, etc.)
    val showBars = currentRoute in listOf("booking", "my_bookings", "profile")

    val database = remember { AppDatabase.getDatabase(context)}
    val dao = database.appDao()

// Usa esto en lugar del remember
    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(dao, authManager) as T
            }
        }
    )
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
        topBar = {
            if (showBars) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                "booking" -> "Reserva"
                                "my_bookings" -> "Mis Reservas"
                                "profile" -> "Mi Perfil"
                                else -> ""
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = OrangePrimary // O el color que prefieras
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("booking") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                    onLoginSuccess = {
                        navController.navigate("booking") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    authManager = authManager
                )
            }
            composable("register") {
                RegisterScreen(
                    onBackToLogin = { navController.popBackStack() },
                    authManager = authManager,
                    dao = dao
                )
            }

            composable("forgot_password") {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable("booking") {
                BookingScreen(onNavigate = { navController.navigate(it) })
            }
            composable("my_bookings") {
                MyBookingsScreen(onNavigate = { navController.navigate(it) })
            }
            composable("profile") {
                ProfileScreen(
                    onNavigate = { navController.navigate(it) },
                    viewModel = profileViewModel
                )
            }
            composable("notifications") {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        NavigationBarItem(
            selected = currentRoute == "booking",
            onClick = { onNavigate("booking") },
            icon = { Icon(Icons.Default.Add, null) },
            label = { Text("Reserva") }
        )
        NavigationBarItem(
            selected = currentRoute == "my_bookings",
            onClick = { onNavigate("my_bookings") },
            icon = { Icon(Icons.Default.CalendarMonth, null) },
            label = { Text("Mis Reservas") }
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.PersonOutline, null) },
            label = { Text("Perfil") }
        )
    }
}