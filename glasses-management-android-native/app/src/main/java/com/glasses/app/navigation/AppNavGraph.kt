package com.glasses.app.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.glasses.app.ui.archive.ArchiveScreen
import com.glasses.app.ui.customer.CustomerScreen
import com.glasses.app.ui.datamanage.DataManageScreen
import com.glasses.app.ui.home.HomeScreen
import com.glasses.app.ui.login.LoginScreen
import com.glasses.app.ui.profile.ProfileScreen
import com.glasses.app.ui.recyclebin.RecycleBinScreen
import com.glasses.app.ui.register.RegisterScreen
import com.glasses.app.ui.stats.StatsScreen
import com.glasses.app.ui.sysuser.SysUserScreen
import com.glasses.app.viewmodel.AuthViewModel
import com.glasses.app.viewmodel.ConnectionViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CUSTOMER = "customer"
    const val STATS = "stats"
    const val ARCHIVE = "archive/{customerId}"
    const val SYS_USER = "sys-user"
    const val RECYCLE_BIN = "recycle-bin"
    const val DATA_MANAGE = "data-manage"
    const val PROFILE = "profile"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    connectionViewModel: ConnectionViewModel,
    isAuthenticated: Boolean
) {
    val startRoute = if (isAuthenticated) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startRoute,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(300)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(300)) }
    ) {
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
            HomeScreen(navController = navController)
        }

        composable(Routes.CUSTOMER) {
            CustomerScreen(
                onNavigateToArchive = { customerId ->
                    navController.navigate("archive/$customerId")
                }
            )
        }

        composable(Routes.STATS) {
            StatsScreen()
        }

        composable(
            route = Routes.ARCHIVE,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: return@composable
            ArchiveScreen(
                customerId = customerId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SYS_USER) {
            SysUserScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.RECYCLE_BIN) {
            RecycleBinScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DATA_MANAGE) {
            DataManageScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}
