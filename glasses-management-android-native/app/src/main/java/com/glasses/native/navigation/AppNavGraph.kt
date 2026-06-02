package com.glasses.native.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.glasses.native.ui.customer.CustomerScreen
import com.glasses.native.ui.home.HomeScreen
import com.glasses.native.ui.login.LoginScreen
import com.glasses.native.ui.register.RegisterScreen
import com.glasses.native.ui.stats.StatsScreen
import com.glasses.native.viewmodel.AuthViewModel
import com.glasses.native.viewmodel.ConnectionViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CUSTOMER = "customer"
    const val STATS = "stats"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    connectionViewModel: ConnectionViewModel,
    isAuthenticated: Boolean
) {
    val startRoute = if (isAuthenticated) Routes.HOME else Routes.LOGIN

    NavHost(navController = navController, startDestination = startRoute) {
        composable(Routes.LOGIN) {
            val authState by authViewModel.uiState.collectAsState()
            LoginScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onLogin = { u, p -> authViewModel.login(u, p) },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Routes.REGISTER) {
            val authState by authViewModel.uiState.collectAsState()
            RegisterScreen(
                isLoading = authState.isLoading,
                error = authState.error,
                onRegister = { inv, u, ph, p, cp ->
                    authViewModel.register(inv, u, ph, p, cp) {
                        navController.popBackStack()
                    }
                },
                onNavigateToLogin = { navController.popBackStack() },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen()
        }

        composable(Routes.CUSTOMER) {
            CustomerScreen()
        }

        composable(Routes.STATS) {
            StatsScreen()
        }
    }
}
