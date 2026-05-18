package me.miki.shindo.utils

import io.netty.util.internal.ThreadLocalRandom

object RandomUtils {
    @JvmStatic
    fun getRandomInt(
        min: Int,
        max: Int,
    ): Int = ThreadLocalRandom.current().nextInt(min, max + 1)

    @JvmStatic
    fun getRandomLong(
        min: Int,
        max: Int,
    ): Long = ThreadLocalRandom.current().nextLong(min.toLong(), (max + 1).toLong())
}
