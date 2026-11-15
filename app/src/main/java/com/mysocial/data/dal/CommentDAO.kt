package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Comment
import com.mysocial.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class CommentDAO(context: Context) {
    private val appContext = context.applicationContext

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

    fun countByPost(postId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "comments", arrayOf("id"), "post_id = ?",
            arrayOf(postId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }

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
