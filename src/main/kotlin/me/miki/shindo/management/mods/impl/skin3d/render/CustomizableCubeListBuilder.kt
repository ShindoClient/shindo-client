package me.miki.shindo.management.mods.impl.skin3d.render

import com.google.common.collect.Lists
import me.miki.shindo.management.mods.impl.skin3d.SkinDirection

class CustomizableCubeListBuilder {
    val cubes: MutableList<CustomizableCube?> = Lists.newArrayList<CustomizableCube?>()
    private var xTexOffs = 0
    private var yTexOffs = 0
    private var mirror = false

    fun texOffs(i: Int, j: Int): CustomizableCubeListBuilder {
        this.xTexOffs = i
        this.yTexOffs = j
        return this
    }

    fun mirror(bl: Boolean): CustomizableCubeListBuilder {
        this.mirror = bl
        return this
    }

    fun addBox(
        x: Float,
        y: Float,
        z: Float,
        pixelSize: Float,
        hide: Array<SkinDirection?>?
    ): CustomizableCubeListBuilder {
        val textureSize = 64
        this.cubes.add(
            CustomizableCube(
                xTexOffs,
                yTexOffs,
                x,
                y,
                z,
                pixelSize,
                pixelSize,
                pixelSize,
                0f,
                0f,
                0f,
                this.mirror,
                textureSize.toFloat(),
                textureSize.toFloat(),
                hide
            )
        )

        return this
    }

    companion object {
        fun create(): CustomizableCubeListBuilder {
            return CustomizableCubeListBuilder()
        }
    }
}