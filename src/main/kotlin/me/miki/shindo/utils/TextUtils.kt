package me.miki.shindo.utils

import java.text.Normalizer

/**
 * Remove caracteres unicode acentuados para evitar bugs no render de fontes.
 * Ex: "Descrição" -> "Descricao", "áéíóú" -> "aeiou"
 */
object TextUtils {
    @JvmStatic
    fun stripUnicodeAccents(text: String): String {
        if (text.isEmpty()) return text
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        return nfd.replace("\\p{M}".toRegex(), "")
    }
}
