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

    // Vira true assim que um post novo é publicado (ou uma edição é salva) com sucesso.
    // A tela de novo post/edição observa isso pra voltar pro feed, e depois chama
    // consumePublished() pra resetar.
    var published by mutableStateOf(false)
        private set

    // Post carregado pra edição (ver loadForEdit). Fica null enquanto não carrega ou se
    // o post não existir/não for do usuário logado.
    var editTarget by mutableStateOf<Post?>(null)
        private set

    // Post carregado pra tela de detalhe (ver loadForView). Diferente do editTarget,
    // qualquer post pode ser visualizado, não só os do usuário logado.
    var viewingPost by mutableStateOf<Post?>(null)
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

    // Busca um post qualquer pra mostrar na tela de detalhe.
    fun loadForView(id: Long) = run {
        val post = withContext(Dispatchers.IO) { dao.findById(id) }
        requireNotNull(post) { "Post não encontrado" }
        viewingPost = post
    }

    // Busca um post do usuário logado pra preencher a tela de edição.
    fun loadForEdit(id: Long) = run {
        val userId = UserSession.requireUser()
        val post = withContext(Dispatchers.IO) { dao.findById(id, userId) }
        requireNotNull(post) { "Post não encontrado" }
        editTarget = post
    }

    // Salva a edição de um post já existente (só funciona se o post for do usuário logado,
    // conferido lá no PostDAO.update).
    fun update(id: Long, content: String) = run {
        val userId = UserSession.requireUser()
        val trimmedContent = content.trim()
        PostValidator.validate(trimmedContent)
        withContext(Dispatchers.IO) { dao.update(id, userId, trimmedContent) }
        published = true
    }

    // Exclui um post do usuário logado e já tira ele da lista em tela, sem precisar
    // recarregar tudo de novo do banco. Também marca "published" como sinal de operação
    // concluída, pra telas como a de detalhe do post saberem que já podem voltar.
    fun delete(id: Long) = run {
        val userId = UserSession.requireUser()
        withContext(Dispatchers.IO) { dao.delete(id, userId) }
        posts = posts.filterNot { it.id == id }
        published = true
    }
}
