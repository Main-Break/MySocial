package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.User
import com.mysocial.data.local.AppDatabase

// Carrega só o que é necessário pra conferir a senha no login: o hash salvo, o salt usado
// pra gerar esse hash, e o id do usuário dono dessa senha.
internal class StoredPassword(val hash: ByteArray, val salt: ByteArray, val userId: Long)

// DAO = Data Access Object. É a única classe que conversa diretamente com a tabela
// "users" do banco. Toda query usa "?" no lugar do valor (nunca concatena texto do
// usuário na query), o que evita SQL Injection.
internal class UserDAO(context: Context) {
    private val appContext = context.applicationContext

    // Cria um usuário novo. A senha que chega aqui já deve vir como hash (NUNCA texto puro),
    // é responsabilidade de quem chama (o AuthViewModel) gerar esse hash antes. "coverColor"
    // é escolhido uma única vez aqui no cadastro (ver AuthManager.pickCoverColor) e fica
    // fixo pro usuário a partir daí.
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

    // Busca o hash da senha e o salt de um usuário pelo e-mail OU pelo username (o login
    // aceita os dois, igual ao X). "COLLATE NOCASE" no username deixa a busca sem
    // diferenciar maiúscula/minúscula. Devolve null se não existir ninguém com esse dado.
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

    // Colunas usadas pra montar um User completo (ver userFromCursor logo abaixo).
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

    // Lista todos os usuários cadastrados, ordenado por nome de usuário.
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

    // Busca um usuário específico pelo id.
    fun findById(id: Long): User? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "users", userColumns,
            "id = ?", arrayOf(id.toString()),
            null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) userFromCursor(cursor) else null
        }
    }

    // Atualiza o username e, se informado, a senha (hash + salt novos) de um usuário.
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

    // Atualiza a bio e o endereço do perfil (dados "extras", separados do username/senha
    // acima). Só o próprio usuário logado edita o próprio perfil, conferido pelo
    // ProfileViewModel antes de chamar isso.
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

    // Exclui um usuário. Nunca deixa o usuário logado excluir a própria conta por aqui,
    // como forma simples de evitar o usuário ficar "órfão" no meio de uma sessão ativa.
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
