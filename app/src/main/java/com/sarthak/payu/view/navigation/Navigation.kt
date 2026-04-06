package com.sarthak.payu.view.navigation



import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector



data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Balances, "Balances", Icons.Default.Info),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person),
)