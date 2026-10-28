package com.mysocial.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal object UserSession {
    var userId by mutableStateOf<Long?>(null)
        private set

    fun start(id: Long) {
        require(id > 0)
        userId = id
    }

    fun clear() {
        userId = null
    }

    fun requireUser(): Long = requireNotNull(userId) { "Faça login para continuar" }
}
