package me.miki.shindo.utils.vector

/**
 * Lightweight vertex container used by the 3D skin renderer.
 */
class Vertex(val pos: Vector3f, val u: Float, val v: Float) {

    constructor(x: Float, y: Float, z: Float, u: Float, v: Float) : this(Vector3f(x, y, z), u, v)

    /**
     * Returns a copy with the same position and new UVs.
     */
    fun setTexturePosition(u: Float, v: Float): Vertex {
        return Vertex(Vector3f(pos.x, pos.y, pos.z), u, v)
    }
}
