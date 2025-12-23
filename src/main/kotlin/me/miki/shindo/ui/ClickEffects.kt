package me.miki.shindo.ui

import me.miki.shindo.Shindo
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn

class ClickEffects {
    private val effects = mutableListOf<ClickEffect>()
    private val toRemove = mutableListOf<ClickEffect>()

    fun drawClickEffects() {
        effects.forEach { effect ->
            if (effect.isDone()) {
                toRemove.add(effect)
            }
            effect.draw()
        }

        if (toRemove.isNotEmpty()) {
            effects.removeAll(toRemove)
            toRemove.clear()
        }
    }

    fun addClickEffect(mouseX: Int, mouseY: Int) {
        effects.add(ClickEffect(mouseX, mouseY))
    }

    private class ClickEffect(
        private val x: Int,
        private val y: Int
    ) {
        private val animation = EaseBackIn(650, 1.0, 0.0f)

        fun draw() {
            val instance = Shindo.getInstance()
            val nvg = instance.nanoVGManager ?: return
            val currentColor = instance.colorManager.currentColor

            nvg.setupAndDraw(Runnable {
                val progress = animation.getValueFloat()
                nvg.drawArc(
                    x.toFloat(),
                    y.toFloat(),
                    progress * 8,
                    0f,
                    360f,
                    2f,
                    ColorUtils.applyAlpha(currentColor.getInterpolateColor(0), (255 - (progress * 255)).toInt())
                )
            });
        }

        fun isDone(): Boolean = animation.isDone(Direction.FORWARDS)
    }
}