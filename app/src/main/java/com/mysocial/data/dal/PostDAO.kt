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

    // Lista só os posts de um usuário (usado na tela de Perfil), mesmo JOIN e ordenação
    // do list() acima, só que filtrado por "user_id".
    fun listByUser(userId: Long): List<Post> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, posts.content, posts.created_at
            FROM posts
            JOIN users ON users.id = posts.user_id
            WHERE posts.user_id = ?
            ORDER BY posts.created_at DESC, posts.id DESC
            """.trimIndent(),
            arrayOf(userId.toString())
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

    // Busca um post específico, mas só se pertencer ao usuário informado (mesma ideia de
    // segurança do update/delete abaixo). Usado pra carregar os dados na tela de edição.
    fun findById(id: Long, currentUserId: Long): Post? = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, posts.content, posts.created_at
            FROM posts
            JOIN users ON users.id = posts.user_id
            WHERE posts.id = ? AND posts.user_id = ?
            """.trimIndent(),
            arrayOf(id.toString(), currentUserId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) Post(
                id = cursor.getLong(0),
                userId = cursor.getLong(1),
                author = cursor.getString(2),
                content = cursor.getString(3),
                createdAt = cursor.getString(4)
            ) else null
        }
    }

    // Altera o conteúdo de um post. O "user_id = ?" na cláusula garante que só o dono
    // do post consegue editá-lo, mesmo que o id do post seja adivinhado/forjado.
    fun update(id: Long, currentUserId: Long, content: String) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply { put("content", content) }
            val updated = db.writableDatabase.update(
                "posts", values, "id = ? AND user_id = ?",
                arrayOf(id.toString(), currentUserId.toString())
            )
            require(updated == 1) { "Post não encontrado, atualize a lista" }
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
