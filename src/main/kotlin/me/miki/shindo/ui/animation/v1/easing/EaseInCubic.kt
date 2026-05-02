package me.miki.shindo.ui.animation.v1.easing

import me.miki.shindo.ui.animation.v1.EasingFunctions
import me.miki.shindo.ui.animation.v1.TimedAnimation

class EaseInCubic(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::inCubic)
