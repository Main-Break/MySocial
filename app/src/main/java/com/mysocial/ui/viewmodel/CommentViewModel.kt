package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.UserSession
import com.mysocial.data.Comment
import com.mysocial.data.dal.CommentDAO
import com.mysocial.validation.CommentValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Ponte entre a tela de detalhe do post e o banco, mesmo padrão MVVM do PostViewModel:
// a tela só chama load()/create()/delete() e lê "comments" pra saber o que mostrar.
class CommentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CommentDAO(application)

    var comments by mutableStateOf<List<Comment>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
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

    // Busca todos os comentários de um post, mais antigo primeiro.
    fun load(postId: Long) = run {
        val result = withContext(Dispatchers.IO) { dao.listByPost(postId) }
        comments = result
    }

    // Cria um comentário novo pro usuário logado e já recarrega a lista.
    fun create(postId: Long, content: String) = run {
        val userId = UserSession.requireUser()
        val trimmedContent = content.trim()
        CommentValidator.validate(trimmedContent)
        withContext(Dispatchers.IO) {
            dao.create(postId, userId, trimmedContent)
        }
        comments = withContext(Dispatchers.IO) { dao.listByPost(postId) }
    }

    // Exclui um comentário do usuário logado e já tira ele da lista em tela.
    fun delete(id: Long) = run {
        val userId = UserSession.requireUser()
        withContext(Dispatchers.IO) { dao.delete(id, userId) }
        comments = comments.filterNot { it.id == id }
    }
}
