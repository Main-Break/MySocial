package com.mysocial.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

internal class AppDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    File(context.noBackupFilesDir, "mysocial.db").absolutePath,
    null,
    4
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE users(
            id INTEGER PRIMARY KEY,
            username TEXT NOT NULL UNIQUE CHECK(length(username) BETWEEN 3 AND 30),
            email TEXT NOT NULL UNIQUE CHECK(length(email) BETWEEN 3 AND 254),
            password BLOB NOT NULL,
            salt BLOB NOT NULL,
            cover_color TEXT NOT NULL DEFAULT '#1D9BF0',
            bio TEXT NOT NULL DEFAULT '' CHECK(length(bio) <= 160),
            cep TEXT NOT NULL DEFAULT '',
            rua TEXT NOT NULL DEFAULT '',
            numero TEXT NOT NULL DEFAULT '',
            bairro TEXT NOT NULL DEFAULT '',
            cidade TEXT NOT NULL DEFAULT '',
            estado TEXT NOT NULL DEFAULT ''
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE posts(
            id INTEGER PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES users(id),
            content TEXT NOT NULL CHECK(length(content) BETWEEN 1 AND 280),
            created_at TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE follows(
            follower_id INTEGER NOT NULL REFERENCES users(id),
            followed_id INTEGER NOT NULL REFERENCES users(id),
            PRIMARY KEY (follower_id, followed_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE likes(
            user_id INTEGER NOT NULL REFERENCES users(id),
            post_id INTEGER NOT NULL REFERENCES posts(id),
            PRIMARY KEY (user_id, post_id)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE comments(
            id INTEGER PRIMARY KEY,
            post_id INTEGER NOT NULL REFERENCES posts(id),
            user_id INTEGER NOT NULL REFERENCES users(id),
            content TEXT NOT NULL CHECK(length(content) BETWEEN 1 AND 280),
            created_at TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE follows(
                follower_id INTEGER NOT NULL REFERENCES users(id),
                followed_id INTEGER NOT NULL REFERENCES users(id),
                PRIMARY KEY (follower_id, followed_id)
                )
            """.trimIndent())
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE users ADD COLUMN cover_color TEXT NOT NULL DEFAULT '#1D9BF0'")
            db.execSQL("""
                CREATE TABLE likes(
                user_id INTEGER NOT NULL REFERENCES users(id),
                post_id INTEGER NOT NULL REFERENCES posts(id),
                PRIMARY KEY (user_id, post_id)
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE comments(
                id INTEGER PRIMARY KEY,
                post_id INTEGER NOT NULL REFERENCES posts(id),
                user_id INTEGER NOT NULL REFERENCES users(id),
                content TEXT NOT NULL CHECK(length(content) BETWEEN 1 AND 280),
                created_at TEXT NOT NULL
                )
            """.trimIndent())
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE users ADD COLUMN bio TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN cep TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN rua TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN numero TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN bairro TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN cidade TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE users ADD COLUMN estado TEXT NOT NULL DEFAULT ''")
        }
    }
}
