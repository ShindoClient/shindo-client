package me.miki.shindo.ui.animation.curve

import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.EasingFunctions
import me.miki.shindo.ui.animation.TimedAnimation

class SmoothStepAnimation(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::smoothStep) {

    constructor(ms: Int, endPoint: Double, direction: Direction) : this(ms, endPoint) {
        setDirection(direction)
    }
}
