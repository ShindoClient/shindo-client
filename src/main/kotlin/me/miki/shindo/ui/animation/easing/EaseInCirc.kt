package me.miki.shindo.ui.animation.easing

import me.miki.shindo.ui.animation.EasingFunctions
import me.miki.shindo.ui.animation.TimedAnimation

class EaseInCirc(ms: Int, endPoint: Double) : TimedAnimation(ms, endPoint, EasingFunctions::inCirc)
