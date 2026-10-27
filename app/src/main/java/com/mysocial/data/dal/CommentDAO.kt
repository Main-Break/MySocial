package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Comment
import com.mysocial.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DAO (Data Access Object) da tabela "comments". Mesmo padrão do PostDAO: toda query
// usa "?" pra evitar SQL Injection, e o "created_at" é gerado aqui, na hora de gravar.
internal class CommentDAO(context: Context) {
    private val appContext = context.applicationContext

    // Cria um comentário novo num post. A validação de tamanho já aconteceu antes, lá
    // no ViewModel/CommentValidator; aqui só grava.
    fun create(postId: Long, userId: Long, content: String) {
        AppDatabase(appContext).use { db ->
            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).format(Date())
            val values = ContentValues().apply {
                put("post_id", postId)
                put("user_id", userId)
                put("content", content)
                put("created_at", createdAt)
            }
            db.writableDatabase.insertOrThrow("comments", null, values)
        }
    }

    // Lista os comentários de um post, mais antigo primeiro (ordem de conversa), já
    // trazendo o username de quem comentou (JOIN com a tabela users).
    fun listByPost(postId: Long): List<Comment> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT comments.id, comments.post_id, comments.user_id, users.username,
                   comments.content, comments.created_at
            FROM comments
            JOIN users ON users.id = comments.user_id
            WHERE comments.post_id = ?
            ORDER BY comments.created_at ASC, comments.id ASC
            """.trimIndent(),
            arrayOf(postId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Comment(
                            id = cursor.getLong(0),
                            postId = cursor.getLong(1),
                            userId = cursor.getLong(2),
                            author = cursor.getString(3),
                            content = cursor.getString(4),
                            createdAt = cursor.getString(5)
                        )
                    )
                }
            }
        }
    }

    // Quantos comentários um post tem, pro contador no card do post.
    fun countByPost(postId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "comments", arrayOf("id"), "post_id = ?",
            arrayOf(postId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }

    // Exclui um comentário. O "user_id = ?" garante que só o dono do comentário
    // consegue excluí-lo.
    fun delete(id: Long, currentUserId: Long) {
        AppDatabase(appContext).use { db ->
            val deleted = db.writableDatabase.delete(
                "comments", "id = ? AND user_id = ?",
                arrayOf(id.toString(), currentUserId.toString())
            )
            require(deleted == 1) { "Comentário não encontrado, atualize a lista" }
        }
    }
}
