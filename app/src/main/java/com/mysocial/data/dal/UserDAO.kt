package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.User
import com.mysocial.data.local.AppDatabase

internal class StoredPassword(val hash: ByteArray, val salt: ByteArray, val userId: Long)

internal class UserDAO(context: Context) {
    private val appContext = context.applicationContext

    fun register(username: String, email: String, password: ByteArray, salt: ByteArray, coverColor: String) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("username", username)
                put("email", email)
                put("password", password)
                put("salt", salt)
                put("cover_color", coverColor)
            }
            db.writableDatabase.insertOrThrow("users", null, values)
        }
    }

    fun findPassword(identifier: String): StoredPassword? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "users",
            arrayOf("password", "salt", "id"),
            "email = ? OR username = ? COLLATE NOCASE",
            arrayOf(identifier, identifier),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) StoredPassword(
                cursor.getBlob(0),
                cursor.getBlob(1),
                cursor.getLong(2)
            )
            else null
        }
    }

    private val userColumns = arrayOf(
        "id", "username", "email", "cover_color",
        "bio", "cep", "rua", "numero", "bairro", "cidade", "estado"
    )

    private fun userFromCursor(cursor: android.database.Cursor) = User(
        id = cursor.getLong(0),
        username = cursor.getString(1),
        email = cursor.getString(2),
        coverColor = cursor.getString(3),
        bio = cursor.getString(4),
        cep = cursor.getString(5),
        rua = cursor.getString(6),
        numero = cursor.getString(7),
        bairro = cursor.getString(8),
        cidade = cursor.getString(9),
        estado = cursor.getString(10)
    )

    fun list(): List<User> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "users", userColumns,
            null, null, null, null,
            "username COLLATE NOCASE, id"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(userFromCursor(cursor))
            }
        }
    }

    fun findById(id: Long): User? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "users", userColumns,
            "id = ?", arrayOf(id.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) userFromCursor(cursor) else null
        }
    }

    fun update(id: Long, username: String, password: ByteArray? = null, salt: ByteArray? = null) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("username", username)
                if (password != null) put("password", password)
                if (salt != null) put("salt", salt)
            }
            val updated = db.writableDatabase.update(
                "users", values, "id = ?", arrayOf(id.toString())
            )
            require(updated == 1) { "Usuário não encontrado, atualize a lista" }
        }
    }

    fun updateProfile(
        id: Long,
        bio: String,
        cep: String,
        rua: String,
        numero: String,
        bairro: String,
        cidade: String,
        estado: String
    ) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("bio", bio)
                put("cep", cep)
                put("rua", rua)
                put("numero", numero)
                put("bairro", bairro)
                put("cidade", cidade)
                put("estado", estado)
            }
            val updated = db.writableDatabase.update(
                "users", values, "id = ?", arrayOf(id.toString())
            )
            require(updated == 1) { "Usuário não encontrado, atualize a lista" }
        }
    }

    fun delete(id: Long, currentUserId: Long) {
        require(currentUserId > 0) { "Faça login para continuar" }
        require(id != currentUserId) { "Você não pode excluir o seu usuário" }
        AppDatabase(appContext).use { db ->
            val deleted = db.writableDatabase.delete(
                "users", "id = ? AND id <> ?",
                arrayOf(id.toString(), currentUserId.toString())
            )
            require(deleted == 1) { "Usuário não encontrado, atualize a lista" }
        }
    }
}
