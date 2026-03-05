package me.miki.shindo.management.event

class Priority {
    companion object {
        const val FIRST: Byte = 0
        const val SECOND: Byte = 1
        const val THIRD: Byte = 2
        const val FOURTH: Byte = 3
        const val FIFTH: Byte = 4

        @JvmField
        val VALUE_ARRAY: ByteArray = byteArrayOf(0, 1, 2, 3, 4)
    }
}
