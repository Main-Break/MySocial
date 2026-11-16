package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Endereco
import com.mysocial.data.local.AppDatabase

internal class EnderecoDAO(context: Context) {
    private val appContext = context.applicationContext

    private val colunas = arrayOf(
        "id", "user_id", "apelido", "cep", "rua", "numero", "bairro", "cidade", "estado"
    )

    private fun fromCursor(cursor: android.database.Cursor) = Endereco(
        id = cursor.getLong(0),
        userId = cursor.getLong(1),
        apelido = cursor.getString(2),
        cep = cursor.getString(3),
        rua = cursor.getString(4),
        numero = cursor.getString(5),
        bairro = cursor.getString(6),
        cidade = cursor.getString(7),
        estado = cursor.getString(8)
    )

    fun create(
        userId: Long, apelido: String, cep: String, rua: String,
        numero: String, bairro: String, cidade: String, estado: String
    ) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("user_id", userId)
                put("apelido", apelido)
                put("cep", cep)
                put("rua", rua)
                put("numero", numero)
                put("bairro", bairro)
                put("cidade", cidade)
                put("estado", estado)
            }
            db.writableDatabase.insertOrThrow("enderecos", null, values)
        }
    }

    fun listByUser(userId: Long): List<Endereco> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "enderecos", colunas,
            "user_id = ?", arrayOf(userId.toString()),
            null, null, "id ASC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(fromCursor(cursor))
            }
        }
    }

    fun update(
        id: Long, currentUserId: Long, apelido: String, cep: String, rua: String,
        numero: String, bairro: String, cidade: String, estado: String
    ) {
        AppDatabase(appContext).use { db ->
            val values = ContentValues().apply {
                put("apelido", apelido)
                put("cep", cep)
                put("rua", rua)
                put("numero", numero)
                put("bairro", bairro)
                put("cidade", cidade)
                put("estado", estado)
            }
            val updated = db.writableDatabase.update(
                "enderecos", values, "id = ? AND user_id = ?",
                arrayOf(id.toString(), currentUserId.toString())
            )
            require(updated == 1) { "Endereço não encontrado, atualize a lista" }
        }
    }

    fun delete(id: Long, currentUserId: Long) {
        AppDatabase(appContext).use { db ->
            val deleted = db.writableDatabase.delete(
                "enderecos", "id = ? AND user_id = ?",
                arrayOf(id.toString(), currentUserId.toString())
            )
            require(deleted == 1) { "Endereço não encontrado, atualize a lista" }
        }
    }
}
