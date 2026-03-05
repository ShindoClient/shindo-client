package me.miki.shindo.gui.modmenu.navigation

import me.miki.shindo.gui.modmenu.style.ModMenuMotion
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.curve.SmoothStepAnimation

/**
 * Shared transition coordinator for right-side detail layers in ModMenu categories.
 *
 * It keeps opening/closing behavior consistent for category-specific overlays
 * such as module/addon settings panels.
 */
class ModMenuDetailLayerTransitionCoordinator {

    enum class State {
        IDLE,
        ENTERING,
        ACTIVE,
        LEAVING
    }

    private lateinit var slideAnimation: Animation
    private var state: State = State.IDLE

    fun reset() {
        slideAnimation = SmoothStepAnimation(ModMenuMotion.DETAILS_PANEL_ANIMATION_MS, 1.0)
        slideAnimation.setValue(1.0)
        slideAnimation.setDirection(Direction.FORWARDS)
        state = State.IDLE
    }

    fun update(onClosed: () -> Unit = {}) {
        when (state) {
            State.ENTERING -> {
                if (slideAnimation.isDone(Direction.BACKWARDS)) {
                    state = State.ACTIVE
                }
            }

            State.LEAVING -> {
                if (slideAnimation.isDone(Direction.FORWARDS)) {
                    state = State.IDLE
                    onClosed()
                }
            }

            else -> {
            }
        }
    }

    fun open() {
        if (state == State.ENTERING || state == State.ACTIVE) {
            return
        }
        state = State.ENTERING
        slideAnimation.setDirection(Direction.BACKWARDS)
    }

    fun close() {
        if (state == State.IDLE || state == State.LEAVING) {
            return
        }
        state = State.LEAVING
        slideAnimation.setDirection(Direction.FORWARDS)
    }

    fun isListInteractive(): Boolean = state == State.IDLE

    fun isDetailsInteractive(): Boolean = state == State.ACTIVE

    fun isTransitioning(): Boolean = state == State.ENTERING || state == State.LEAVING

    fun getListTranslateX(slideDistance: Float = ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE): Float {
        val distance = slideDistance.coerceAtLeast(0f)
        return -(distance - (getProgress() * distance))
    }

    fun getDetailsTranslateX(slideDistance: Float = ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE): Float {
        return getProgress() * slideDistance.coerceAtLeast(0f)
    }

    private fun getProgress(): Float {
        return slideAnimation.getValue().toFloat().coerceIn(0f, 1f)
    }
}

