@file:JvmName("AnimationExtensions")

package me.miki.extensions.ui.animation

import me.miki.shindo.ui.animation.v2.value.SimpleAnimation


var SimpleAnimation.animation: Float
    get() = getValue()
    set(target) = setAnimation(target)

fun SimpleAnimation.setValue(value: Number) {
    setValue(value.toFloat())
}


fun SimpleAnimation.getValueI(): Int {
    return getValue().toInt()
}
