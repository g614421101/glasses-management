package com.glasses.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.glasses.app.discovery.ConnectionState
import com.glasses.app.navigation.*
import com.glasses.app.theme.GlassesTheme
import com.glasses.app.ui.splash.SplashScreen
import com.glasses.app.viewmodel.AuthViewModel
import com.glasses.app.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val connectionViewModel: ConnectionViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionViewModel.connect()

        setContent {
            GlassesTheme {
                val connectionState by connectionViewModel.state.collectAsState()
                val authState by authViewModel.uiState.collectAsState()

                when (connectionState) {
                    is ConnectionState.Connected -> {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        var showMoreSheet by remember { mutableStateOf(false) }

                        Scaffold(
                            bottomBar = {
                                if (currentRoute in listOf(Routes.HOME, Routes.CUSTOMER, Routes.STATS)) {
                                    BottomNavBar(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        onMoreClick = { showMoreSheet = true }
                                    )
                                }
                            }
                        ) { paddingValues ->
                            Box(modifier = Modifier.padding(paddingValues)) {
                                AppNavGraph(
                                    navController = navController,
                                    authViewModel = authViewModel,
                                    connectionViewModel = connectionViewModel,
                                    isAuthenticated = authState.isLoggedIn
                                )
                            }
                        }

                        if (showMoreSheet) {
                            MoreSheet(
                                onDismiss = { showMoreSheet = false },
                                onNavigate = { route ->
                                    navController.navigate(route)
                                    showMoreSheet = false
                                },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    showMoreSheet = false
                                }
                            )
                        }
                    }
                    else -> {
                        SplashScreen(
                            connectionState = connectionState,
                            onConnectManual = { ip, port ->
                                connectionViewModel.connectManual(ip, port)
                            }
                        )
                    }
                }
            }
        }
    }
}
