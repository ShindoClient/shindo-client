package me.miki.shindo.utils

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.sqrt

class LocationUtils(
    var x: Double,
    var y: Double,
    var z: Double,
    var yaw: Float = 0.0f,
    var pitch: Float = 0.0f,
) {
    constructor(pos: BlockPos) : this(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

    constructor(entity: EntityLivingBase) : this(entity.posX, entity.posY, entity.posZ)

    constructor(x: Int, y: Int, z: Int) : this(x.toDouble(), y.toDouble(), z.toDouble())

    fun add(
        x: Int,
        y: Int,
        z: Int,
    ): LocationUtils {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    fun add(
        x: Double,
        y: Double,
        z: Double,
    ): LocationUtils {
        this.x += x
        this.y += y
        this.z += z
        return this
    }

    fun subtract(
        x: Int,
        y: Int,
        z: Int,
    ): LocationUtils {
        this.x -= x
        this.y -= y
        this.z -= z
        return this
    }

    fun subtract(
        x: Double,
        y: Double,
        z: Double,
    ): LocationUtils {
        this.x -= x
        this.y -= y
        this.z -= z
        return this
    }

    fun getBlock(): Block =
        Minecraft
            .getMinecraft()
            .theWorld
            .getBlockState(toBlockPos())
            .block

    fun setX(x: Double): LocationUtils {
        this.x = x
        return this
    }

    fun setY(y: Double): LocationUtils {
        this.y = y
        return this
    }

    fun setZ(z: Double): LocationUtils {
        this.z = z
        return this
    }

    fun setYaw(yaw: Float): LocationUtils {
        this.yaw = yaw
        return this
    }

    fun setPitch(pitch: Float): LocationUtils {
        this.pitch = pitch
        return this
    }

    fun toBlockPos(): BlockPos = BlockPos(x, y, z)

    fun distanceTo(loc: LocationUtils): Double {
        val dx = loc.x - x
        val dz = loc.z - z
        val dy = loc.y - y
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distanceToXZ(loc: LocationUtils): Double {
        val dx = loc.x - x
        val dz = loc.z - z
        return sqrt(dx * dx + dz * dz)
    }

    fun distanceToY(loc: LocationUtils): Double {
        val dy = loc.y - y
        return sqrt(dy * dy)
    }

    fun toVector(): Vec3 = Vec3(x, y, z)

    companion object {
        @JvmStatic
        fun fromBlockPos(blockPos: BlockPos): LocationUtils =
            LocationUtils(blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble())
    }
}
