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
import com.mysocial.ui.screen.DetailPostScreen
import com.mysocial.ui.screen.EditProfileScreen
import com.mysocial.ui.screen.FeedScreen
import com.mysocial.ui.screen.FollowsScreen
import com.mysocial.ui.screen.LoginScreen
import com.mysocial.ui.screen.PostScreen
import com.mysocial.ui.screen.ProfileScreen
import com.mysocial.ui.screen.RegisterScreen
import com.mysocial.ui.screen.UsersScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val destination = currentEntry?.destination
    val currentUser = UserSession.userId

    LaunchedEffect(destination, currentUser) {
        val isPublic = destination?.hasRoute<LoginUser>() == true || destination?.hasRoute<RegisterUser>() == true
        if (destination != null && !isPublic && currentUser == null) {
            navController.navigate(LoginUser) {
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
        composable<LoginUser> {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(RegisterUser) },
                onLoginSuccess = {
                    navController.navigate(Feed) {
                        popUpTo(LoginUser) { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterUser> {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(LoginUser) }
            )
        }

        composable<Feed> {
            FeedScreen(
                onNavigateToEditPost = { postId -> navController.navigate(EditPost(postId)) },
                onNavigateToDetail = { postId -> navController.navigate(DetailPost(postId)) }
            )
        }

        composable<RegisterPost> {
            PostScreen(
                voltar = { navController.popBackStack() }
            )
        }

        composable<EditPost> { backStackEntry ->
            val rota = backStackEntry.toRoute<EditPost>()
            PostScreen(
                postId = rota.postId,
                voltar = { navController.popBackStack() }
            )
        }

        composable<Profile> { backStackEntry ->
            val rota = backStackEntry.toRoute<Profile>()
            ProfileScreen(
                userId = rota.userId,
                onNavigateToFollows = { type -> navController.navigate(Follows(rota.userId, type)) },
                onNavigateToEditPost = { postId -> navController.navigate(EditPost(postId)) },
                onNavigateToDetail = { postId -> navController.navigate(DetailPost(postId)) },
                onNavigateToEditProfile = { navController.navigate(EditProfile) }
            )
        }

        composable<EditProfile> {
            EditProfileScreen(
                voltar = { navController.popBackStack() }
            )
        }

        composable<Follows> { backStackEntry ->
            val rota = backStackEntry.toRoute<Follows>()
            FollowsScreen(
                userId = rota.userId,
                type = rota.type,
                onNavigateToProfile = { userId -> navController.navigate(Profile(userId)) }
            )
        }

        composable<Users> {
            UsersScreen(
                onNavigateToProfile = { userId -> navController.navigate(Profile(userId)) }
            )
        }

        composable<DetailPost> { backStackEntry ->
            val rota = backStackEntry.toRoute<DetailPost>()
            DetailPostScreen(
                postId = rota.postId,
                onNavigateToProfile = { userId -> navController.navigate(Profile(userId)) },
                onNavigateToEditPost = { postId -> navController.navigate(EditPost(postId)) },
                voltar = { navController.popBackStack() }
            )
        }
    }
}
