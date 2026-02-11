package me.miki.shindo.management.mods.impl.skin3d.render

import me.miki.shindo.management.mods.impl.skin3d.SkinDirection
import me.miki.shindo.utils.vector.Polygon
import me.miki.shindo.utils.vector.Vertex
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats

class CustomizableCube(
    u: Int,
    v: Int,
    x: Float,
    y: Float,
    z: Float,
    sizeX: Float,
    sizeY: Float,
    sizeZ: Float,
    extraX: Float,
    extraY: Float,
    extraZ: Float,
    mirror: Boolean,
    textureWidth: Float,
    textureHeight: Float,
    hide: Array<SkinDirection?>?
) {
    val minX: Float
    val minY: Float
    val minZ: Float
    val maxX: Float
    val maxY: Float
    val maxZ: Float
    private val hidden: Array<SkinDirection?>
    private val polygons: Array<Polygon?>
    private var polygonCount = 0

    init {
        var x = x
        var y = y
        var z = z
        this.hidden = hide ?: emptyArray()
        this.minX = x
        this.minY = y
        this.minZ = z
        this.maxX = x + sizeX
        this.maxY = y + sizeY
        this.maxZ = z + sizeZ
        this.polygons = arrayOfNulls(6)

        var pX = x + sizeX
        var pY = y + sizeY
        var pZ = z + sizeZ

        x -= extraX
        y -= extraY
        z -= extraZ
        pX += extraX
        pY += extraY
        pZ += extraZ

        if (mirror) {
            val i = pX
            pX = x
            x = i
        }

        val vertex = Vertex(x, y, z, 0.0f, 0.0f)
        val vertex2 = Vertex(pX, y, z, 0.0f, 8.0f)
        val vertex3 = Vertex(pX, pY, z, 8.0f, 8.0f)
        val vertex4 = Vertex(x, pY, z, 8.0f, 0.0f)
        val vertex5 = Vertex(x, y, pZ, 0.0f, 0.0f)
        val vertex6 = Vertex(pX, y, pZ, 0.0f, 8.0f)
        val vertex7 = Vertex(pX, pY, pZ, 8.0f, 8.0f)
        val vertex8 = Vertex(x, pY, pZ, 8.0f, 0.0f)

        val l = u + sizeZ + sizeX
        val n = u + sizeZ + sizeX + sizeZ

        val q = v + sizeZ
        val r = v + sizeZ + sizeY

        if (visibleFace(SkinDirection.DOWN)) {
            this.polygons[polygonCount++] = Polygon(
                arrayOf<Vertex>(vertex6, vertex5, vertex, vertex2),
                l,
                q,
                n,
                r,
                textureWidth,
                textureHeight,
                mirror,
                SkinDirection.DOWN
            )
        }

        if (visibleFace(SkinDirection.UP)) {
            this.polygons[polygonCount++] = Polygon(
                arrayOf<Vertex>(vertex3, vertex4, vertex8, vertex7),
                l,
                q,
                n,
                r,
                textureWidth,
                textureHeight,
                mirror,
                SkinDirection.UP
            )
        }

        if (visibleFace(SkinDirection.WEST)) {
            this.polygons[polygonCount++] = Polygon(
                arrayOf<Vertex>(vertex, vertex5, vertex8, vertex4),
                l,
                q,
                n,
                r,
                textureWidth,
                textureHeight,
                mirror,
                SkinDirection.WEST
            )
        }

        if (visibleFace(SkinDirection.NORTH)) {
            this.polygons[polygonCount++] = Polygon(
                arrayOf<Vertex>(vertex2, vertex, vertex4, vertex3),
                l,
                q,
                n,
                r,
                textureWidth,
                textureHeight,
                mirror,
                SkinDirection.NORTH
            )
        }

        if (visibleFace(SkinDirection.EAST)) {
            this.polygons[polygonCount++] = Polygon(
                arrayOf<Vertex>(vertex6, vertex2, vertex3, vertex7),
                l,
                q,
                n,
                r,
                textureWidth,
                textureHeight,
                mirror,
                SkinDirection.EAST
            )
        }

        if (visibleFace(SkinDirection.SOUTH)) {
            this.polygons[polygonCount++] = Polygon(
                arrayOf<Vertex>(vertex5, vertex6, vertex7, vertex8),
                l,
                q,
                n,
                r,
                textureWidth,
                textureHeight,
                mirror,
                SkinDirection.SOUTH
            )
        }
    }

    private fun visibleFace(face: SkinDirection?): Boolean {
        for (dir in hidden) {
            if (dir == face) {
                return false
            }
        }

        return true
    }

    fun render(worldRenderer: WorldRenderer, redTint: Boolean) {
        var redTint = redTint
        redTint = false
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL)
        var polygon: Polygon

        for (id in 0 until polygonCount) {
            polygon = polygons[id]!!

            for (i in 0..3) {
                val vertex = polygon.vertices[i]
                worldRenderer.pos(vertex.pos.x.toDouble(), vertex.pos.y.toDouble(), vertex.pos.z.toDouble())
                    .tex(vertex.u.toDouble(), vertex.v.toDouble())
                    .color(255, if (redTint) 127 else 255, if (redTint) 127 else 255, 255)
                    .normal(polygon.normal.x, polygon.normal.y, polygon.normal.z).endVertex()
            }
        }

        Tessellator.getInstance().draw()
    }
}

