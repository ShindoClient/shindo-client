@file:JvmName("MouseUtilsExtensions")

package com.shindoclient.extensions.utils.mouse

import com.shindoclient.shindo.utils.mouse.MouseUtils

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
