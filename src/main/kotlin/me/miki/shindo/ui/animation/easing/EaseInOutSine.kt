package me.miki.shindo.ui.animation.easing

import me.miki.shindo.ui.animation.EasingFunctions
import me.miki.shindo.ui.animation.TimedAnimation

class EaseInOutSine(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::inOutSine)
