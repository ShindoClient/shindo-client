package me.miki.shindo.ui.animation.v2.easing

import me.miki.shindo.ui.animation.v1.EasingFunctions
import me.miki.shindo.ui.animation.v1.TimedAnimation

class EaseInOutSine(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::inOutSine)
