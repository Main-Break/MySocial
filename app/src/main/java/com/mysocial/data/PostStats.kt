package com.mysocial.data

// Dados agregados de um post que não vivem na própria tabela "posts" (curtidas e
// comentários são tabelas separadas). Montado pelos ViewModels que listam posts
// (PostViewModel, ProfileViewModel) a partir de LikeDAO/CommentDAO.
data class PostStats(
    val likes: Int = 0,
    val comments: Int = 0,
    val likedByMe: Boolean = false
)
