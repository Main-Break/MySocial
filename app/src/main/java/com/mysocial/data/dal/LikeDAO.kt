package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mysocial.data.local.AppDatabase

// DAO (Data Access Object) da tabela "likes". Mesmo padrão dos outros DAOs: só essa
// classe conversa com a tabela, e toda query usa "?" pra evitar SQL Injection.
internal class LikeDAO(context: Context) {
    private val appContext = context.applicationContext

    // Curte um post. Usa CONFLICT_IGNORE porque a chave primária composta (user_id,
    // post_id) já impede duplicar: clicar duas vezes rápido só não faz nada na segunda vez.
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

    // Descurte um post.
    fun unlike(userId: Long, postId: Long) {
        AppDatabase(appContext).use { db ->
            db.writableDatabase.delete(
                "likes", "user_id = ? AND post_id = ?",
                arrayOf(userId.toString(), postId.toString())
            )
        }
    }

    // Confere se o usuário já curtiu esse post, pra decidir se o coração aparece
    // cheio ou vazio.
    fun isLiked(userId: Long, postId: Long): Boolean = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "likes", arrayOf("user_id"),
            "user_id = ? AND post_id = ?",
            arrayOf(userId.toString(), postId.toString()),
            null, null, null
        ).use { cursor -> cursor.count > 0 }
    }

    // Quantas curtidas um post tem, pro contador ao lado do coração.
    fun countLikes(postId: Long): Int = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "likes", arrayOf("user_id"), "post_id = ?",
            arrayOf(postId.toString()), null, null, null
        ).use { cursor -> cursor.count }
    }
}
