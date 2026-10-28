package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mysocial.data.local.AppDatabase

internal class LikeDAO(context: Context) {
    private val appContext = context.applicationContext

    fun like(userId: Long, postId: Long) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("user_id", userId)
                put("post_id", postId)
            }
            db.writableDatabase.insertWithOnConflict(
                "likes", null, values, SQLiteDatabase.CONFLICT_IGNORE
            )
        }
    }

    fun unlike(userId: Long, postId: Long) {
        AppDatabase(appContext).use { db ->
            db.writableDatabase.delete(
                "likes", "user_id = ? AND post_id = ?",
                arrayOf(userId.toString(), postId.toString())
            )
        }
    }

    fun isLiked(userId: Long, postId: Long): Boolean = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "likes", arrayOf("user_id"),
            "user_id = ? AND post_id = ?",
            arrayOf(userId.toString(), postId.toString()),
            null, null, null
        ).use { cursor -> cursor.count > 0 }
    }

    fun countLikes(postId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "likes", arrayOf("user_id"), "post_id = ?",
            arrayOf(postId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }
}
