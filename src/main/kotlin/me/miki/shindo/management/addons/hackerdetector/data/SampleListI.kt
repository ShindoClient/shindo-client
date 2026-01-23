package me.miki.shindo.management.addons.hackerdetector.data

/**
 * Lista circular otimizada para armazenar amostras de valores int
 */
class SampleListI(private val capacity: Int) {
    
    init {
        require(capacity >= 2) { "Size must be at least 2" }
    }
    
    private val data = IntArray(capacity)
    private var size = 0
    private var latestIndex = -1
    
    fun add(value: Int) {
        latestIndex = (latestIndex + 1) % capacity
        data[latestIndex] = value
        if (size < capacity) size++
    }
    
    fun get(index: Int): Int {
        require(index in 0..size) { "Index out of bounds: $index" }
        val i = latestIndex - index
        return data[if (i < 0) i + capacity else i]
    }
    
    fun clear() {
        size = 0
        latestIndex = -1
    }
    
    fun size(): Int = size
    fun capacity(): Int = capacity
    fun hasCollected(): Boolean = size == capacity
    
    fun sum(): Int {
        var s = 0
        for (i in 0 until size) {
            s += get(i)
        }
        return s
    }
    
    fun average(): Float = sum() / size.toFloat()
    
    fun isSameValues(): Boolean {
        if (size < 2) return false
        val v = get(0)
        for (i in 1 until size) {
            if (v != get(i)) return false
        }
        return true
    }
    
    override fun toString(): String {
        if (size == 0) return "[]"
        return buildString {
            append('[')
            for (i in 0 until size) {
                append(get(i))
                if (i < size - 1) append(", ")
            }
            append(']')
        }
    }
}
