@file:JvmName("MouseUtilsExtensions")

package me.miki.extensions.utils.mouse

import me.miki.shindo.utils.mouse.MouseUtils

fun MouseUtils.isInside(
    mouseX: Number,
    mouseY: Number,
    x: Number,
    y: Number,
    w: Number,
    h: Number,
): Boolean =
    this.isInside(
        mouseX.toInt(),
        mouseY.toInt(),
        x.toDouble(),
        y.toDouble(),
        w.toDouble(),
        h.toDouble(),
    )
