package com.canteenconnect.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.canteenconnect.ui.admin.*
import com.canteenconnect.ui.auth.LoginScreen
import com.canteenconnect.ui.staff.StaffDashboardScreen
import com.canteenconnect.ui.student.*
import com.canteenconnect.ui.splash.SplashScreen
import com.canteenconnect.viewmodel.CanteenViewModel

@Composable
fun CanteenNavGraph(
    viewModel: CanteenViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController, 
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn() + slideInHorizontally() },
        exitTransition = { fadeOut() + slideOutHorizontally() }
    ) {

        // ─── Splash ──────────────────────────────────────────────────────────
        composable(Routes.SPLASH) {
            SplashScreen(
                viewModel = viewModel,
                onNavToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavToDashboard = { role ->
                    val dest = when (role) {
                        "staff" -> Routes.STAFF_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.STUDENT_HOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // ─── Auth ──────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    val dest = when (role) {
                        "staff" -> Routes.STAFF_DASHBOARD
                        "admin" -> Routes.ADMIN_DASHBOARD
                        else -> Routes.STUDENT_HOME
                    }
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    // TODO: Navigate to forgot password screen with OTP verification
                    // For now, we'll just show a toast or snackbar
                    // In a full implementation, this would navigate to a forgot password screen
                }
            )
        }

        // ─── Student ───────────────────────────────────────────────────────
        composable(Routes.STUDENT_HOME) {
            StudentHomeScreen(
                viewModel = viewModel,
                onNavigateToMenu = { navController.navigate(Routes.MENU) },
                onNavigateToHistory = { navController.navigate(Routes.ORDER_HISTORY) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToCart = { navController.navigate(Routes.CART) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.STUDENT_HOME) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.STUDENT_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MENU) {
            MenuScreen(
                viewModel = viewModel,
                onNavigateToCart = { navController.navigate(Routes.CART) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                viewModel = viewModel,
                onNavigateToPayment = { navController.navigate(Routes.PAYMENT) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PAYMENT) {
            PaymentScreen(
                viewModel = viewModel,
                onPaymentSuccess = {
                    navController.navigate(Routes.TOKEN) {
                        popUpTo(Routes.STUDENT_HOME)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TOKEN) {
            TokenScreen(
                viewModel = viewModel,
                onBack = {
                    navController.navigate(Routes.STUDENT_HOME) {
                        popUpTo(Routes.STUDENT_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ORDER_HISTORY) {
            OrderHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // ─── Staff ─────────────────────────────────────────────────────────
        composable(Routes.STAFF_DASHBOARD) {
            StaffDashboardScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.STAFF_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        // ─── Admin ─────────────────────────────────────────────────────────
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                viewModel = viewModel,
                onNavigateToMenu = { navController.navigate(Routes.MENU_MANAGEMENT) },
                onNavigateToReports = { navController.navigate(Routes.REPORTS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MENU_MANAGEMENT) {
            MenuManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REPORTS) {
            ReportsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.USER_MANAGEMENT) {
            UserManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
