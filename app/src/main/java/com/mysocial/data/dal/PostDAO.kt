package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Post
import com.mysocial.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class PostDAO(context: Context) {
    private val appContext = context.applicationContext

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

    fun list(): List<Post> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, users.cover_color, posts.content, posts.created_at
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
                            authorCoverColor = cursor.getString(3),
                            content = cursor.getString(4),
                            createdAt = cursor.getString(5)
                        )
                    )
                }
            }
        }
    }

    fun listByUser(userId: Long): List<Post> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, users.cover_color, posts.content, posts.created_at
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
                            authorCoverColor = cursor.getString(3),
                            content = cursor.getString(4),
                            createdAt = cursor.getString(5)
                        )
                    )
                }
            }
        }
    }

    fun findById(id: Long): Post? = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, users.cover_color, posts.content, posts.created_at
            FROM posts
            JOIN users ON users.id = posts.user_id
            WHERE posts.id = ?
            """.trimIndent(),
            arrayOf(id.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) Post(
                id = cursor.getLong(0),
                userId = cursor.getLong(1),
                author = cursor.getString(2),
                authorCoverColor = cursor.getString(3),
                content = cursor.getString(4),
                createdAt = cursor.getString(5)
            ) else null
        }
    }

    fun findById(id: Long, currentUserId: Long): Post? = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.user_id, users.username, users.cover_color, posts.content, posts.created_at
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
                authorCoverColor = cursor.getString(3),
                content = cursor.getString(4),
                createdAt = cursor.getString(5)
            ) else null
        }
    }

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
