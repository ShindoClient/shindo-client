package me.miki.shindo.types

class Size @JvmOverloads constructor(width: Float = 0f, height: Float = 0f) {
    var width = 0f
    var height = 0f
    operator fun set(w: Float, h: Float): Size {
        width = w
        height = h
        return this
    }

    fun set(s: Size): Size {
        width = s.width
        height = s.height
        return this
    }

    fun aspect(): Float {
        return if (height == 0f) 0f else width / height
    }

    fun scale(factor: Float): Size {
        width *= factor
        height *= factor
        return this
    }

    override fun toString(): String {
        return "Size[w=$width, h=$height]"
    }

    init {
        set(width, height)
    }
}