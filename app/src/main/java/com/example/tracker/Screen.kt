package com.example.tracker

sealed class Screen(val route: String)  {
    object HomeScreen : Screen("home_screen")
    object SignInScreen : Screen("sign_in_screen")
    object ProfileScreen: Screen("profile")
    object AddActivity: Screen("add-activity")
}