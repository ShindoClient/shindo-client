package me.miki.shindo.management.mods.impl.skin3d

import net.minecraft.util.Vec3i
import org.lwjgl.util.vector.Vector3f

enum class SkinDirection(private val normal: Vec3i) {
    DOWN(Vec3i(0, -1, 0)), UP(Vec3i(0, 1, 0)), NORTH(Vec3i(0, 0, -1)), SOUTH(Vec3i(0, 0, 1)),
    WEST(Vec3i(-1, 0, 0)), EAST(Vec3i(1, 0, 0));

    val stepX: Int
        get() = this.normal.x

    val stepY: Int
        get() = this.normal.y

    val stepZ: Int
        get() = this.normal.z

    fun step(): Vector3f {
        return Vector3f(this.stepX.toFloat(), this.stepY.toFloat(), this.stepZ.toFloat())
    }
}
