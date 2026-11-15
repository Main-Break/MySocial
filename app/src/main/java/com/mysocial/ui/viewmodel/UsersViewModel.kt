package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.data.User
import com.mysocial.data.dal.UserDAO
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = UserDAO(application)

    private var todos by mutableStateOf<List<User>>(emptyList())

    var users by mutableStateOf<List<User>>(emptyList())
        private set

    var query by mutableStateOf("")
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
            } catch (error: Exception) {
                message = "Ocorreu um erro inesperado!"
            } finally {
                loading = false
            }
        }
    }

    fun load() = run {
        val result = withContext(Dispatchers.IO) { dao.list() }
        todos = result
        users = result
    }

    fun search(texto: String) {
        query = texto
        users = if (texto.isBlank()) todos
        else todos.filter { it.username.contains(texto, ignoreCase = true) }
    }
}
