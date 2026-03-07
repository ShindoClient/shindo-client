package me.miki.shindo.addon.api.animation

/**
 * Interface de animação. O client fornece implementação (ex: SimpleAnimation).
 */
interface IAnimation {

    var value: Float

    fun setAnimation(target: Float, speed: Double)

    fun setAnimation(target: Float, speed: Int) {
        setAnimation(target, speed.toDouble())
    }

    fun setAnimation(target: Float) {
        setAnimation(target, 16.0)
    }
}
