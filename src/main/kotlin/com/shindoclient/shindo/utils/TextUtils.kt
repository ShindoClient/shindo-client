package com.shindoclient.shindo.utils

import java.text.Normalizer

object TextUtils {
    @JvmStatic
    fun stripUnicodeAccents(text: String): String {
        if (text.isEmpty()) return text
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        return nfd.replace("\\p{M}".toRegex(), "")
    }
}
