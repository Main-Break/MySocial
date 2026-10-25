package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Post
import com.mysocial.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DAO (Data Access Object) da tabela "posts". Mesma ideia do UserDAO: só essa classe
// conversa com a tabela, e toda query usa "?" pra evitar SQL Injection.
internal class PostDAO(context: Context) {
    private val appContext = context.applicationContext

    // Cria um post novo pro usuário informado. A validação de tamanho (limite de 280
    // caracteres) já aconteceu antes, lá no PostViewModel/PostValidator; aqui só grava.
    fun create(userId: Long, content: String) {
        AppDatabase(appContext).use { db ->
            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).format(Date())
            val values = ContentValues().apply {
                put("user_id", userId)
                put("content", content)
                put("created_at", createdAt)
            }
            db.writableDatabase.insertOrThrow("posts", null, values)
        }
    }

    // Lista todos os posts pro feed, já trazendo o username de quem postou (JOIN com a
    // tabela users), do mais recente pro mais antigo.
    fun list(): List<Post> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, posts.content, posts.created_at
            FROM posts
            JOIN users ON users.id = posts.user_id
            ORDER BY posts.created_at DESC, posts.id DESC
            """.trimIndent(),
            null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Post(
                            id = cursor.getLong(0),
                            userId = cursor.getLong(1),
                            author = cursor.getString(2),
                            content = cursor.getString(3),
                            createdAt = cursor.getString(4)
                        )
                    )
                }
            }
        }
    }

    // Exclui um post. O "user_id = ?" na cláusula garante que só o dono do post
    // consegue excluí-lo, mesmo que o id do post seja adivinhado/forjado.
    fun delete(id: Long, currentUserId: Long) {
        require(currentUserId > 0) { "Faça login para continuar" }
        AppDatabase(appContext).use { db ->
            val deleted = db.writableDatabase.delete(
                "posts", "id = ? AND user_id = ?",
                arrayOf(id.toString(), currentUserId.toString())
            )
            require(deleted == 1) { "Post não encontrado, atualize a lista" }
        }
    }
}
