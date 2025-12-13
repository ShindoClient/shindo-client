package me.miki.shindo.utils.vector

class Vector2(var x: Float, var y: Float) {

    fun clone(): Vector2 = Vector2(x, y)

    fun copy(vec: Vector2) {
        this.x = vec.x
        this.y = vec.y
    }

    fun add(vec: Vector2): Vector2 {
        this.x += vec.x
        this.y += vec.y
        return this
    }

    fun subtract(vec: Vector2): Vector2 {
        this.x -= vec.x
        this.y -= vec.y
        return this
    }

    fun div(amount: Float): Vector2 {
        this.x /= amount
        this.y /= amount
        return this
    }

    fun mul(amount: Float): Vector2 {
        this.x *= amount
        this.y *= amount
        return this
    }

    fun normalize(): Vector2 {
        val f = kotlin.math.sqrt(this.x * this.x + this.y * this.y)
        if (f < 1.0e-4f) {
            this.x = 0f
            this.y = 0f
        } else {
            this.x /= f
            this.y /= f
        }
        return this
    }

    override fun toString(): String {
        return "Vector2 [x=$x, y=$y]"
    }
}
