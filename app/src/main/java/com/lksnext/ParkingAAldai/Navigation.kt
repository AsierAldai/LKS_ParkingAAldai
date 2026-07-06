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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lksnext.ParkingAAldai.ui.theme.OrangePrimary
import androidx.lifecycle.ViewModelProvider
import com.lksnext.ParkingAAldai.auth.AuthManager
import com.lksnext.ParkingAAldai.data.repository.FirebaseRepository
import com.lksnext.ParkingAAldai.ui.screens.BookingScreen
import com.lksnext.ParkingAAldai.ui.screens.ForgotPasswordScreen
import com.lksnext.ParkingAAldai.ui.screens.LoginScreen
import com.lksnext.ParkingAAldai.ui.screens.MyBookingsScreen
import com.lksnext.ParkingAAldai.ui.screens.NotificationsScreen
import com.lksnext.ParkingAAldai.ui.screens.ProfileScreen
import com.lksnext.ParkingAAldai.ui.screens.RegisterScreen
import com.lksnext.ParkingAAldai.ui.viewmodels.BookingViewModel
import com.lksnext.ParkingAAldai.ui.viewmodels.ForgotPasswordViewModel
import com.lksnext.ParkingAAldai.ui.viewmodels.LoginViewModel
import com.lksnext.ParkingAAldai.ui.viewmodels.MyBookingsViewModel
import com.lksnext.ParkingAAldai.ui.viewmodels.NotificationsViewModel
import com.lksnext.ParkingAAldai.ui.viewmodels.ProfileViewModel
import com.lksnext.ParkingAAldai.ui.viewmodels.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { AuthManager(context) }


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBars = currentRoute in listOf("booking", "my_bookings", "profile")

    val repo = remember { FirebaseRepository() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars),
        containerColor = Color.White,
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
                        val currentEmail = authManager.getUserEmailWithFirebase().orEmpty()
                        val unreadCount by repo.getUnreadCount(currentEmail).collectAsState(initial = 0)
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
                                    imageVector = Icons.Outlined.Notifications,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            composable("login") {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = appViewModelFactory { LoginViewModel(authManager) }
                )

                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                    onLoginSuccess = {
                        navController.navigate("booking") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    viewModel = loginViewModel
                )
            }


            composable("register") {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = appViewModelFactory { RegisterViewModel(authManager, repo) }
                )

                RegisterScreen(
                    onBackToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.popBackStack()
                    },
                    viewModel = registerViewModel
                )
            }

            composable("forgot_password") {
                val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel(
                    factory = appViewModelFactory { ForgotPasswordViewModel(authManager) }
                )

                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = forgotPasswordViewModel
                )
            }
            composable("booking") {
                val bookingViewModel: BookingViewModel = viewModel(
                    factory = appViewModelFactory { BookingViewModel(repo, authManager) }
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = appViewModelFactory { ProfileViewModel(repo, authManager) }
                )

                BookingScreen(
                    onNavigate = { navController.navigate(it) },
                    viewModel = bookingViewModel,
                    profileViewModel = profileViewModel
                )
            }
            composable("my_bookings") {
                val myBookingViewModel: MyBookingsViewModel = viewModel(
                    factory = appViewModelFactory { MyBookingsViewModel(repo, authManager) }
                )

                MyBookingsScreen(
                    viewModel = myBookingViewModel
                )
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = appViewModelFactory { ProfileViewModel(repo, authManager) }
                )

                ProfileScreen(
                    onNavigate = { navController.navigate(it) },
                    viewModel = profileViewModel
                )
            }
            composable("notifications") {
                val notificationsViewModel: NotificationsViewModel = viewModel(
                    factory = appViewModelFactory { NotificationsViewModel(repo, authManager) }
                )

                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = notificationsViewModel
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = OrangePrimary,
            selectedTextColor = OrangePrimary,
            indicatorColor = OrangePrimary.copy(alpha = 0.1f),
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray
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

private fun <T : ViewModel> appViewModelFactory(
    create: () -> T
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return create() as T
        }
    }
}