package com.example.tracker

//sealed class Screen(val route: String)  {
//    object HomeScreen : Screen("home_screen")
//    object SignInScreen : Screen("sign_in_screen")
//    object ProfileScreen: Screen("profile")
//    object AddActivity: Screen("add-activity")
//}

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: String, val route: String) {
    sealed class BottomScreen(val btitle: String, val broute: String, val selectedImage: ImageVector, val unselectedImage: ImageVector): Screen(btitle, broute) {
        object Home: BottomScreen("Home", "home", Icons.Filled.Home, Icons.Outlined.Home)
        object Template: BottomScreen("Template", "template", Icons.Filled.Menu, Icons.Outlined.Menu)
        object Progress: BottomScreen("Graph", "progress", Icons.Filled.Info, Icons.Outlined.Info)
    }
    object AddActivity: Screen("add-activity", "add-activity")
    object SignInScreen: Screen("sign-in", "sign-in")
}

val screenInBottomScreen = listOf(Screen.BottomScreen.Home, Screen.BottomScreen.Template, Screen.BottomScreen.Progress)