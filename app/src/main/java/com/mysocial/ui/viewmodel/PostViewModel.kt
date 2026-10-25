package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.SessaoUsuario
import com.mysocial.data.Post
import com.mysocial.data.dal.PostDAO
import com.mysocial.validation.PostValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Ponte entre as telas de feed/novo post e o banco de dados, no mesmo padrão do
// AuthViewModel: a tela só chama listar()/criar() e lê os estados (posts, carregando,
// mensagem, publicado) pra saber o que mostrar.
class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = PostDAO(application)

    // Lista de posts carregada do banco, pronta pra desenhar o feed.
    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var mensagem by mutableStateOf<String?>(null)
        private set

    var carregando by mutableStateOf(false)
        private set

    // Vira true assim que um post novo é publicado com sucesso. A tela de novo post
    // observa isso pra voltar pro feed, e depois chama consumirPublicado() pra resetar.
    var publicado by mutableStateOf(false)
        private set

    // Mesma ideia do "executar" do AuthViewModel: evita clique duplo e transforma erro
    // numa mensagem amigável.
    private fun executar(operacao: suspend () -> Unit) {
        if (carregando) return
        carregando = true
        mensagem = null

        viewModelScope.launch {
            try {
                operacao()
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                // Erro vindo do PostValidator/require (ex: post vazio ou grande demais).
                mensagem = erro.message
            } catch (erro: Exception) {
                mensagem = "Ocorreu um erro inesperado!"
            } finally {
                carregando = false
            }
        }
    }

    // Busca todos os posts no banco (mais recente primeiro) pra exibir no feed.
    fun listar() = executar {
        val lista = withContext(Dispatchers.IO) { dao.listar() }
        posts = lista
    }

    // Cria um post novo pro usuário logado. Exige estar logado (SessaoUsuario.exigirUsuario)
    // e valida o tamanho do texto antes de gravar no banco.
    fun criar(conteudo: String) = executar {
        val usuarioId = SessaoUsuario.exigirUsuario()
        val conteudoLimpo = conteudo.trim()
        PostValidator.validar(conteudoLimpo)
        withContext(Dispatchers.IO) { dao.criar(usuarioId, conteudoLimpo) }
        publicado = true
    }

    fun consumirPublicado() {
        publicado = false
    }
}
