package me.miki.shindo.ui.animation.curve

import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.EasingFunctions
import me.miki.shindo.ui.animation.TimedAnimation


class DecelerateAnimation(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::decelerate) {

    constructor(ms: Int, endPoint: Double, direction: Direction) : this(ms, endPoint) {
        setDirection(direction)
    }
}
