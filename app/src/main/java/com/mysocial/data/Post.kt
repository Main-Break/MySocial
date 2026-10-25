package com.mysocial.data

data class Post(
    val id: Long,
    val usuarioId: Long,
    val autor: String,
    val conteudo: String,
    val dataCriacao: String
)
