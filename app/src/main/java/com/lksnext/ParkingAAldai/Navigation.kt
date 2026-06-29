package com.lksnext.ParkingAAldai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { AuthManager(context) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Definimos qué pantallas NO deben mostrar la barra inferior (Login, etc.)
    val showBars = currentRoute in listOf("booking", "my_bookings", "profile")

    val repo = remember { FirebaseRepository() }

// Usa esto en lugar del remember
    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(repo, authManager) as T
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
                        val unreadCount by repo.getUnreadCount(profileViewModel.email.value).collectAsState(initial = 0)

                        IconButton(
                            onClick = { navController.navigate("notifications") },
                            modifier = Modifier
                                .padding(end = 12.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = OrangePrimary,
                                            contentColor = Color.White,
                                            modifier = Modifier.offset(x = (-2).dp, y = 2.dp)
                                        ) {
                                            Text(
                                                text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Outlined.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
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
                    repository = repo
                )
            }

            composable("forgot_password") {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable("booking") {
                BookingScreen(onNavigate = { navController.navigate(it) }, repo, profileViewModel)
            }
            composable("my_bookings") {
                MyBookingsScreen(repo, profileViewModel)
            }
            composable("profile") {
                ProfileScreen(
                    onNavigate = { navController.navigate(it) },
                    viewModel = profileViewModel
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    repo,
                    profileViewModel,
                    onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = OrangePrimary,       // Color del icono cuando está seleccionado
            selectedTextColor = OrangePrimary,       // Color del texto cuando está seleccionado
            indicatorColor = OrangePrimary.copy(alpha = 0.1f), // Color del "highlight" (fondo redondeado)
            unselectedIconColor = Color.Gray,        // Color del icono no seleccionado
            unselectedTextColor = Color.Gray         // Color del texto no seleccionado
        )

        NavigationBarItem(
            selected = currentRoute == "booking",
            onClick = { onNavigate("booking") },
            icon = { Icon(Icons.Default.Add, null) },
            label = { Text("Reserva") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == "my_bookings",
            onClick = { onNavigate("my_bookings") },
            icon = { Icon(Icons.Default.CalendarMonth, null) },
            label = { Text("Mis Reservas") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = { Icon(Icons.Default.PersonOutline, null) },
            label = { Text("Perfil") },
            colors = navItemColors
        )
    }
}