package me.miki.shindo.ui.animation.v1.curve

import me.miki.shindo.ui.animation.v1.Direction
import me.miki.shindo.ui.animation.v1.EasingFunctions
import me.miki.shindo.ui.animation.v1.TimedAnimation


class DecelerateAnimation(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::decelerate) {

    constructor(ms: Int, endPoint: Double, direction: Direction) : this(ms, endPoint) {
        setDirection(direction)
    }
}
