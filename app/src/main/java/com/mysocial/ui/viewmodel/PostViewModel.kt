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
import com.mysocial.data.dal.CommentDAO
import com.mysocial.data.dal.LikeDAO
import com.mysocial.data.dal.PostDAO
import com.mysocial.validation.PostValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PostDAO(application)
    private val likeDao = LikeDAO(application)
    private val commentDao = CommentDAO(application)

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var stats by mutableStateOf<Map<Long, PostStats>>(emptyMap())
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var published by mutableStateOf(false)
        private set

    var editTarget by mutableStateOf<Post?>(null)
        private set

    var viewingPost by mutableStateOf<Post?>(null)
        private set

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

    fun list() = run {
        val userId = UserSession.userId
        val result = withContext(Dispatchers.IO) {
            val loadedPosts = dao.list()
            val loadedStats = loadedPosts.associate { it.id to statsFor(it.id, userId) }
            loadedPosts to loadedStats
        }
        posts = result.first
        stats = result.second
    }

    private fun statsFor(postId: Long, userId: Long?): PostStats = PostStats(
        likes = likeDao.countLikes(postId),
        comments = commentDao.countByPost(postId),
        likedByMe = userId != null && likeDao.isLiked(userId, postId)
    )

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

    fun create(content: String) = run {
        val userId = UserSession.requireUser()
        val trimmedContent = content.trim()
        PostValidator.validate(trimmedContent)
        withContext(Dispatchers.IO) { dao.create(userId, trimmedContent) }
        published = true
    }

    fun consumePublished() {
        published = false
    }

    fun loadForView(id: Long) = run {
        val userId = UserSession.userId
        val result = withContext(Dispatchers.IO) {
            val post = dao.findById(id)
            val postStats = if (post != null) statsFor(id, userId) else null
            post to postStats
        }
        requireNotNull(result.first) { "Post não encontrado" }
        viewingPost = result.first
        if (result.second != null) stats = stats + (id to result.second!!)
    }

    fun loadForEdit(id: Long) = run {
        val userId = UserSession.requireUser()
        val post = withContext(Dispatchers.IO) { dao.findById(id, userId) }
        requireNotNull(post) { "Post não encontrado" }
        editTarget = post
    }

    fun update(id: Long, content: String) = run {
        val userId = UserSession.requireUser()
        val trimmedContent = content.trim()
        PostValidator.validate(trimmedContent)
        withContext(Dispatchers.IO) { dao.update(id, userId, trimmedContent) }
        published = true
    }

    fun delete(id: Long) = run {
        val userId = UserSession.requireUser()
        withContext(Dispatchers.IO) { dao.delete(id, userId) }
        posts = posts.filterNot { it.id == id }
        published = true
    }
}
