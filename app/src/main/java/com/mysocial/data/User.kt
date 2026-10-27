package com.mysocial.data

// "coverColor" é a cor de fundo da capa do perfil (estilo X), um texto hexadecimal
// (ex: "#1D9BF0") escolhido uma vez no cadastro e guardado no banco.
data class User(val id: Long, val username: String, val email: String, val coverColor: String)
