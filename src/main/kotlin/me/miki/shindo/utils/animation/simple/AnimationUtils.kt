package me.miki.shindo.utils.animation.simple

object AnimationUtils {

    fun calculateCompensation(target: Float, current: Float, speed: Double, delta: Long): Float {
        var currentVar = current
        val diff = currentVar - target
        val add = delta * (speed / 50)

        when {
            diff > speed -> {
                currentVar = if (currentVar - add > target) {
                    (currentVar - add).toFloat()
                } else {
                    target
                }
            }
            diff < -speed -> {
                currentVar = if (currentVar + add < target) {
                    (currentVar + add).toFloat()
                } else {
                    target
                }
            }
            else -> {
                if (kotlin.math.abs(currentVar - target) < 0.03f) {
                    currentVar = target
                }
            }
        }

        return currentVar
    }
}