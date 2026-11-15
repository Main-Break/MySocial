package com.mysocial.validation

object PostValidator {
    const val MAX_LENGTH = 280

    fun validate(content: String) {
        require(content.isNotBlank() && content.length <= MAX_LENGTH) {
            "O post deve ter entre 1 e $MAX_LENGTH caracteres"
        }
    }
}
