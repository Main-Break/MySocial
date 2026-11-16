package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.auth.UserSession
import com.mysocial.data.Endereco
import com.mysocial.data.dal.EnderecoDAO
import com.mysocial.validation.EnderecoValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EnderecoViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = EnderecoDAO(application)

    var enderecos by mutableStateOf<List<Endereco>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    var saved by mutableStateOf(false)
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

    fun load() = run {
        val userId = UserSession.requireUser()
        enderecos = withContext(Dispatchers.IO) { dao.listByUser(userId) }
    }

    fun create(apelido: String, cep: String, rua: String, numero: String, bairro: String, cidade: String, estado: String) = run {
        val userId = UserSession.requireUser()
        val trimmedApelido = apelido.trim()
        EnderecoValidator.validate(trimmedApelido)
        withContext(Dispatchers.IO) { dao.create(userId, trimmedApelido, cep, rua, numero, bairro, cidade, estado) }
        enderecos = withContext(Dispatchers.IO) { dao.listByUser(userId) }
        saved = true
    }

    fun update(id: Long, apelido: String, cep: String, rua: String, numero: String, bairro: String, cidade: String, estado: String) = run {
        val userId = UserSession.requireUser()
        val trimmedApelido = apelido.trim()
        EnderecoValidator.validate(trimmedApelido)
        withContext(Dispatchers.IO) { dao.update(id, userId, trimmedApelido, cep, rua, numero, bairro, cidade, estado) }
        enderecos = withContext(Dispatchers.IO) { dao.listByUser(userId) }
        saved = true
    }

    fun delete(id: Long) = run {
        val userId = UserSession.requireUser()
        withContext(Dispatchers.IO) { dao.delete(id, userId) }
        enderecos = enderecos.filterNot { it.id == id }
    }

    fun consumeSaved() {
        saved = false
    }
}
