package com.mysocial.data

data class Post(
    val id: Long,
    val userId: Long,
    val author: String,
    val authorCoverColor: String,
    val content: String,
    val createdAt: String
)
