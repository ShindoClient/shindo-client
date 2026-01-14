package me.miki.shindo.management.mods.impl.skin3d.render

import me.miki.shindo.management.mods.impl.skin3d.SkinDirection
import me.miki.shindo.management.mods.impl.skin3d.opengl.NativeImage

object SolidPixelWrapper {
    private val offsets = arrayOf(intArrayOf(0, 1), intArrayOf(0, -1), intArrayOf(1, 0), intArrayOf(-1, 0))
    private val hiddenDirN: Array<SkinDirection> = arrayOf(
        SkinDirection.WEST, SkinDirection.EAST, SkinDirection.UP,
        SkinDirection.DOWN
    )
    private val hiddenDirS: Array<SkinDirection> = arrayOf(
        SkinDirection.EAST, SkinDirection.WEST, SkinDirection.UP,
        SkinDirection.DOWN
    )
    private val hiddenDirW: Array<SkinDirection> = arrayOf(
        SkinDirection.SOUTH, SkinDirection.NORTH, SkinDirection.UP,
        SkinDirection.DOWN
    )
    private val hiddenDirE: Array<SkinDirection> = arrayOf(
        SkinDirection.NORTH, SkinDirection.SOUTH, SkinDirection.UP,
        SkinDirection.DOWN
    )
    private val hiddenDirUD: Array<SkinDirection> = arrayOf(
        SkinDirection.EAST, SkinDirection.WEST, SkinDirection.NORTH,
        SkinDirection.SOUTH
    )

    fun wrapBox(
        natImage: NativeImage,
        width: Int,
        height: Int,
        depth: Int,
        textureU: Int,
        textureV: Int,
        topPivot: Boolean,
        rotationOffset: Float
    ): CustomizableModelPart {
        val cubes: MutableList<CustomizableCube> = ArrayList()
        val pixelSize = 1f
        val staticXOffset = -width / 2f
        val staticYOffset = if (topPivot) +rotationOffset else -height + rotationOffset
        val staticZOffset = -depth / 2f

        for (u in 0..<width) {
            for (v in 0..<height) {
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
                    textureU + depth + u, textureV + depth + v, staticXOffset + u, staticYOffset + v, staticZOffset,
                    SkinDirection.SOUTH
                )

                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == height - 1,
                    textureU + 2 * depth + width + u, textureV + depth + v, staticXOffset + width - 1 - u,
                    staticYOffset + v, staticZOffset + depth - 1, SkinDirection.NORTH
                )
            }
        }

        for (u in 0..<depth) {
            for (v in 0..<height) {
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
                    textureU - 1 + depth - u, textureV + depth + v, staticXOffset, staticYOffset + v,
                    staticZOffset + u, SkinDirection.EAST
                )

                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == depth - 1 || v == height - 1,
                    textureU + depth + width + u, textureV + depth + v, staticXOffset + width - 1f,
                    staticYOffset + v, staticZOffset + u, SkinDirection.WEST
                )
            }
        }

        for (u in 0..<width) {
            for (v in 0..<depth) {
                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
                    textureU + depth + u, textureV + depth - 1 - v, staticXOffset + u, staticYOffset,
                    staticZOffset + v, SkinDirection.UP
                )

                addPixel(
                    natImage, cubes, pixelSize, u == 0 || v == 0 || u == width - 1 || v == depth - 1,
                    textureU + depth + width + u, textureV + depth - 1 - v, staticXOffset + u,
                    staticYOffset + height - 1f, staticZOffset + v, SkinDirection.DOWN
                )
            }
        }

        return CustomizableModelPart(cubes)
    }

    private fun addPixel(
        natImage: NativeImage,
        cubes: MutableList<CustomizableCube>,
        pixelSize: Float,
        onBorder: Boolean,
        u: Int,
        v: Int,
        x: Float,
        y: Float,
        z: Float,
        dir: SkinDirection
    ) {
        if (natImage.getLuminanceOrAlpha(u, v).toInt() != 0) {
            val hide: MutableSet<SkinDirection> = HashSet()
            if (!onBorder) {
                for (i in offsets.indices) {
                    val tU = u + offsets[i][1]
                    val tV = v + offsets[i][0]
                    if (tU >= 0 && tU < 64 && tV >= 0 && tV < 64 && natImage.getLuminanceOrAlpha(tU, tV).toInt() != 0) {
                        if (dir == SkinDirection.NORTH) {
                            hide.add(hiddenDirN[i])
                        }

                        if (dir == SkinDirection.SOUTH) {
                            hide.add(hiddenDirS[i])
                        }

                        if (dir == SkinDirection.EAST) {
                            hide.add(hiddenDirE[i])
                        }

                        if (dir == SkinDirection.WEST) {
                            hide.add(hiddenDirW[i])
                        }

                        if (dir == SkinDirection.UP || dir == SkinDirection.DOWN) {
                            hide.add(hiddenDirUD[i])
                        }
                    }
                }
                hide.add(dir)
            }

            cubes.addAll(
                CustomizableCubeListBuilder.Companion.create().texOffs(u - 2, v - 1)
                    .addBox(x, y, z, pixelSize, hide.toTypedArray()).cubes
            )
        }
    }
}
