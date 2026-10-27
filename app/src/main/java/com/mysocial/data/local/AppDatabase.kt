package com.mysocial.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

// Classe que cria e abre o banco SQLite do app. É aberta/fechada toda vez que um DAO
// (UserDAO, PostDAO) precisa ler ou gravar algo, por isso ela fica bem enxuta.
internal class AppDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    // noBackupFilesDir evita que o Android inclua o banco em backup automático na nuvem,
    // já que ele guarda senha (mesmo em hash) e dado de usuário.
    File(context.noBackupFilesDir, "mysocial.db").absolutePath,
    null,
    2 // versão do schema, usada pelo onUpgrade logo abaixo
) {
    // Roda só uma vez, na primeira vez que o app abre e o banco ainda não existe.
    // Aqui fica o schema completo e atual do banco (sempre consolidado, nunca espalhado
    // em vários arquivos de migração separados).
    override fun onCreate(db: SQLiteDatabase) {
        // Tabela de usuários cadastrados. A senha nunca é salva em texto puro: "password"
        // guarda o hash (gerado pelo AuthManager) e "salt" o valor aleatório usado nesse hash.
        db.execSQL("""
            CREATE TABLE users(
            id INTEGER PRIMARY KEY,
            username TEXT NOT NULL UNIQUE CHECK(length(username) BETWEEN 3 AND 30),
            email TEXT NOT NULL UNIQUE CHECK(length(email) BETWEEN 3 AND 254),
            password BLOB NOT NULL,
            salt BLOB NOT NULL
            )
        """.trimIndent())

        // Tabela de posts. "user_id" é o autor do post (referencia a tabela users).
        // O CHECK de tamanho do conteúdo é o mesmo limite de 280 caracteres do Twitter/X.
        db.execSQL("""
            CREATE TABLE posts(
            id INTEGER PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES users(id),
            content TEXT NOT NULL CHECK(length(content) BETWEEN 1 AND 280),
            created_at TEXT NOT NULL
            )
        """.trimIndent())

        // Tabela de quem segue quem. "follower_id" é quem segue, "followed_id" é quem é
        // seguido. A chave primária composta impede seguir a mesma pessoa duas vezes.
        db.execSQL("""
            CREATE TABLE follows(
            follower_id INTEGER NOT NULL REFERENCES users(id),
            followed_id INTEGER NOT NULL REFERENCES users(id),
            PRIMARY KEY (follower_id, followed_id)
            )
        """.trimIndent())
    }

    // Roda sozinho, automaticamente, sempre que o app abre e percebe que a versão do banco
    // no celular do usuário é mais antiga que a versão declarada no construtor acima.
    // Não existe (e não pode existir) nenhum passo manual no servidor/celular: é tudo aqui.
    // Quando o schema mudar de verdade (ex: adicionar uma coluna nova), sobe o número da
    // versão lá em cima e adiciona um "if (oldVersion < X) { db.execSQL(...) }" aqui,
    // sem apagar os ifs de versões anteriores, pra quem estiver em qualquer versão antiga
    // conseguir migrar sozinho, sem perder dado e sem precisar de ninguém rodar nada à mão.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Quem já tinha o banco na versão 1 (sem a tabela follows) ganha ela aqui,
        // sem perder os dados de users/posts que já existiam.
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE follows(
                follower_id INTEGER NOT NULL REFERENCES users(id),
                followed_id INTEGER NOT NULL REFERENCES users(id),
                PRIMARY KEY (follower_id, followed_id)
                )
            """.trimIndent())
        }
    }
}
