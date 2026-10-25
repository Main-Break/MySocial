package com.mysocial.validation

import android.util.Patterns

// Concentra as regras de "o que é um cadastro válido" num lugar só, em vez de espalhar
// esses "if" dentro da tela ou do ViewModel. Se algum campo for inválido, lança uma
// IllegalArgumentException com uma mensagem pronta pra mostrar pro usuário.
object CadastroValidator {
    fun validar(username: String, email: String, senha: String) {
        validarUsername(username)
        require(
            email.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) { "Informe um e-mail válido" }
        validarSenha(senha)
    }

    private fun validarSenha(senha: String) {
        require(senha.length in 6..128 && senha.isNotBlank()) {
            "Use uma senha entre 6 e 128 caracteres"
        }
    }

    private fun validarUsername(username: String) {
        require(username.length in 3..30) {
            "Informe um usuário entre 3 e 30 caracteres"
        }
    }
}
