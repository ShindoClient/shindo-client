package me.miki.shindo.utils

import java.util.TreeMap
import kotlin.math.abs

object RomanNumeralUtil {

    private val map: TreeMap<Int, String> = TreeMap()
    private val cache: MutableMap<Int, String> = HashMap()

    init {
        map[1000] = "M"
        map[900] = "CM"
        map[500] = "D"
        map[400] = "CD"
        map[100] = "C"
        map[90] = "XC"
        map[50] = "L"
        map[40] = "XL"
        map[10] = "X"
        map[9] = "IX"
        map[5] = "V"
        map[4] = "IV"
        map[1] = "I"
    }

    @JvmStatic
    fun toRoman(number: Int): String {
        return cache.getOrPut(number) { toRomanUncached(number) }
    }

    private fun toRomanUncached(number: Int): String {
        if (number == 0) return "0"
        if (number < 0) return "-" + toRomanUncached(abs(number))
        val floor = map.floorKey(number)
        return if (number == floor) {
            map[number]!!
        } else {
            map[floor]!! + toRomanUncached(number - floor)
        }
    }
}
