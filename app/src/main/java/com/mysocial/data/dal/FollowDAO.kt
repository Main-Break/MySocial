package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mysocial.data.User
import com.mysocial.data.local.AppDatabase

internal class FollowDAO(context: Context) {
    private val appContext = context.applicationContext

    fun follow(followerId: Long, followedId: Long) {
        require(followerId != followedId) { "Você não pode seguir você mesmo" }
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("follower_id", followerId)
                put("followed_id", followedId)
            }
            db.writableDatabase.insertWithOnConflict(
                "follows", null, values, SQLiteDatabase.CONFLICT_IGNORE
            )
        }
    }

    fun unfollow(followerId: Long, followedId: Long) {
        AppDatabase(appContext).use { db ->
            db.writableDatabase.delete(
                "follows", "follower_id = ? AND followed_id = ?",
                arrayOf(followerId.toString(), followedId.toString())
            )
        }
    }

    fun isFollowing(followerId: Long, followedId: Long): Boolean = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "follows", arrayOf("follower_id"),
            "follower_id = ? AND followed_id = ?",
            arrayOf(followerId.toString(), followedId.toString()),
            null, null, null
        ).use { cursor -> cursor.count > 0 }
    }

    fun countFollowers(userId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "follows", arrayOf("follower_id"), "followed_id = ?",
            arrayOf(userId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }

    fun countFollowing(userId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "follows", arrayOf("followed_id"), "follower_id = ?",
            arrayOf(userId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }

    fun listFollowers(userId: Long): List<User> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT users.id, users.username, users.email, users.cover_color
            FROM follows
            JOIN users ON users.id = follows.follower_id
            WHERE follows.followed_id = ?
            ORDER BY users.username COLLATE NOCASE
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(User(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
                }
            }
        }
    }

    fun listFollowing(userId: Long): List<User> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT users.id, users.username, users.email, users.cover_color
            FROM follows
            JOIN users ON users.id = follows.followed_id
            WHERE follows.follower_id = ?
            ORDER BY users.username COLLATE NOCASE
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(User(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3)))
                }
            }
        }
    }
}
