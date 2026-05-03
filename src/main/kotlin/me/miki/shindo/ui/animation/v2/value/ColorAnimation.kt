package me.miki.shindo.ui.animation.v2.value

import java.awt.Color

class ColorAnimation(initial: Color = Color(0, 0, 0, 0)) {

    private val r = SimpleAnimation(initial.red.toFloat())
    private val g = SimpleAnimation(initial.green.toFloat())
    private val b = SimpleAnimation(initial.blue.toFloat())
    private val a = SimpleAnimation(initial.alpha.toFloat())

    private var cached: Color = initial


    fun toward(target: Color, rgbSpeed: Int = 12, alphaSpeed: Int = rgbSpeed): Color {
        r.toward(target.red.toFloat(),   rgbSpeed)
        g.toward(target.green.toFloat(), rgbSpeed)
        b.toward(target.blue.toFloat(),  rgbSpeed)
        a.toward(target.alpha.toFloat(), alphaSpeed)

        val ri = r.value.toInt()
        val gi = g.value.toInt()
        val bi = b.value.toInt()
        val ai = a.value.toInt()

        if (cached.red != ri || cached.green != gi || cached.blue != bi || cached.alpha != ai) {
            cached = Color(ri, gi, bi, ai)
        }
        return cached
    }

    fun snap(color: Color) {
        r.snap(color.red.toFloat())
        g.snap(color.green.toFloat())
        b.snap(color.blue.toFloat())
        a.snap(color.alpha.toFloat())
        cached = color
    }

    val current: Color get() = cached
}
