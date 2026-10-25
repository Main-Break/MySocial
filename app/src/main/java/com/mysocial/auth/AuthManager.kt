package com.mysocial.auth

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Cuida só de uma coisa: transformar a senha digitada num hash seguro pra guardar no banco,
// e depois conferir se uma senha digitada bate com o hash guardado. A senha em texto puro
// nunca é salva em lugar nenhum.
object AuthManager {
    // Quanto maior esse número, mais lento (e mais seguro contra força bruta) fica gerar
    // o hash. 60 mil é um valor comum/recomendado pra esse algoritmo.
    private const val ITERATIONS = 60_000

    // Gera um valor aleatório novo (salt) pra cada usuário. Isso impede que duas pessoas
    // com a mesma senha acabem com o mesmo hash salvo no banco.
    fun createSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    // Transforma a senha (texto) + salt num hash de 256 bits usando PBKDF2, um algoritmo
    // feito pra isso (ao contrário de algo tipo MD5/SHA simples, que não servem pra senha).
    fun generateHash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
        } finally {
            // Limpa a senha da memória assim que não precisa mais dela.
            spec.clearPassword()
        }
    }

    // Confere se a senha digitada no login gera o mesmo hash que está salvo no banco.
    // Usa MessageDigest.isEqual (em vez de comparar byte a byte na mão) porque essa função
    // evita vazar, pelo tempo de execução, quantos bytes já bateram, dificultando ataques.
    fun validatePassword(password: String, salt: ByteArray, storedHash: ByteArray): Boolean {
        val computed = generateHash(password, salt)
        return try {
            MessageDigest.isEqual(storedHash, computed)
        } finally {
            computed.fill(0)
        }
    }
}
