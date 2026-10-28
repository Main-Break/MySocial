package com.mysocial.data

data class Comment(
    val id: Long,
    val postId: Long,
    val userId: Long,
    val author: String,
    val content: String,
    val createdAt: String
)
