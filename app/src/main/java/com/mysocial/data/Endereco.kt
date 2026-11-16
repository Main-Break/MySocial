package com.mysocial.data

data class Endereco(
    val id: Long,
    val userId: Long,
    val apelido: String,
    val cep: String,
    val rua: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val estado: String
)
