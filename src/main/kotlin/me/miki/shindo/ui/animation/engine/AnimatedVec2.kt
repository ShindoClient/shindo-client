package me.miki.shindo.ui.animation.engine

data class Vec2(val x: Float, val y: Float)

class AnimatedVec2(
    initial: Vec2 = Vec2(0f, 0f),
    controller: AnimationController? = AnimationController.global
) : AnimatedValue<Vec2>(initial, controller) {

    override fun lerp(from: Vec2, to: Vec2, t: Float): Vec2 {
        val x = from.x + (to.x - from.x) * t
        val y = from.y + (to.y - from.y) * t
        return Vec2(x, y)
    }
}
