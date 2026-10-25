package com.mysocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.mysocial.navigation.AppNavigation
import com.mysocial.ui.theme.MySocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MySocialTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
