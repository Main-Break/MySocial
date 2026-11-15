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

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UserDAO(application)

    var loading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var done by mutableStateOf(false)
        private set

    private fun run(op: suspend () -> Boolean) {
        if (loading || done) return
        loading = true
        message = null

        viewModelScope.launch {
            try {
                done = withContext(Dispatchers.IO) { op() }
                if (!done) message = "E-mail ou senha inválido."
            } catch (e: SQLiteConstraintException) {
                message = "Não foi possível cadastrar. Confira se o e-mail/usuário já está em uso"
            } catch (e: IllegalArgumentException) {
                message = e.message
            } catch (e: Exception) {
                message = "Ocorreu um erro inesperado."
            } finally {
                loading = false
            }
        }
    }

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
            passwordHash.fill(0)
        }
    }

    fun login(identifier: String, password: String) = run {
        require(identifier.length <= 254 && password.length in 6..128) {
            "E-mail/usuário ou senha inválidos"
        }
        val trimmedIdentifier = identifier.trim().removePrefix("@").lowercase(Locale.ROOT)
        val authenticatedId = withContext(Dispatchers.IO) {
            val stored = dao.findPassword(trimmedIdentifier)
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

    fun consumeResult() {
        done = false
    }
}
