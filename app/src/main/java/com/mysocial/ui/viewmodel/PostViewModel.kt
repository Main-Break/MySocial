package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.UserSession
import com.mysocial.data.Post
import com.mysocial.data.dal.PostDAO
import com.mysocial.validation.PostValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Ponte entre as telas de feed/novo post e o banco de dados, no mesmo padrão do
// AuthViewModel: a tela só chama list()/create() e lê os estados (posts, loading,
// message, published) pra saber o que mostrar.
class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PostDAO(application)

    // Lista de posts carregada do banco, pronta pra desenhar o feed.
    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    // Vira true assim que um post novo é publicado com sucesso. A tela de novo post
    // observa isso pra voltar pro feed, e depois chama consumePublished() pra resetar.
    var published by mutableStateOf(false)
        private set

    // Mesma ideia do "run" do AuthViewModel: evita clique duplo e transforma erro
    // numa mensagem amigável.
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
                // Erro vindo do PostValidator/require (ex: post vazio ou grande demais).
                message = error.message
            } catch (error: Exception) {
                message = "Ocorreu um erro inesperado!"
            } finally {
                loading = false
            }
        }
    }

    // Busca todos os posts no banco (mais recente primeiro) pra exibir no feed.
    fun list() = run {
        val result = withContext(Dispatchers.IO) { dao.list() }
        posts = result
    }

    // Cria um post novo pro usuário logado. Exige estar logado (UserSession.requireUser)
    // e valida o tamanho do texto antes de gravar no banco.
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
}
