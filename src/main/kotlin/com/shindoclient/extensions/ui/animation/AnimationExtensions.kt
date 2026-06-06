@file:JvmName("AnimationExtensions")

package com.shindoclient.extensions.ui.animation

import com.shindoclient.shindo.ui.animation.v2.screen.ScreenAnimation
import com.shindoclient.shindo.ui.animation.v2.value.ColorAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import java.awt.Color

fun SimpleAnimation.setValue(value: Number) = setValue(value.toFloat())

fun SimpleAnimation.getValueI(): Int = getValue().toInt()

fun SimpleAnimation.setAnimation(
    value: Number,
    speed: Number,
) = setAnimation(value.toFloat(), speed.toDouble())

fun ScreenAnimation.wrap(
    glRender: Runnable?,
    task: Runnable,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    animationProgress: Float,
    alphaProgress: Float,
) = wrap(glRender, task, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), animationProgress, alphaProgress)

fun ScreenAnimation.wrap(
    task: Runnable,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    animationProgress: Float,
    alphaProgress: Float,
    stencil: Boolean,
) = wrap(
    null,
    task,
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    animationProgress,
    alphaProgress,
    stencil,
)

fun ScreenAnimation.wrap(
    task: Runnable,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    animationProgress: Float,
    alphaProgress: Float,
) = wrap(
    null,
    task,
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    animationProgress,
    alphaProgress,
    false,
)

fun ScreenAnimation.wrap(
    task: Runnable,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    progress: Float,
) = wrap(null, task, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), progress, progress, false)

fun ColorAnimation.reset(color: Color = Color(0, 0, 0, 0)) = setColor(color)
