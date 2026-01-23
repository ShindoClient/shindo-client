package me.miki.shindo.management.addons.hackerdetector.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper

/**
 * Vetor 3D para cálculos matemáticos
 */
data class Vector3D(val x: Double, val y: Double, val z: Double) {
    
    companion object {
        fun getVectToEntity(from: Entity, to: Entity): Vector3D {
            return Vector3D(to.posX - from.posX, to.posY - from.posY, to.posZ - from.posZ)
        }
        
        fun getPlayersEyePos(player: EntityPlayer): Vector3D {
            return Vector3D(player.posX, player.posY + player.eyeHeight.toDouble(), player.posZ)
        }
        
        fun getPlayersLookVec(player: EntityPlayer): Vector3D {
            return getVectorFromRotation(player.rotationPitch, player.rotationYawHead)
        }
        
        /**
         * Cria um Vector3D normalizado a partir de pitch e yaw
         */
        fun getVectorFromRotation(pitch: Float, yaw: Float): Vector3D {
            val f = MathHelper.cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val f1 = MathHelper.sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val f2 = -MathHelper.cos(-pitch * 0.017453292F)
            val f3 = MathHelper.sin(-pitch * 0.017453292F)
            return Vector3D((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
        }
    }
    
    /**
     * Retorna true se as coordenadas x, y, z estão dentro do AxisAlignedBB fornecido
     */
    fun isVectInside(bb: AxisAlignedBB): Boolean {
        return x > bb.minX && x < bb.maxX && y > bb.minY && y < bb.maxY && z > bb.minZ && z < bb.maxZ
    }
    
    fun addVector(x1: Double, y1: Double, z1: Double): Vector3D {
        return Vector3D(x + x1, y + y1, z + z1)
    }
    
    /**
     * Retorna a norma do vetor
     */
    fun norm(): Double {
        val sum = x * x + y * y + z * z
        return Math.sqrt(sum)
    }
    
    fun normSquared(): Double = x * x + y * y + z * z
    
    fun dotProduct(other: Vector3D): Double = x * other.x + y * other.y + z * other.z
    
    /**
     * Retorna o ângulo absoluto entre as projeções 2D dos vetores no plano XZ
     */
    fun getXZAngleDiffWithVector(other: Vector3D): Double {
        val den = Math.sqrt((x * x + z * z) * (other.x * other.x + other.z * other.z))
        if (den < 1.0000000116860974E-7) return 0.0
        
        val cos = (x * other.x + z * other.z) / den
        return when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(Math.acos(cos))
        }
    }
    
    /**
     * Retorna o vetor 2D resultante da projeção deste no plano XZ
     */
    fun getProjectionInXZPlane(): Vector2D = Vector2D(x, z)
    
    /**
     * Retorna o comprimento do vetor 2D resultante da projeção deste no plano XZ
     */
    fun normInXZPlane(): Double = getProjectionInXZPlane().norm()
    
    /**
     * Retorna o ângulo absoluto entre este vetor e outro vetor
     */
    fun getAngleWithVector(other: Vector3D): Double {
        val den = Math.sqrt((x * x + y * y + z * z) * (other.x * other.x + other.y * other.y + other.z * other.z))
        if (den < 1.0000000116860974E-7) return 0.0
        
        val cos = dotProduct(other) / den
        return when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(Math.acos(cos))
        }
    }
    
    fun multiply(d: Double): Vector3D = Vector3D(x * d, y * d, z * d)
    
    override fun toString(): String = "{${String.format("%.4f", x)}, ${String.format("%.4f", y)}, ${String.format("%.4f", z)}}"
}
