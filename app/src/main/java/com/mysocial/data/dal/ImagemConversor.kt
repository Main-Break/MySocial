package com.mysocial.data.dal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object ImagemConversor {
    private const val LADO_MAXIMO = 1024
    private const val QUALIDADE_JPEG = 80

    fun paraBlob(imagem: Bitmap): ByteArray {
        val reduzida = reduzir(imagem)
        return ByteArrayOutputStream().use { saida ->
            check(reduzida.compress(Bitmap.CompressFormat.JPEG, QUALIDADE_JPEG, saida)) {
                "Não foi possível salvar a imagem."
            }
            saida.toByteArray()
        }
    }

    suspend fun paraBlob(contexto: Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = contexto.contentResolver

        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }

        var amostra = 1
        while (medidas.outWidth / amostra > LADO_MAXIMO * 2 ||
            medidas.outHeight / amostra > LADO_MAXIMO * 2
        ) amostra *= 2

        val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
        val bitmap = checkNotNull(
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes) }
        ) { "Não foi possível abrir a imagem" }

        paraBlob(bitmap)
    }

    private fun reduzir(imagem: Bitmap): Bitmap {
        val maior = maxOf(imagem.width, imagem.height)
        if (maior <= LADO_MAXIMO) return imagem
        val escala = LADO_MAXIMO.toFloat() / maior
        return imagem.scale(
            (imagem.width * escala).toInt(),
            (imagem.height * escala).toInt(),
            true
        )
    }
}
