package me.miki.shindo.utils.vector

class Polygon(
    vertices: Array<Vertex>,
    u1: Float,
    v1: Float,
    u2: Float,
    v2: Float,
    textureWidth: Float,
    textureHeight: Float,
    mirror: Boolean,
    @Suppress("UNUSED_PARAMETER") ignoredDirection: Any?,
) {
    val vertices: Array<Vertex>
    val normal: Vector3f

    init {
        val minU = u1 / textureWidth
        val minV = v1 / textureHeight
        val maxU = u2 / textureWidth
        val maxV = v2 / textureHeight

        val arranged =
            arrayOf(
                vertices[0].setTexturePosition(maxU, minV),
                vertices[1].setTexturePosition(minU, minV),
                vertices[2].setTexturePosition(minU, maxV),
                vertices[3].setTexturePosition(maxU, maxV),
            )

        if (mirror) {
            arranged.reverse()
        }

        this.vertices = arranged
        this.normal = computeNormal(arranged)
    }

    private fun computeNormal(verts: Array<Vertex>): Vector3f {
        val v0 =
            Vector3f(verts[1].pos.x, verts[1].pos.y, verts[1].pos.z).apply {
                sub(Vector3f(verts[0].pos.x, verts[0].pos.y, verts[0].pos.z))
            }
        val v1 =
            Vector3f(verts[1].pos.x, verts[1].pos.y, verts[1].pos.z).apply {
                sub(Vector3f(verts[2].pos.x, verts[2].pos.y, verts[2].pos.z))
            }

        val nx = v1.y() * v0.z() - v1.z() * v0.y()
        val ny = v1.z() * v0.x() - v1.x() * v0.z()
        val nz = v1.x() * v0.y() - v1.y() * v0.x()

        return Vector3f(nx, ny, nz).also { it.normalize() }
    }
}
