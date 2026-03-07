package me.miki.shindo.types

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

class Rect @JvmOverloads constructor(x: Float = 0f, y: Float = 0f, width: Float = 0f, height: Float = 0f) {

    @JvmField
    var x = 0f
    @JvmField
    var y = 0f
    @JvmField
    var width = 0f
    @JvmField
    var height = 0f
    operator fun set(x: Float, y: Float, w: Float, h: Float): Rect {
        this.x = x
        this.y = y
        this.width = w
        this.height = h
        return this
    }

    fun set(r: Rect): Rect {
        this.x = r.x
        this.y = r.y
        this.width = r.width
        this.height = r.height
        return this
    }

    fun setPosition(x: Float, y: Float): Rect {
        this.x = x
        this.y = y
        return this
    }

    fun setSize(w: Float, h: Float): Rect {
        this.width = w
        this.height = h
        return this
    }

    fun translate(dx: Float, dy: Float): Rect {
        this.x += dx
        this.y += dy
        return this
    }

    fun inflate(dw: Float, dh: Float): Rect {
        this.width += dw
        this.height += dh
        return this
    }

    fun contains(px: Float, py: Float): Boolean {
        return px >= x && py >= y && px < x + width && py < y + height
    }

    fun intersects(r: Rect): Boolean {
        return x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y
    }

    fun intersection(r: Rect, dest: Rect): Boolean {
        val nx = max(x, r.x)
        val ny = max(y, r.y)
        val nx2 = min(x + width, r.x + r.width)
        val ny2 = min(y + height, r.y + r.height)
        val nw = nx2 - nx
        val nh = ny2 - ny
        if (nw > 0f && nh > 0f) {
            dest.x = nx
            dest.y = ny
            dest.width = nw
            dest.height = nh
            return true
        }
        dest.x = nx
        dest.y = ny
        dest.width = 0f
        dest.height = 0f
        return false
    }

    fun expandToInclude(px: Float, py: Float): Rect {
        if (width <= 0f || height <= 0f) {
            x = px
            y = py
            width = 0f
            height = 0f
            return this
        }
        val minX = min(x, px)
        val minY = min(y, py)
        val maxX = max(x + width, px)
        val maxY = max(y + height, py)
        x = minX
        y = minY
        width = maxX - minX
        height = maxY - minY
        return this
    }

    val isEmpty: Boolean
        get() = width <= 0f || height <= 0f

    fun copyTo(dest: Rect) {
        dest.x = x
        dest.y = y
        dest.width = width
        dest.height = height
    }

    override fun toString(): String {
        return "Rect[x=$x, y=$y, w=$width, h=$height]"
    }

    companion object {
        fun cover(src: Size?, target: Rect?, dest: Rect?): Rect {
            require(!(src == null || target == null || dest == null)) { "src, target and dest must be non-null" }
            val sw = src.width
            val sh = src.height
            if (sw <= 0f || sh <= 0f) {
                dest.x = target.x
                dest.y = target.y
                dest.width = 0f
                dest.height = 0f
                return dest
            }
            val scaleX = target.width / sw
            val scaleY = target.height / sh
            val scale = max(scaleX, scaleY)
            val rw = sw * scale
            val rh = sh * scale
            val rx = target.x + (target.width - rw) * 0.5f
            val ry = target.y + (target.height - rh) * 0.5f
            dest.x = rx
            dest.y = ry
            dest.width = rw
            dest.height = rh
            return dest
        }
    }

    init {
        set(x, y, width, height)
    }
}