package com.mysocial.data

// "authorCoverColor" é a cor de perfil de quem postou (ver User.coverColor), já vindo
// pronta do JOIN feito no PostDAO, usada pra colorir o avatar do post no card.
data class Post(
    val id: Long,
    val userId: Long,
    val author: String,
    val authorCoverColor: String,
    val content: String,
    val createdAt: String
)
