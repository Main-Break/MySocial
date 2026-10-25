package com.mysocial.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Guarda, em memória, quem é o usuário logado no app agora. É um "object" (existe uma
// única instância pro app inteiro), então qualquer tela/ViewModel pode consultar
// UserSession.userId sem precisar passar esse valor manualmente de tela em tela.
// Como usa mutableStateOf, a navegação consegue "reagir" sozinha quando o login muda
// (ver o LaunchedEffect em AppNavigation.kt).
internal object UserSession {
    var userId by mutableStateOf<Long?>(null)
        private set

    // Chamado assim que o login dá certo.
    fun start(id: Long) {
        require(id > 0)
        userId = id
    }

    // Chamado no logout (ainda não tem tela de logout, mas a função já fica pronta).
    fun clear() {
        userId = null
    }

    // Usado nas telas/ações que exigem estar logado (ex: criar post). Se ninguém estiver
    // logado, já quebra na hora com uma mensagem clara, em vez de deixar passar um id nulo
    // escondido e dar erro em outro lugar mais difícil de rastrear.
    fun requireUser(): Long = requireNotNull(userId) { "Faça login para continuar" }
}
