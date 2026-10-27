package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.UserSession
import com.mysocial.data.Post
import com.mysocial.data.PostStats
import com.mysocial.data.User
import com.mysocial.data.dal.CommentDAO
import com.mysocial.data.dal.FollowDAO
import com.mysocial.data.dal.LikeDAO
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
    val isFollowing: Boolean,
    val stats: Map<Long, PostStats>
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = UserDAO(application)
    private val postDao = PostDAO(application)
    private val followDao = FollowDAO(application)
    private val likeDao = LikeDAO(application)
    private val commentDao = CommentDAO(application)

    var user by mutableStateOf<User?>(null)
        private set

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    // Curtidas/comentários de cada post do perfil (chave = id do post).
    var stats by mutableStateOf<Map<Long, PostStats>>(emptyMap())
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

    // Vira true quando a edição do perfil é salva com sucesso. A tela de edição
    // observa isso pra voltar pro perfil, e depois chama consumeSaved() pra resetar.
    var saved by mutableStateOf(false)
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
            val loadedPosts = postDao.listByUser(profileUserId)
            val loadedStats = loadedPosts.associate { post ->
                post.id to PostStats(
                    likes = likeDao.countLikes(post.id),
                    comments = commentDao.countByPost(post.id),
                    likedByMe = currentUserId != null && likeDao.isLiked(currentUserId, post.id)
                )
            }
            ProfileData(
                user = userDao.findById(profileUserId),
                posts = loadedPosts,
                followersCount = followDao.countFollowers(profileUserId),
                followingCount = followDao.countFollowing(profileUserId),
                isFollowing = currentUserId != null && currentUserId != profileUserId &&
                        followDao.isFollowing(currentUserId, profileUserId),
                stats = loadedStats
            )
        }
        requireNotNull(result.user) { "Usuário não encontrado" }
        user = result.user
        posts = result.posts
        followersCount = result.followersCount
        followingCount = result.followingCount
        isFollowing = result.isFollowing
        stats = result.stats
    }

    // Curte ou descurte um post do perfil, mesma lógica do PostViewModel.toggleLike.
    fun toggleLike(postId: Long) = run {
        val userId = UserSession.requireUser()
        val current = stats[postId] ?: PostStats()
        withContext(Dispatchers.IO) {
            if (current.likedByMe) likeDao.unlike(userId, postId) else likeDao.like(userId, postId)
        }
        val updated = current.copy(
            likes = if (current.likedByMe) (current.likes - 1).coerceAtLeast(0) else current.likes + 1,
            likedByMe = !current.likedByMe
        )
        stats = stats + (postId to updated)
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

    // Salva a edição da bio/endereço do próprio usuário logado e já atualiza o "user"
    // em tela, sem precisar recarregar o perfil inteiro do banco.
    fun updateProfile(bio: String, cep: String, rua: String, numero: String, bairro: String, cidade: String, estado: String) = run {
        val currentUserId = UserSession.requireUser()
        withContext(Dispatchers.IO) {
            userDao.updateProfile(currentUserId, bio, cep, rua, numero, bairro, cidade, estado)
        }
        user = user?.copy(
            bio = bio, cep = cep, rua = rua, numero = numero,
            bairro = bairro, cidade = cidade, estado = estado
        )
        saved = true
    }

    fun consumeSaved() {
        saved = false
    }

    // Exclui um post exibido no perfil (só funciona se for do usuário logado, conferido
    // lá no PostDAO.delete) e já tira ele da lista em tela.
    fun deletePost(id: Long) = run {
        val currentUserId = UserSession.requireUser()
        withContext(Dispatchers.IO) { postDao.delete(id, currentUserId) }
        posts = posts.filterNot { it.id == id }
    }
}
