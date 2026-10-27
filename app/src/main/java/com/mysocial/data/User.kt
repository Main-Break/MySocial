package com.mysocial.data

// "coverColor" é a cor de fundo da capa do perfil (estilo X), um texto hexadecimal
// (ex: "#1D9BF0") escolhido uma vez no cadastro e guardado no banco. "bio" e o
// endereço (cep/rua/numero/bairro/cidade/estado) começam vazios e são preenchidos
// depois pelo próprio usuário, na tela de editar perfil.
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val coverColor: String,
    val bio: String = "",
    val cep: String = "",
    val rua: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = ""
)
