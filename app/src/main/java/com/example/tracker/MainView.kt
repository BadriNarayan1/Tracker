package com.example.tracker


import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tracker.presentation.home.HomeScreen
import com.example.tracker.presentation.home.dummyActivities
import com.example.tracker.presentation.sign_in.GoogleAuthUiClient
import com.example.tracker.presentation.sign_in.SignInScreen
import com.example.tracker.presentation.sign_in.SignInViewModel
import kotlinx.coroutines.launch

import com.example.tracker.presentation.profile.ProfileScreen
import com.example.tracker.presentation.progress.ProgressScreen
import com.example.tracker.presentation.template.TemplateScreen

@Composable
fun MainView(googleAuthUiClient: GoogleAuthUiClient) {
    val controller = rememberNavController()
    val currentRoute = controller.currentBackStackEntryAsState().value?.destination?.route
    val shouldShowBottomBar = screenInBottomScreen.any { it.broute == currentRoute }

//    val floatingActionButton: @Composable () -> Unit = {
//        FloatingActionButton(onClick = {
//
//        }) {
//            Icon(Icons.Default.Add, null)
//        }
//    }
    val bottomBar: @Composable () -> Unit = {
        NavigationBar(containerColor = Color.Transparent,
            modifier = Modifier
                .navigationBarsPadding()) {
            screenInBottomScreen.forEach { item ->
                val selected = currentRoute == item.broute
                NavigationBarItem(selected = selected,
                    onClick = {
                        controller.navigate(item.broute) {
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedImage else item.unselectedImage,
                            contentDescription = item.btitle
                        )
                    },
                    label = {
                        Text(item.btitle)
                    }
                )
            }
        }
    }
    Scaffold(modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) bottomBar()
        }
//        floatingActionButton = {
//            AnimatedContent(targetState = currentRoute, label = "floatingActionButton") { route ->
//                if (route == Screen.BottomScreen.Utilites.broute) {
//                    floatingActionButton()
//                }
//            }
//        }
    ) { paddingValues ->
        Navigation(controller, googleAuthUiClient, paddingValues = paddingValues)
    }
}

//@Composable
//fun Navigation(navController: NavHostController, todoViewModel: TodoViewModel = viewModel(), paddingValues: PaddingValues) {
//    NavHost(navController, startDestination = Screen.BottomScreen.Home.route) {
//        composable(Screen.BottomScreen.Home.route) {
//            HomeView()
//        }
//        composable(Screen.BottomScreen.Utilites.route) {
//            UtilitesView(todoViewModel, paddingValues)
//        }
//        composable(Screen.BottomScreen.Progress.route) {
//            ProgressView()
//        }
//    }
//}

@Composable
fun Navigation(
    navController: NavHostController,
    googleAuthUiClient: GoogleAuthUiClient,
    paddingValues: PaddingValues
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Screen.SignInScreen.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.SignInScreen.route) {
            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                if (googleAuthUiClient.getSignedInUser() != null) {
                    navController.navigate(Screen.BottomScreen.Home.broute)
                }
            }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK) {
                        coroutineScope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            viewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(state.isSignInSuccessfull) {
                if (state.isSignInSuccessfull) {
//                    Toast.makeText(
//                        context,
//                        "Sign in successful",
//                        Toast.LENGTH_LONG
//                    ).show()
                    navController.navigate(Screen.BottomScreen.Home.broute)
                    viewModel.resetState()
                }
            }

            SignInScreen(
                state = state,
                onSignInClick = {
                    coroutineScope.launch {
                        val intentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(intentSender ?: return@launch).build()
                        )
                    }
                }
            )
        }

        composable(Screen.BottomScreen.Home.broute) {
            HomeScreen(userData = googleAuthUiClient.getSignedInUser(),
                onSignOut = {coroutineScope.launch {
                    googleAuthUiClient.signOut()
//                    Toast.makeText(
//                        context,
//                        "Signed out",
//                        Toast.LENGTH_LONG
//                    ).show()
                    navController.navigate(Screen.SignInScreen.route)
                }},
                navController = navController,
                activities = dummyActivities
            )
//            ProfileScreen(
//                userData = googleAuthUiClient.getSignedInUser(),
//                onSignOut = {
//                    coroutineScope.launch {
//                        googleAuthUiClient.signOut()
//                        Toast.makeText(
//                            context,
//                            "Signed out",
//                            Toast.LENGTH_LONG
//                        ).show()
//                        navController.popBackStack()
//                    }
//                }
//            )
        }
        composable(Screen.BottomScreen.Template.broute) {
            TemplateScreen()
        }
        composable(Screen.BottomScreen.Progress.broute) {
            ProgressScreen()
        }
    }
}



@Preview(showBackground = true)
@Composable
fun MainViewPreview() {
//    MainView()
}