package com.quotatracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Permission : Screen("permission", "Izin")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.BarChart)
    object History : Screen("history", "Riwayat", Icons.Default.History)
    object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
    object Detail : Screen("detail/{uid}", "Detail") {
        fun createRoute(uid: Int) = "detail/$uid"
    }
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.History,
    Screen.Settings
)
