package com.shindoclient.shindo.utils.vector

class Vertex(
    val pos: Vector3f,
    val u: Float,
    val v: Float,
) {
    constructor(x: Float, y: Float, z: Float, u: Float, v: Float) : this(Vector3f(x, y, z), u, v)

    fun setTexturePosition(
        u: Float,
        v: Float,
    ): Vertex = Vertex(Vector3f(pos.x, pos.y, pos.z), u, v)
}
