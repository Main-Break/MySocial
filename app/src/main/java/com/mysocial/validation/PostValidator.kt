package com.mysocial.validation

// Regra de "o que é um post válido": não pode ser vazio/só espaço, e tem o mesmo limite
// de caracteres que o Twitter/X usa (280).
object PostValidator {
    // Fica público porque a tela de novo post (PostScreen) também usa esse número pra
    // travar a digitação e mostrar o contador "123/280".
    const val TAMANHO_MAXIMO = 280

    fun validar(conteudo: String) {
        require(conteudo.isNotBlank() && conteudo.length <= TAMANHO_MAXIMO) {
            "O post deve ter entre 1 e $TAMANHO_MAXIMO caracteres"
        }
    }
}
