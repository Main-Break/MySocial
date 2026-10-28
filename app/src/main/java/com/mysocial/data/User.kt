package com.mysocial.data

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
