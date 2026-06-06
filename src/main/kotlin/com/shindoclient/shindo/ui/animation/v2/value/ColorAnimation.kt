package com.shindoclient.shindo.ui.animation.v2.value

import com.shindoclient.extensions.ui.animation.getValueI
import com.shindoclient.extensions.ui.animation.setValue
import java.awt.Color

class ColorAnimation {
    private val animation = arrayOfNulls<SimpleAnimation>(3)

    init {
        for (i in animation.indices) {
            animation[i] = SimpleAnimation()
        }
    }

    fun getColor(
        color: Color,
        speed: Int,
    ): Color {
        animation[0]!!.setAnimation(color.red.toFloat(), speed.toDouble())
        animation[1]!!.setAnimation(color.green.toFloat(), speed.toDouble())
        animation[2]!!.setAnimation(color.blue.toFloat(), speed.toDouble())

        return Color(
            animation[0]!!.getValueI(),
            animation[1]!!.getValueI(),
            animation[2]!!.getValueI(),
            color.alpha,
        )
    }

    fun getColor(color: Color): Color = getColor(color, 12)

    fun setColor(color: Color) {
        animation[0]!!.setValue(color.red)
        animation[1]!!.setValue(color.green)
        animation[2]!!.setValue(color.blue)
    }
}
