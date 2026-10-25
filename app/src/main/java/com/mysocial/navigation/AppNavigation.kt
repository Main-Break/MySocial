package com.mysocial.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mysocial.ui.screen.LoginScreen
import com.mysocial.ui.screen.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = LoginUser,
        modifier = modifier
    ) {
        composable<LoginUser> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterUser) }
            )
        }

        composable<RegisterUser> {
            RegisterScreen()
        }
    }
}
