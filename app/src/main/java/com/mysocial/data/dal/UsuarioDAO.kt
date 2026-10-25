package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Usuario
import com.mysocial.data.local.AppDatabase

// Carrega só o que é necessário pra conferir a senha no login: o hash salvo, o salt usado
// pra gerar esse hash, e o id do usuário dono dessa senha.
internal class SenhaSalva(val hash: ByteArray, val salt: ByteArray, val usuarioId: Long)

// DAO = Data Access Object. É a única classe que conversa diretamente com a tabela
// "usuarios" do banco. Toda query usa "?" no lugar do valor (nunca concatena texto do
// usuário na query), o que evita SQL Injection.
internal class UsuarioDAO(context: Context) {
    private val appContext = context.applicationContext

    // Cria um usuário novo. A senha que chega aqui já deve vir como hash (NUNCA texto puro),
    // é responsabilidade de quem chama (o AuthViewModel) gerar esse hash antes.
    fun cadastrar(username: String, email: String, senha: ByteArray, salt: ByteArray) {
        AppDatabase(appContext).use { db ->
            val dados = ContentValues().apply {
                put("username", username)
                put("email", email)
                put("senha", senha)
                put("salt", salt)
            }
            db.writableDatabase.insertOrThrow("usuarios", null, dados)
        }
    }

    // Busca o hash da senha e o salt de um usuário pelo e-mail, usado só na hora do login.
    // Devolve null se não existir ninguém com esse e-mail.
    fun buscarSenha(email: String): SenhaSalva? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "usuarios",
            arrayOf("senha", "salt", "id"),
            "email = ?",
            arrayOf(email),
            null, null, null
        ).use { usuario ->
            if (usuario.moveToFirst()) SenhaSalva(
                usuario.getBlob(0),
                usuario.getBlob(1),
                usuario.getLong(2)
            )
            else null
        }
    }

    // Lista todos os usuários cadastrados, ordenado por nome de usuário.
    fun listar(): List<Usuario> = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "usuarios", arrayOf("id", "username", "email"),
            null, null, null, null,
            "username COLLATE NOCASE, id"
        ).use { usuario ->
            buildList {
                while (usuario.moveToNext()) {
                    add(Usuario(usuario.getLong(0), usuario.getString(1), usuario.getString(2)))
                }
            }
        }
    }

    // Busca um usuário específico pelo id.
    fun buscarPorId(id: Long): Usuario? = AppDatabase(appContext).use { db ->
        db.readableDatabase.query(
            "usuarios", arrayOf("id", "username", "email"),
            "id = ?", arrayOf(id.toString()),
            null, null, null
        ).use { usuario ->
            if (usuario.moveToFirst()) Usuario(
                usuario.getLong(0),
                usuario.getString(1),
                usuario.getString(2)
            )
            else null
        }
    }

    // Atualiza o username e, se informado, a senha (hash + salt novos) de um usuário.
    fun alterar(id: Long, username: String, hash: ByteArray? = null, salt: ByteArray? = null) {
        AppDatabase(appContext).use { db ->
            val valores = ContentValues().apply {
                put("username", username)
                if (hash != null) put("senha", hash)
                if (salt != null) put("salt", salt)
            }
            val alterado = db.writableDatabase.update(
                "usuarios", valores, "id = ?", arrayOf(id.toString())
            )
            require(alterado == 1) { "Usuário não encontrado, atualize a lista" }
        }
    }

    // Exclui um usuário. Nunca deixa o usuário logado excluir a própria conta por aqui,
    // como forma simples de evitar o usuário ficar "órfão" no meio de uma sessão ativa.
    fun excluir(id: Long, usuarioAtualId: Long) {
        require(usuarioAtualId > 0) { "Faça login para continuar" }
        require(id != usuarioAtualId) { "Você não pode excluir o seu usuário" }
        AppDatabase(appContext).use { db ->
            val excluido = db.writableDatabase.delete(
                "usuarios", "id = ? AND id <> ?",
                arrayOf(id.toString(), usuarioAtualId.toString())
            )
            require(excluido == 1) { "Usuário não encontrado, atualize a lista" }
        }
    }
}
