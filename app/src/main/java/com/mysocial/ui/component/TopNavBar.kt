package com.mysocial.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.mysocial.auth.UserSession
import com.mysocial.navigation.Feed
import com.mysocial.navigation.LoginUser
import com.mysocial.navigation.Profile
import com.mysocial.navigation.RegisterPost
import com.mysocial.navigation.Users
import com.mysocial.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text("MySocial", style = MaterialTheme.typography.titleMedium)
        },
        actions = {
            IconButton(onClick = { navController.navigate(RegisterPost) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo post",
                    tint = PrimaryColor
                )
            }

            IconButton(onClick = { navController.navigate(Feed) }) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Feed")
            }

            IconButton(onClick = { navController.navigate(Users) }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Pesquisar usuários")
            }

            IconButton(
                onClick = {
                    val userId = UserSession.userId ?: return@IconButton
                    navController.navigate(Profile(userId))
                }
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil")
            }

            IconButton(
                onClick = {
                    UserSession.clear()
                    navController.navigate(LoginUser) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair")
            }
        }
    )
}
