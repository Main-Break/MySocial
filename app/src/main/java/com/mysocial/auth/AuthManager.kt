package com.mysocial.auth

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object AuthManager {
    private const val ITERATIONS = 60_000

    fun createSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun generateHash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private val coverColorPalette = listOf(
        "#1D9BF0", "#7856FF", "#00BA7C", "#F91880", "#FFAD1F", "#F4212E"
    )

    fun pickCoverColor(username: String): String {
        val index = (username.hashCode().mod(coverColorPalette.size))
        return coverColorPalette[index]
    }

    fun validatePassword(password: String, salt: ByteArray, storedHash: ByteArray): Boolean {
        val computed = generateHash(password, salt)
        return try {
            MessageDigest.isEqual(storedHash, computed)
        } finally {
            computed.fill(0)
        }
    }
}
