package me.miki.shindo.utils.mouse

import me.miki.shindo.ui.animation.value.SimpleAnimation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min

class Scroll {

    private val minScroll = 0f
    private val scrollAnimation = SimpleAnimation(0.0f)
    var maxScroll = Float.MAX_VALUE
    private var rawScroll = 0f

    fun onScroll() {
        onScroll(2)
    }

    fun onAnimation() {
        onAnimation(14)
    }

    private fun onScroll(scrollSpeed: Int) {
        val dWheel = Mouse.getDWheel()
        rawScroll += if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            dWheel.toFloat() / scrollSpeed
        } else {
            dWheel.toFloat() / (scrollSpeed * 2)
        }
        rawScroll = max(min(minScroll, rawScroll), -maxScroll)
    }

    fun onKey(keyCode: Int) {
        var amount = 0
        if (keyCode == Keyboard.KEY_DOWN) {
            amount = -30
        }
        if (keyCode == Keyboard.KEY_UP) {
            amount = 30
        }
        rawScroll += amount
        rawScroll = max(min(minScroll, rawScroll), -maxScroll)
    }

    fun manualScroll(amount: Int) {
        rawScroll += amount
        rawScroll = max(min(minScroll, rawScroll), -maxScroll)
    }

    private fun onAnimation(animationSpeed: Int) {
        scrollAnimation.setAnimation(rawScroll, animationSpeed.toDouble())
    }

    fun getValue(): Float = scrollAnimation.value

    fun setScrollPosition(scroll: Float) {
        rawScroll = scroll
        scrollAnimation.value = scroll
    }

    fun reset() {
        rawScroll = minScroll
    }

    fun resetAll() {
        rawScroll = minScroll
        scrollAnimation.value = minScroll
    }
}
