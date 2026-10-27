package com.mysocial.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginUser

@Serializable
object RegisterUser

@Serializable
object Feed

@Serializable
object RegisterPost

@Serializable
data class EditPost(val postId: Long)

@Serializable
data class DetailPost(val postId: Long)

@Serializable
data class Profile(val userId: Long)

@Serializable
object EditProfile

@Serializable
data class Follows(val userId: Long, val type: TypeListFollows)

@Serializable
object Users

@Serializable
enum class TypeListFollows { FOLLOW, FOLLOWERS }
// Seguindo / Seguidores