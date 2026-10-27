package com.mysocial.ui.viewmodel

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.AuthManager
import com.mysocial.auth.UserSession
import com.mysocial.data.dal.UserDAO
import com.mysocial.validation.RegisterValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Ponte entre a tela de Login/Cadastro e o banco de dados. A tela (LoginScreen/
// RegisterScreen) só chama login()/register() e lê loading/message/done pra
// saber o que mostrar; toda a lógica de validar, gerar hash e acessar o banco fica aqui.
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UserDAO(application)

    // true enquanto uma operação (login/cadastro) está em andamento, pra desabilitar botão
    // e evitar o usuário clicar duas vezes.
    var loading by mutableStateOf(false)
        private set

    // Mensagem de erro pra mostrar na tela (ex: "e-mail já cadastrado"). Null = sem erro.
    var message by mutableStateOf<String?>(null)
        private set

    // Vira true quando login/cadastro terminou com sucesso. A tela observa isso num
    // LaunchedEffect pra navegar e depois chama consumeResult() pra zerar de novo.
    var done by mutableStateOf(false)
        private set

    // Função auxiliar que todo login/cadastro passa por dentro: controla o loading,
    // roda a operação numa coroutine e transforma qualquer erro numa mensagem amigável.
    private fun run(op: suspend () -> Boolean) {
        if (loading || done) return
        loading = true
        message = null

        viewModelScope.launch {
            try {
                done = withContext(Dispatchers.IO) { op() }
                if (!done) message = "E-mail ou senha inválido."
            } catch (e: SQLiteConstraintException) {
                // Disparado pelo UNIQUE da tabela (username ou e-mail repetido).
                message = "Não foi possível cadastrar. Confira se o e-mail/usuário já está em uso"
            } catch (e: IllegalArgumentException) {
                // Erro vindo do RegisterValidator/require (ex: senha curta demais).
                message = e.message
            } catch (e: Exception) {
                message = "Ocorreu um erro inesperado."
            } finally {
                loading = false
            }
        }
    }

    // Cadastra um usuário novo: valida os campos, gera um salt+hash pra senha
    // e só então grava no banco.
    fun register(username: String, email: String, password: String) = run {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim().lowercase(Locale.ROOT)
        val trimmedPassword = password.trim()

        RegisterValidator.validate(trimmedUsername, trimmedEmail, trimmedPassword)
        val salt = AuthManager.createSalt()
        val passwordHash = AuthManager.generateHash(password, salt)
        val coverColor = AuthManager.pickCoverColor(trimmedUsername)
        try {
            dao.register(trimmedUsername, trimmedEmail, passwordHash, salt, coverColor)
            true
        } finally {
            // Limpa o hash da memória depois de gravar, não precisa mais dele aqui.
            passwordHash.fill(0)
        }
    }

    // Confere e-mail (ou @username) + senha contra o que está salvo no banco. Se bater,
    // marca a sessão como logada (UserSession.start).
    fun login(identifier: String, password: String) = run {
        require(identifier.length <= 254 && password.length in 6..128) {
            "E-mail/usuário ou senha inválidos"
        }
        // Remove o "@" caso a pessoa digite "@username" (igual ao X), e ignora
        // maiúscula/minúscula tanto no e-mail quanto no username.
        val trimmedIdentifier = identifier.trim().removePrefix("@").lowercase(Locale.ROOT)
        val authenticatedId = withContext(Dispatchers.IO) {
            val stored = dao.findPassword(trimmedIdentifier)
            // Mesmo se o e-mail não existir, gera um hash de um valor "vazio" aqui embaixo,
            // só pra a operação demorar um tempo parecido com a de um e-mail que existe.
            // Isso evita que alguém descubra quais e-mails estão cadastrados só medindo
            // quanto tempo o login demorou pra responder.
            val passwordHash = stored?.hash ?: ByteArray(32)
            val salt = stored?.salt ?: ByteArray(16)

            try {
                val authenticated = AuthManager.validatePassword(password, salt, passwordHash)
                if (authenticated) stored?.userId else null
            } finally {
                passwordHash.fill(0)
            }
        }
        if (authenticatedId != null) {
            UserSession.start(authenticatedId)
            true
        } else false
    }

    // Chamado pela tela depois que ela já reagiu ao "done = true" (ex: já navegou),
    // pra resetar o estado e deixar o ViewModel pronto pra uma próxima operação.
    fun consumeResult() {
        done = false
    }
}
