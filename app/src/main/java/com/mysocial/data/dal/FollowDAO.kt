package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mysocial.data.User
import com.mysocial.data.local.AppDatabase

// DAO (Data Access Object) da tabela "follows". Mesmo padrão dos outros DAOs: só essa
// classe conversa com a tabela, e toda query usa "?" pra evitar SQL Injection.
internal class FollowDAO(context: Context) {
    private val appContext = context.applicationContext

    // Passa a seguir alguém. Usa CONFLICT_IGNORE porque a chave primária composta
    // (follower_id, followed_id) já impede duplicar: se o usuário clicar duas vezes
    // rápido em "Seguir", a segunda chamada só não faz nada, em vez de quebrar com erro.
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

    // Deixa de seguir alguém.
    fun unfollow(followerId: Long, followedId: Long) {
        AppDatabase(appContext).use { db ->
            db.writableDatabase.delete(
                "follows", "follower_id = ? AND followed_id = ?",
                arrayOf(followerId.toString(), followedId.toString())
            )
        }
    }

    // Confere se "followerId" já segue "followedId", usado pra decidir se o botão da
    // tela de perfil mostra "Seguir" ou "Deixar de seguir".
    fun isFollowing(followerId: Long, followedId: Long): Boolean = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "follows", arrayOf("follower_id"),
            "follower_id = ? AND followed_id = ?",
            arrayOf(followerId.toString(), followedId.toString()),
            null, null, null
        ).use { cursor -> cursor.count > 0 }
    }

    // Quantas pessoas seguem o usuário (pro contador "X Seguidores" do perfil).
    fun countFollowers(userId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "follows", arrayOf("follower_id"), "followed_id = ?",
            arrayOf(userId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }

    // Quantas pessoas o usuário segue (pro contador "X Seguindo" do perfil).
    fun countFollowing(userId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "follows", arrayOf("followed_id"), "follower_id = ?",
            arrayOf(userId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }

    // Lista quem segue o usuário (os "seguidores").
    fun listFollowers(userId: Long): List<User> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT users.id, users.username, users.email
            FROM follows
            JOIN users ON users.id = follows.follower_id
            WHERE follows.followed_id = ?
            ORDER BY users.username COLLATE NOCASE
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(User(cursor.getLong(0), cursor.getString(1), cursor.getString(2)))
                }
            }
        }
    }

    // Lista quem o usuário está seguindo.
    fun listFollowing(userId: Long): List<User> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT users.id, users.username, users.email
            FROM follows
            JOIN users ON users.id = follows.followed_id
            WHERE follows.follower_id = ?
            ORDER BY users.username COLLATE NOCASE
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(User(cursor.getLong(0), cursor.getString(1), cursor.getString(2)))
                }
            }
        }
    }
}
