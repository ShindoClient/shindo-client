package me.miki.shindo.utils

import org.apache.commons.lang3.StringUtils
import java.util.Locale

object SearchUtils {

    @JvmStatic
    fun isSimilar(s1: String, s2: String): Boolean = isSimilar(s1, s2, 1)

    @JvmStatic
    fun isSimilar(s1: String, s2: String, searchDistance: Int): Boolean {
        val left = s1.toLowerCase(Locale.ENGLISH)
        val right = s2.toLowerCase(Locale.ENGLISH)

        if (left.length <= searchDistance) {
            return left.contains(right)
        }

        for (token in StringUtils.split(left)) {
            if (token.contains(right) || StringUtils.getLevenshteinDistance(token, right) <= searchDistance) {
                return true
            }
        }

        return left.contains(right) || StringUtils.getLevenshteinDistance(left, right) <= searchDistance
    }
}
