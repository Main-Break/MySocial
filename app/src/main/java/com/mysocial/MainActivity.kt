package com.mysocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mysocial.navigation.AppNavigation
import com.mysocial.ui.component.TopNavBar
import com.mysocial.ui.theme.MySocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MySocialTheme {
                val navController = rememberNavController()

                val currentEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentEntry?.destination?.route
                val showTopBar = currentRoute != null &&
                        !currentRoute.contains("LoginUser") &&
                        !currentRoute.contains("RegisterUser")

                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            TopNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
