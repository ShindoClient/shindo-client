package me.miki.shindo.ui.animation.value

import java.awt.Color
open class ColorAnimation {

    private val animation = Array(3) { SimpleAnimation() }

    fun getColor(color: Color, speed: Int): Color {
        animation[0].setAnimation(color.red.toFloat(), speed.toDouble())
        animation[1].setAnimation(color.green.toFloat(), speed.toDouble())
        animation[2].setAnimation(color.blue.toFloat(), speed.toDouble())
        return Color(
            animation[0].value.toInt(),
            animation[1].value.toInt(),
            animation[2].value.toInt(),
            color.alpha
        )
    }

    fun getColor(color: Color): Color = getColor(color, 12)

    fun setColor(color: Color) {
        animation[0].value = color.red.toFloat()
        animation[1].value = color.green.toFloat()
        animation[2].value = color.blue.toFloat()
    }
}
