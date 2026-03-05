package me.miki.shindo.api.compat

import me.miki.client_api.animation.AnimationDirection
import me.miki.client_api.animation.IColorAnimation
import me.miki.client_api.animation.IAnimation
import me.miki.client_api.animation.ITimedAnimation
import me.miki.client_api.render.AddonColor
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation
import java.awt.Color

/**
 * Adaptador unificado para animações. Usa [wrap], [wrapTimed], [wrapColor] conforme o tipo.
 * Um único ponto de entrada, sem classes separadas por animação.
 */
object AnimationAdapter {

    @JvmStatic
    fun wrap(delegate: SimpleAnimation): IAnimation = Simple(delegate)

    @JvmStatic
    fun wrapTimed(delegate: Animation): ITimedAnimation = Timed(delegate)

    @JvmStatic
    fun wrapColor(delegate: ColorAnimation): IColorAnimation = Color(delegate)

    private class Simple(private val delegate: SimpleAnimation) : IAnimation {
        override var value: Float
            get() = delegate.value
            set(v) { delegate.value = v }
        override fun setAnimation(target: Float, speed: Double) = delegate.setAnimation(target, speed)
        override fun setAnimation(target: Float, speed: Int) = delegate.setAnimation(target, speed)
        override fun setAnimation(target: Float) = delegate.setAnimation(target)
    }

    private class Timed(private val delegate: Animation) : ITimedAnimation {
        private var onCompleteCallback: (() -> Unit)? = null
        private var completedForward = false

        override fun getValue(): Double {
            val v = delegate.getValue()
            val done = delegate.isDone()
            if (done && delegate.direction == Direction.FORWARDS && !completedForward) {
                completedForward = true
                onCompleteCallback?.invoke()
            }
            if (!done) completedForward = false
            return v
        }
        override fun reset() {
            completedForward = false
            delegate.reset()
        }
        override fun changeDirection() = delegate.changeDirection()
        override fun setDirection(direction: AnimationDirection) {
            delegate.setDirection(when (direction) {
                AnimationDirection.FORWARDS -> Direction.FORWARDS
                AnimationDirection.BACKWARDS -> Direction.BACKWARDS
            })
        }
        override fun isDone() = delegate.isDone()
        override fun setOnComplete(callback: () -> Unit) {
            onCompleteCallback = callback
        }
    }

    private class Color(private val delegate: ColorAnimation) : IColorAnimation {
        override fun getColor(color: AddonColor): AddonColor {
            val awt = Color(color.r, color.g, color.b, color.a)
            val result = delegate.getColor(awt)
            return AddonColor(result.red, result.green, result.blue, result.alpha)
        }
        override fun getColor(color: AddonColor, speed: Int): AddonColor {
            val awt = Color(color.r, color.g, color.b, color.a)
            val result = delegate.getColor(awt, speed)
            return AddonColor(result.red, result.green, result.blue, result.alpha)
        }
        override fun setColor(color: AddonColor) {
            delegate.setColor(Color(color.r, color.g, color.b, color.a))
        }
    }
}
