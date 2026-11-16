package com.mysocial.validation

object EnderecoValidator {
    const val MAX_APELIDO = 40

    fun validate(apelido: String) {
        require(apelido.isNotBlank() && apelido.length <= MAX_APELIDO) {
            "O apelido do endereço deve ter entre 1 e $MAX_APELIDO caracteres"
        }
    }
}
