package me.miki.shindo.ui.animation.engine

class AnimatedFloat(
    initial: Float = 0f,
    private val controller: AnimationController? = AnimationController.global
) : AnimatedValue<Float>(initial, controller) {

    private var springSolver: SpringSolver? = null
    private var springVelocity = 0f

    override fun update(deltaMs: Long) {
        val solver = springSolver
        if (solver == null) {
            super.update(deltaMs)
            return
        }

        if (!isRunning) {
            return
        }

        if (!GlobalAnimationSettings.enabled) {
            snapTo(target)
            springSolver = null
            springVelocity = 0f
            return
        }

        val dt = deltaMs.toFloat() / 1000f
        val (newValue, newVelocity) = solver.step(value, target, springVelocity, dt)
        value = newValue
        springVelocity = newVelocity

        if (solver.isAtRest(value, target, springVelocity)) {
            value = target
            isRunning = false
            springSolver = null
            springVelocity = 0f
        }
    }

    fun animateSpring(
        to: Float,
        stiffness: Float = 170f,
        damping: Float = 26f
    ): AnimatedFloat {
        start = value
        target = to
        elapsedMs = 0
        durationMs = 0
        easing = Easing.EASE_OUT
        springSolver = SpringSolver(stiffness, damping)
        springVelocity = 0f
        isRunning = true

        if (!GlobalAnimationSettings.enabled) {
            snapTo(to)
            springSolver = null
            return this
        }

        controller?.add(this)
        return this
    }

    override fun lerp(from: Float, to: Float, t: Float): Float {
        return from + (to - from) * t
    }

    companion object {
        fun spring(
            from: Float,
            to: Float,
            stiffness: Float = 170f,
            damping: Float = 26f
        ): AnimatedFloat {
            val animated = AnimatedFloat(from)
            animated.animateSpring(to, stiffness, damping)
            return animated
        }
    }
}
