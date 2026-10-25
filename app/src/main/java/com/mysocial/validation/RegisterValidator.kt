package com.mysocial.validation

import android.util.Patterns

// Concentra as regras de "o que é um cadastro válido" num lugar só, em vez de espalhar
// esses "if" dentro da tela ou do ViewModel. Se algum campo for inválido, lança uma
// IllegalArgumentException com uma mensagem pronta pra mostrar pro usuário.
object RegisterValidator {
    fun validate(username: String, email: String, password: String) {
        validateUsername(username)
        require(
            email.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) { "Informe um e-mail válido" }
        validatePassword(password)
    }

    private fun validatePassword(password: String) {
        require(password.length in 6..128 && password.isNotBlank()) {
            "Use uma senha entre 6 e 128 caracteres"
        }
    }

    private fun validateUsername(username: String) {
        require(username.length in 3..30) {
            "Informe um usuário entre 3 e 30 caracteres"
        }
    }
}
