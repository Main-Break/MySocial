package com.mysocial.data

// Um comentário feito num post. "author" é o username de quem comentou (igual ao
// "author" de Post), já vindo pronto do JOIN feito no CommentDAO.
data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val author: String,
    val content: String,
    val createdAt: String
)
