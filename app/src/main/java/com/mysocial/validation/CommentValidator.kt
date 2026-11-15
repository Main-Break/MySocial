package com.mysocial.validation

object CommentValidator {
    const val MAX_LENGTH = 280

    fun validate(content: String) {
        require(content.isNotBlank() && content.length <= MAX_LENGTH) {
            "O comentário deve ter entre 1 e $MAX_LENGTH caracteres"
        }
    }
}
