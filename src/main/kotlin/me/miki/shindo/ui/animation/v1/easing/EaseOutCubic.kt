package me.miki.shindo.ui.animation.v1.easing

import me.miki.shindo.ui.animation.v1.EasingFunctions
import me.miki.shindo.ui.animation.v1.TimedAnimation

class EaseOutCubic(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::outCubic)
