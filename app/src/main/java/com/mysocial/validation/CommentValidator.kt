package com.mysocial.validation

// Regra de "o que é um comentário válido": mesmo limite de 280 caracteres dos posts,
// não pode ser vazio/só espaço.
object CommentValidator {
    const val MAX_LENGTH = 280

    fun validate(content: String) {
        require(content.isNotBlank() && content.length <= MAX_LENGTH) {
            "O comentário deve ter entre 1 e $MAX_LENGTH caracteres"
        }
    }
}
