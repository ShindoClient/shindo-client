package me.miki.shindo.ui.animation.engine

import kotlin.math.abs

data class SpringSolver(
    val stiffness: Float = 170f,
    val damping: Float = 26f,
    val restSpeed: Float = 0.001f,
    val restDelta: Float = 0.001f
) {
    fun step(value: Float, target: Float, velocity: Float, deltaSeconds: Float): Pair<Float, Float> {
        val force = -stiffness * (value - target)
        val dampingForce = -damping * velocity
        val accel = force + dampingForce
        val newVelocity = velocity + accel * deltaSeconds
        val newValue = value + newVelocity * deltaSeconds
        return Pair(newValue, newVelocity)
    }

    fun isAtRest(value: Float, target: Float, velocity: Float): Boolean {
        return abs(velocity) < restSpeed && abs(value - target) < restDelta
    }
}
