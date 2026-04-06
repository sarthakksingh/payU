package com.sarthak.payu.view.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Balances : Screen("balances")
    object Profile : Screen("profile")
    object AddTransaction : Screen("add_transaction")
}