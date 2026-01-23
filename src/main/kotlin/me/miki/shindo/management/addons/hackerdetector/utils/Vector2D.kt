package me.miki.shindo.management.addons.hackerdetector.utils

import net.minecraft.util.MathHelper

/**
 * Vetor 2D para cálculos matemáticos
 */
data class Vector2D(val u: Double, val v: Double) {
    
    /**
     * Retorna a norma do vetor
     */
    fun norm(): Double = Math.sqrt(u * u + v * v)
    
    /**
     * Retorna o ângulo absoluto entre este vetor e o eixo OU
     */
    fun getAngle(): Double {
        val norm = this.norm()
        if (norm < 1.0000000116860974E-7) return 0.0
        
        val cos = u / norm
        return when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(Math.acos(cos))
        }
    }
    
    /**
     * Retorna o ângulo orientado entre este vetor e o eixo OU
     */
    fun getOrientedAngle(): Double {
        val norm = this.norm()
        if (norm < 1.0000000116860974E-7) return 0.0
        
        val cos = u / norm
        val angle = when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(Math.acos(cos))
        }
        
        return if (v >= 0) angle else -angle
    }
    
    /**
     * Retorna o produto escalar
     */
    fun dotProduct(other: Vector2D): Double = u * other.u + v * other.v
    
    /**
     * Retorna o ângulo absoluto entre este vetor e outro vetor em graus
     */
    fun getAngleWithVector(other: Vector2D): Double {
        val den = Math.sqrt((u * u + v * v) * (other.u * other.u + other.v * other.v))
        if (den < 1.0000000116860974E-7) return 0.0
        
        val cos = dotProduct(other) / den
        return when {
            cos > 1 -> 0.0
            cos < -1 -> 180.0
            else -> Math.toDegrees(Math.acos(cos))
        }
    }
    
    companion object {
        /**
         * Cria um Vector2D no plano XZ a partir de pitch e yaw
         */
        fun getVectorFromRotation(pitch: Float, yaw: Float): Vector2D {
            val f = MathHelper.cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val f1 = MathHelper.sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val f2 = -MathHelper.cos(-pitch * 0.017453292F)
            return Vector2D((f1 * f2).toDouble(), (f * f2).toDouble())
        }
    }
    
    override fun toString(): String = "{${String.format("%.4f", u)}, ${String.format("%.4f", v)}}"
}
