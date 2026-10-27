package com.mysocial.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mysocial.data.User
import com.mysocial.data.dal.FollowDAO
import com.mysocial.navigation.TypeListFollows
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Ponte entre a FollowsScreen e o banco: a tela só chama load() e lê "users" pra saber
// o que mostrar. Mesmo padrão MVVM dos outros ViewModels do projeto.
class FollowsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FollowDAO(application)

    var users by mutableStateOf<List<User>>(emptyList())
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

    // Busca a lista certa (seguidores ou seguindo) de acordo com o "type" da rota.
    fun load(userId: Long, type: TypeListFollows) = run {
        val result = withContext(Dispatchers.IO) {
            when (type) {
                TypeListFollows.FOLLOWERS -> dao.listFollowers(userId)
                TypeListFollows.FOLLOW -> dao.listFollowing(userId)
            }
        }
        users = result
    }
}
