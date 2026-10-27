package com.mysocial.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.mysocial.auth.UserSession
import com.mysocial.ui.screen.FeedScreen
import com.mysocial.ui.screen.LoginScreen
import com.mysocial.ui.screen.PostScreen
import com.mysocial.ui.screen.ProfileScreen
import com.mysocial.ui.screen.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    // Observa pra qual tela o app está navegando agora, e se tem alguém logado.
    val currentEntry by navController.currentBackStackEntryAsState()
    val destination = currentEntry?.destination
    val currentUser = UserSession.userId

    // Roda de novo toda vez que o destino ou o usuário logado mudam. Se a rota não for
    // Login/Cadastro (ou seja, é uma rota "protegida", tipo Feed) e ninguém estiver logado,
    // manda forçado de volta pro Login. Isso evita, por exemplo, alguém cair direto no feed
    // sem ter passado pelo login.
    LaunchedEffect(destination, currentUser) {
        val isPublic = destination?.hasRoute<LoginUser>() == true || destination?.hasRoute<RegisterUser>() == true
        if (destination != null && !isPublic && currentUser == null) {
            navController.navigate(LoginUser) {
                // Limpa todo o histórico de navegação, pra não dar pra voltar com o botão
                // "voltar" do celular pra uma tela que exige login.
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = LoginUser,
        modifier = modifier
    ) {
        // Tela de login: some um não logado, pede email/senha reais e, se bater, vai pro feed.
        composable<LoginUser> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterUser) },
                onLoginSuccess = {
                    navController.navigate(Feed) {
                        // Tira a tela de Login do histórico, pra não dar pra voltar pra ela.
                        popUpTo(LoginUser) { inclusive = true }
                    }
                }
            )
        }

        // Tela de cadastro de usuário novo.
        composable<RegisterUser> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(LoginUser) }
            )
        }

        // Tela principal com os posts, lendo direto do banco de dados.
        composable<Feed> {
            FeedScreen(
                onNavigateToEditPost = { postId -> navController.navigate(EditPost(postId)) }
            )
        }

        // Tela de criar um post novo (com limite de 280 caracteres).
        composable<RegisterPost> {
            PostScreen(
                voltar = { navController.popBackStack() }
            )
        }

        // Tela de editar um post existente (reaproveita a mesma PostScreen do post novo).
        composable<EditPost> { backStackEntry ->
            val rota = backStackEntry.toRoute<EditPost>()
            PostScreen(
                postId = rota.postId,
                voltar = { navController.popBackStack() }
            )
        }

        // Tela de perfil do usuário. Ainda é só um placeholder (sem dados reais), a tela
        // de perfil em si não foi pedida ainda.
        composable<Profile> {
            ProfileScreen(name = "Android")
        }
    }
}
