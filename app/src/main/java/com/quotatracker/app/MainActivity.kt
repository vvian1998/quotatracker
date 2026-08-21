package com.quotatracker.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quotatracker.app.service.DataUsageSyncWorker
import com.quotatracker.app.ui.components.BottomNavBar
import com.quotatracker.app.ui.navigation.Screen
import com.quotatracker.app.ui.screen.dashboard.DashboardScreen
import com.quotatracker.app.ui.screen.dashboard.DashboardViewModel
import com.quotatracker.app.ui.screen.detail.AppDetailScreen
import com.quotatracker.app.ui.screen.detail.AppDetailViewModel
import com.quotatracker.app.ui.screen.history.HistoryScreen
import com.quotatracker.app.ui.screen.history.HistoryViewModel
import com.quotatracker.app.ui.screen.onboarding.PermissionScreen
import com.quotatracker.app.ui.screen.settings.SettingsScreen
import com.quotatracker.app.ui.screen.settings.SettingsViewModel
import com.quotatracker.app.ui.theme.BackgroundDark
import com.quotatracker.app.ui.theme.QuotaTrackerTheme
import com.quotatracker.app.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic background worker
        DataUsageSyncWorker.schedule(this)

        setContent {
            QuotaTrackerTheme {
                QuotaTrackerAppNav()
            }
        }
    }
}

@Composable
fun QuotaTrackerAppNav() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hasRequiredPermissions = PermissionUtils.hasUsageStatsPermission(context)
    val startDestination = if (hasRequiredPermissions) Screen.Dashboard.route else Screen.Permission.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Onboarding Screen
            composable(Screen.Permission.route) {
                PermissionScreen(
                    onAllPermissionsGranted = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Permission.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Screen
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = hiltViewModel()
                DashboardScreen(
                    viewModel = viewModel,
                    onAppClick = { uid ->
                        navController.navigate(Screen.Detail.createRoute(uid))
                    }
                )
            }

            // App Detail Screen
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("uid") { type = NavType.IntType })
            ) {
                val viewModel: AppDetailViewModel = hiltViewModel()
                AppDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // History Screen
            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel = viewModel)
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToPermissions = {
                        navController.navigate(Screen.Permission.route)
                    }
                )
            }
        }
    }
}
