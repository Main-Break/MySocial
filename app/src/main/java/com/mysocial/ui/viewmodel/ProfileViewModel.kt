package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.UserSession
import com.mysocial.data.Post
import com.mysocial.data.User
import com.mysocial.data.dal.FollowDAO
import com.mysocial.data.dal.PostDAO
import com.mysocial.data.dal.UserDAO
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Reúne tudo que a tela de Perfil precisa mostrar: dados do dono do perfil, os posts
// dele e a contagem/estado de seguidores. Mesmo padrão MVVM do AuthViewModel/PostViewModel.
private data class ProfileData(
    val user: User?,
    val posts: List<Post>,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = UserDAO(application)
    private val postDao = PostDAO(application)
    private val followDao = FollowDAO(application)

    var user by mutableStateOf<User?>(null)
        private set

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var followersCount by mutableStateOf(0)
        private set

    var followingCount by mutableStateOf(0)
        private set

    // true quando o usuário logado já segue o dono desse perfil.
    var isFollowing by mutableStateOf(false)
        private set

    var loading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    // Mesma ideia do "run" do AuthViewModel/PostViewModel: evita clique duplo e
    // transforma erro numa mensagem amigável.
    private fun run(operation: suspend () -> Unit) {
        if (loading) return
        loading = true
        message = null

        viewModelScope.launch {
            try {
                operation()
            } catch (error: CancellationException) {
                throw error
            } catch (error: IllegalArgumentException) {
                message = error.message
            } catch (error: Exception) {
                message = "Ocorreu um erro inesperado!"
            } finally {
                loading = false
            }
        }
    }

    // Carrega tudo que a tela de perfil precisa: dados do usuário, posts dele, contadores
    // de seguidores/seguindo e se quem está logado já o segue.
    fun load(profileUserId: Long) = run {
        val result = withContext(Dispatchers.IO) {
            val currentUserId = UserSession.userId
            ProfileData(
                user = userDao.findById(profileUserId),
                posts = postDao.listByUser(profileUserId),
                followersCount = followDao.countFollowers(profileUserId),
                followingCount = followDao.countFollowing(profileUserId),
                isFollowing = currentUserId != null && currentUserId != profileUserId &&
                        followDao.isFollowing(currentUserId, profileUserId)
            )
        }
        requireNotNull(result.user) { "Usuário não encontrado" }
        user = result.user
        posts = result.posts
        followersCount = result.followersCount
        followingCount = result.followingCount
        isFollowing = result.isFollowing
    }

    // Passa a seguir o dono do perfil e já atualiza o contador na tela.
    fun follow(profileUserId: Long) = run {
        val currentUserId = UserSession.requireUser()
        withContext(Dispatchers.IO) { followDao.follow(currentUserId, profileUserId) }
        isFollowing = true
        followersCount += 1
    }

    // Deixa de seguir o dono do perfil e já atualiza o contador na tela.
    fun unfollow(profileUserId: Long) = run {
        val currentUserId = UserSession.requireUser()
        withContext(Dispatchers.IO) { followDao.unfollow(currentUserId, profileUserId) }
        isFollowing = false
        followersCount = (followersCount - 1).coerceAtLeast(0)
    }

    // Exclui um post exibido no perfil (só funciona se for do usuário logado, conferido
    // lá no PostDAO.delete) e já tira ele da lista em tela.
    fun deletePost(id: Long) = run {
        val currentUserId = UserSession.requireUser()
        withContext(Dispatchers.IO) { postDao.delete(id, currentUserId) }
        posts = posts.filterNot { it.id == id }
    }
}
