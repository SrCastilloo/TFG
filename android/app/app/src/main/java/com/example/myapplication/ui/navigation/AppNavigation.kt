package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screen.CameraScreen
import com.example.myapplication.ui.screen.DashboardScreen
import com.example.myapplication.ui.screen.HandScreen
import com.example.myapplication.ui.screen.StatusScreen
import androidx.compose.ui.unit.dp
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem(
            route = AppRoutes.DASHBOARD,
            label = "Inicio",
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Inicio") }
        ),
        BottomNavItem(
            route = AppRoutes.HAND,
            label = "Mano",
            icon = { Icon(Icons.Default.PanTool, contentDescription = "Mano") }
        ),
        BottomNavItem(
            route = AppRoutes.CAMERA,
            label = "Cámara",
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Cámara") }
        ),
        BottomNavItem(
            route = AppRoutes.STATUS,
            label = "Estado",
            icon = { Icon(Icons.Default.Info, contentDescription = "Estado") }
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0B1020),
                        Color(0xFF111827),
                        Color(0xFF1F2937)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomBar(
                    items = items,
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppRoutes.DASHBOARD,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppRoutes.DASHBOARD) {
                    DashboardScreen(
                        onGoToStatus = { navController.navigate(AppRoutes.STATUS) },
                        onGoToHand = { navController.navigate(AppRoutes.HAND) },
                        onGoToCamera = { navController.navigate(AppRoutes.CAMERA) }
                    )
                }

                composable(AppRoutes.STATUS) {
                    StatusScreen(
                        onBack = { navController.navigate(AppRoutes.DASHBOARD) }
                    )
                }

                composable(AppRoutes.HAND) {
                    HandScreen(
                        onBack = { navController.navigate(AppRoutes.DASHBOARD) }
                    )
                }

                composable(AppRoutes.CAMERA) {
                    CameraScreen(
                        onBack = { navController.navigate(AppRoutes.DASHBOARD) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    items: List<BottomNavItem>,
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xEE0F172A),
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = item.icon,
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = Color(0xFF312E81),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8)
                )
            )
        }
    }
}