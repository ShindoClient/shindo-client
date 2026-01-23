package me.miki.shindo.management.addons.hackerdetector.data

/**
 * Lista circular otimizada para armazenar amostras de valores boolean
 */
class SampleListZ(private val capacity: Int) {
    
    init {
        require(capacity >= 2) { "Size must be at least 2" }
    }
    
    private val data = BooleanArray(capacity)
    private var size = 0
    private var latestIndex = -1
    
    fun add(value: Boolean) {
        latestIndex = (latestIndex + 1) % capacity
        data[latestIndex] = value
        if (size < capacity) size++
    }
    
    fun get(index: Int): Boolean {
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
            if (get(i)) s++
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
        val sb = StringBuilder()
        sb.append('[')
        for (i in 0 until size) {
            sb.append(if (get(i)) '1' else '0')
            if (i < size - 1) sb.append(", ")
        }
        sb.append(']')
        return sb.toString()
    }
}
