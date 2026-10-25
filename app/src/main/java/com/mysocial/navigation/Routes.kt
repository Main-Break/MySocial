package com.mysocial.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Cadastro

@Serializable
object Feed

@Serializable
object CriarPost

@Serializable
data class DetalhePost(val postId: Long)

@Serializable
data class Perfil(val userId: Long)

@Serializable
data class ListaRelacionamentos(val userId: Long, val tipo: TipoLista)

@Serializable
enum class TipoLista { SEGUIDORES, SEGUINDO }