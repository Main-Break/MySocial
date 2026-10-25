package com.mysocial.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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

// Barra de navegação do topo do app, visível em todas as telas depois do login.
// Mesma ideia do Menu.kt do material do professor: um TopAppBar só, com botões de ação
// pra trocar de tela, em vez de uma barra de navegação embaixo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text("MySocial")
        },
        actions = {
            // Botão de "+" (cor primária, igual ao botão de compor do Twitter): abre a
            // tela de criar post.
            IconButton(onClick = { navController.navigate(RegisterPost) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo post",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Ícone de casinha: volta pro feed de posts.
            IconButton(onClick = { navController.navigate(Feed) }) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Feed")
            }

            // Ícone de usuário: vai pro perfil de quem está logado.
            IconButton(
                onClick = {
                    val userId = UserSession.userId ?: return@IconButton
                    navController.navigate(Profile(userId))
                }
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil")
            }

            // Ícone de sair: encerra a sessão e volta pro login.
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
