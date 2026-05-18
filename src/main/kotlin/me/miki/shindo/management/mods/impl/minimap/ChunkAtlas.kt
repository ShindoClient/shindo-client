package me.miki.shindo.management.mods.impl.minimap

import me.miki.shindo.injection.interfaces.IMixinWorld
import net.minecraft.block.material.MapColor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.minecraft.util.BlockPos.MutableBlockPos
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.nio.IntBuffer
import java.util.*
import java.util.function.IntFunction
import java.util.function.IntPredicate
import java.util.stream.IntStream
import kotlin.math.sqrt

class ChunkAtlas(
    maxChunkRadius: Int,
) : Iterable<ChunkTile> {
    private val chunkCoords: Array<ChunkCoordIntPair?>
    private val reusableChunks: BitSet
    val chunkRadius: Int
    private val chunkSpanL2: Int
    val spriteWidth: Double
    val spriteHeight: Double
    private val pixels: IntBuffer
    val textureHandle: Int

    init {
        var maxChunks = maxChunkRadius * maxChunkRadius shl 2

        var texWidth = Integer.highestOneBit(maxChunks - 1) shl 5
        var texHeight = 16

        val texLimit = Minecraft.getGLMaximumTextureSize()
        while (texWidth > texLimit) {
            texWidth = texWidth shr 1
            texHeight = texHeight shl 1
        }

        while (texHeight > texLimit) {
            texHeight = texHeight shr 1
        }

        val chunkCapacity = texWidth * texHeight shr 8

        if (maxChunks > chunkCapacity) {
            maxChunks = chunkCapacity
        }

        this.chunkRadius = sqrt((maxChunks shr 2).toDouble()).toInt()
        this.chunkSpanL2 = Integer.numberOfTrailingZeros(texWidth shr 4)

        this.spriteWidth = 16.0 / texWidth
        this.spriteHeight = 16.0 / texHeight

        this.chunkCoords = arrayOfNulls<ChunkCoordIntPair>(maxChunks)
        this.reusableChunks = BitSet(maxChunks)

        this.textureHandle = GL11.glGenTextures()
        GlStateManager.bindTexture(this.textureHandle)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA,
            texWidth,
            texHeight,
            0,
            GL12.GL_BGRA,
            GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
            null as IntBuffer?,
        )

        this.pixels = GLAllocation.createDirectIntBuffer(256)
    }

    fun getSpriteX(offset: Int): Double = (offset and ((1 shl this.chunkSpanL2) - 1)) * this.spriteWidth

    fun getSpriteY(offset: Int): Double = (offset shr this.chunkSpanL2) * this.spriteHeight

    fun clear() {
        Arrays.fill(this.chunkCoords, null)
    }

    fun loadChunks(
        chunkX: Int,
        chunkZ: Int,
    ) {
        val w: World? = Minecraft.getMinecraft().theWorld
        if (w == null) {
            return
        }

        this.reusableChunks.clear()

        for (offs in this.chunkCoords.indices) {
            val coords = this.chunkCoords[offs]
            if (coords == null) {
                continue
            }

            var offsX = coords.chunkXPos - chunkX
            var offsZ = coords.chunkZPos - chunkZ

            if (offsX < -this.chunkRadius || this.chunkRadius <= offsX) {
                this.chunkCoords[offs] = null
                continue
            }

            if (offsZ < -this.chunkRadius || this.chunkRadius <= offsZ) {
                this.chunkCoords[offs] = null
                continue
            }

            offsX += this.chunkRadius
            offsZ += this.chunkRadius

            this.reusableChunks.set(offsX + offsZ * this.chunkRadius * 2)
        }

        for (relZ in this.chunkRadius * 2 - 1 downTo 0) {
            for (relX in this.chunkRadius * 2 - 1 downTo 0) {
                val checkIdx = relX + relZ * this.chunkRadius * 2

                if (this.reusableChunks.get(checkIdx)) {
                    continue
                }

                val x = chunkX + relX - this.chunkRadius
                val z = chunkZ + relZ - this.chunkRadius

                val c = this.getLoadedChunk(x, z)
                if (c == null) {
                    continue
                }

                this.reserveOffset(c)

                this.recolorChunk(x, z + 1)
            }
        }
    }

    fun refreshChunk(
        x: Int,
        z: Int,
    ) {
        this.recolorChunk(x, z)
        this.recolorChunk(x, z + 1)
    }

    override fun iterator(): MutableIterator<ChunkTile> =
        IntStream
            .range(0, this.chunkCoords.size)
            .filter(IntPredicate { offs: Int -> this.chunkCoords[offs] != null })
            .mapToObj<ChunkTile>(
                IntFunction { offs: Int ->
                    val coords = this.chunkCoords[offs]!!
                    ChunkTile(coords.chunkXPos, coords.chunkZPos, offs)
                },
            ).iterator()

    private fun reserveOffset(c: Chunk) {
        val offs = this.searchChunkAtlas(null)

        check(offs != -1) { "Chunk coordinate array full." }

        this.chunkCoords[offs] = c.chunkCoordIntPair

        this.updateColorData(c, offs)
    }

    private fun recolorChunk(
        x: Int,
        z: Int,
    ) {
        val c = this.getLoadedChunk(x, z)

        if (c == null) {
            return
        }

        val offs = this.searchChunkAtlas(ChunkCoordIntPair(x, z))
        if (offs == -1) {
            return
        }

        this.updateColorData(c, offs)
    }

    private fun updateColorData(
        src: Chunk,
        offs: Int,
    ) {
        this.computeColors(src)

        var x = offs and ((1 shl this.chunkSpanL2) - 1)
        var y = offs shr this.chunkSpanL2

        x = x shl 4
        y = y shl 4

        GlStateManager.bindTexture(this.textureHandle)
        GL11.glTexSubImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            x,
            y,
            16,
            16,
            GL12.GL_BGRA,
            GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
            this.pixels,
        )
    }

    private fun computeColors(src: Chunk) {
        val northHeights = IntArray(16)

        Arrays.fill(northHeights, -1)

        val north = this.getLoadedChunk(src.xPosition, src.zPosition - 1)
        if (north != null) {
            for (x in 0..15) {
                northHeights[x] = this.getTopColoredBlockState(north, x, 15).y
            }
        }

        for (x in 0..15) {
            var northHeight = northHeights[x]

            for (z in 0..15) {
                var pos = this.getTopColoredBlockState(src, x, z)
                var state = src.getBlockState(pos)
                val color = state.block.getMapColor(state)

                val height = pos.y
                var shade = 1

                if (northHeight > height) {
                    shade = 0
                } else if (northHeight >= 0 && northHeight < height) {
                    shade = 2
                }

                var depth = 0
                while (pos.y >= 0 && !state.block.material.isSolid) {
                    pos = pos.add(0, -1, 0)
                    state = src.getBlockState(pos)
                    depth++
                }

                if (depth > 0) {
                    val dither = depth + (((x xor z) and 1) shl 1)

                    if (dither < 5) {
                        shade = 2
                    } else if (dither > 9) {
                        shade = 0
                    }
                }

                val rgb: Int

                if (height > 0) {
                    rgb = color.getMapColor(shade)
                } else if (((x xor z) and 3) == 0) {
                    rgb = 0x2d2d5a
                } else {
                    rgb = 0x1e1e3c
                }

                northHeight = height

                this.pixels.put(x or (z shl 4), rgb)
            }
        }
    }

    private fun getTopColoredBlockState(
        src: Chunk,
        x: Int,
        z: Int,
    ): BlockPos {
        val pos = MutableBlockPos()

        for (y in src.topFilledSegment + 15 downTo 0) {
            val state = src.getBlockState(pos.set(x, y, z))

            if (state.block.getMapColor(state) !== MapColor.airColor) {
                break
            }
        }

        return pos
    }

    private fun searchChunkAtlas(c: ChunkCoordIntPair?): Int {
        for (offs in this.chunkCoords.indices) {
            if (c == this.chunkCoords[offs]) {
                return offs
            }
        }

        return -1
    }

    private fun getLoadedChunk(
        x: Int,
        z: Int,
    ): Chunk? {
        val world: World? = Minecraft.getMinecraft().theWorld

        if (world == null) {
            return null
        }

        if (!(world as IMixinWorld).isLoaded(x, z, true)) {
            return null
        }

        val c = world.getChunkFromChunkCoords(x, z)
        if (c.isEmpty) {
            return null
        }

        return c
    }
}
