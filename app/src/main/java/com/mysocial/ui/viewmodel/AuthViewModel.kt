package com.mysocial.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.AuthManager
import com.mysocial.auth.SessaoUsuario
import com.mysocial.data.dal.UsuarioDAO
import com.mysocial.validation.CadastroValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Ponte entre a tela de Login/Cadastro e o banco de dados. A tela (LoginScreen/
// RegisterScreen) só chama login()/cadastrar() e lê carregando/mensagem/concluido pra
// saber o que mostrar; toda a lógica de validar, gerar hash e acessar o banco fica aqui.
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UsuarioDAO(application)

    // true enquanto uma operação (login/cadastro) está em andamento, pra desabilitar botão
    // e evitar o usuário clicar duas vezes.
    var carregando by mutableStateOf(false)
        private set

    // Mensagem de erro pra mostrar na tela (ex: "e-mail já cadastrado"). Null = sem erro.
    var mensagem by mutableStateOf<String?>(null)
        private set

    // Vira true quando login/cadastro terminou com sucesso. A tela observa isso num
    // LaunchedEffect pra navegar e depois chama consumirResultado() pra zerar de novo.
    var concluido by mutableStateOf(false)
        private set

    // Função auxiliar que todo login/cadastro passa por dentro: controla o carregando,
    // roda a operação numa coroutine e transforma qualquer erro numa mensagem amigável.
    private fun executar(op: suspend () -> Boolean) {
        if (carregando || concluido) return
        carregando = true
        mensagem = null

        viewModelScope.launch {
            try {
                concluido = withContext(Dispatchers.IO) { op() }
                if (!concluido) mensagem = "E-mail ou senha inválido."
            } catch (e: SQLiteConstraintException) {
                // Disparado pelo UNIQUE da tabela (username ou e-mail repetido).
                mensagem = "Não foi possível cadastrar. Confira se o e-mail/usuário já está em uso"
            } catch (e: IllegalArgumentException) {
                // Erro vindo do CadastroValidator/require (ex: senha curta demais).
                mensagem = e.message
            } catch (e: Exception) {
                mensagem = "Ocorreu um erro inesperado."
            } finally {
                carregando = false
            }
        }
    }

    // Cadastra um usuário novo: valida os campos, gera um salt+hash pra senha
    // e só então grava no banco.
    fun cadastrar(username: String, email: String, senha: String) = executar {
        val usernameLimpo = username.trim()
        val emailLimpo = email.trim().lowercase(Locale.ROOT)
        val senhaLimpa = senha.trim()

        CadastroValidator.validar(usernameLimpo, emailLimpo, senhaLimpa)
        val salt = AuthManager.criarSalt()
        val senhaHash = AuthManager.gerarHash(senha, salt)
        try {
            dao.cadastrar(usernameLimpo, emailLimpo, senhaHash, salt)
            true
        } finally {
            // Limpa o hash da memória depois de gravar, não precisa mais dele aqui.
            senhaHash.fill(0)
        }
    }

    // Confere e-mail/senha contra o que está salvo no banco. Se bater, marca a sessão
    // como logada (SessaoUsuario.iniciar).
    fun login(email: String, senha: String) = executar {
        require(email.length <= 254 && senha.length in 6..128) {
            "E-mail ou senha inválidos"
        }
        val idAutenticado = withContext(Dispatchers.IO) {
            val dados = dao.buscarSenha(email.trim().lowercase(Locale.ROOT))
            // Mesmo se o e-mail não existir, gera um hash de um valor "vazio" aqui embaixo,
            // só pra a operação demorar um tempo parecido com a de um e-mail que existe.
            // Isso evita que alguém descubra quais e-mails estão cadastrados só medindo
            // quanto tempo o login demorou pra responder.
            val senhaHash = dados?.hash ?: ByteArray(32)
            val salt = dados?.salt ?: ByteArray(16)

            try {
                val autenticado = AuthManager.validarSenha(senha, salt, senhaHash)
                if (autenticado) dados?.usuarioId else null
            } finally {
                senhaHash.fill(0)
            }
        }
        if (idAutenticado != null) {
            SessaoUsuario.iniciar(idAutenticado)
            true
        } else false
    }

    // Chamado pela tela depois que ela já reagiu ao "concluido = true" (ex: já navegou),
    // pra resetar o estado e deixar o ViewModel pronto pra uma próxima operação.
    fun consumirResultado() {
        concluido = false
    }
}
