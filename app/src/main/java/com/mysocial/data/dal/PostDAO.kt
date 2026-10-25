package com.mysocial.data.dal

import android.content.ContentValues
import android.content.Context
import com.mysocial.data.Post
import com.mysocial.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DAO (Data Access Object) da tabela "posts". Mesma ideia do UsuarioDAO: só essa classe
// conversa com a tabela, e toda query usa "?" pra evitar SQL Injection.
internal class PostDAO(context: Context) {
    private val appContext = context.applicationContext

    // Cria um post novo pro usuário informado. A validação de tamanho (limite de 280
    // caracteres) já aconteceu antes, lá no PostViewModel/PostValidator; aqui só grava.
    fun criar(usuarioId: Long, conteudo: String) {
        AppDatabase(appContext).use { db ->
            val dataCriacao = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).format(Date())
            val dados = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("conteudo", conteudo)
                put("data_criacao", dataCriacao)
            }
            db.writableDatabase.insertOrThrow("posts", null, dados)
        }
    }

    // Lista todos os posts pro feed, já trazendo o username de quem postou (JOIN com a
    // tabela usuarios), do mais recente pro mais antigo.
    fun listar(): List<Post> = AppDatabase(appContext).use { db ->
        db.readableDatabase.rawQuery(
            """
            SELECT posts.id, posts.usuario_id, usuarios.username, posts.conteudo, posts.data_criacao
            FROM posts
            JOIN usuarios ON usuarios.id = posts.usuario_id
            ORDER BY posts.data_criacao DESC, posts.id DESC
            """.trimIndent(),
            null
        ).use { resultado ->
            buildList {
                while (resultado.moveToNext()) {
                    add(
                        Post(
                            id = resultado.getLong(0),
                            usuarioId = resultado.getLong(1),
                            autor = resultado.getString(2),
                            conteudo = resultado.getString(3),
                            dataCriacao = resultado.getString(4)
                        )
                    )
                }
            }
        }
    }

    // Exclui um post. O "usuario_id = ?" na cláusula garante que só o dono do post
    // consegue excluí-lo, mesmo que o id do post seja adivinhado/forjado.
    fun excluir(id: Long, usuarioAtualId: Long) {
        require(usuarioAtualId > 0) { "Faça login para continuar" }
        AppDatabase(appContext).use { db ->
            val excluido = db.writableDatabase.delete(
                "posts", "id = ? AND usuario_id = ?",
                arrayOf(id.toString(), usuarioAtualId.toString())
            )
            require(excluido == 1) { "Post não encontrado, atualize a lista" }
        }
    }
}
