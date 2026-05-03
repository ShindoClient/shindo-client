package me.miki.shindo.ui.animation.v2.curve

import me.miki.shindo.ui.animation.v2.core.Animation
import me.miki.shindo.ui.animation.v2.core.Direction
import me.miki.shindo.ui.animation.v2.core.EasingFunctions

class SmoothStepAnimation(
    ms: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(ms, endPoint, EasingFunctions::smoothStep, direction)
