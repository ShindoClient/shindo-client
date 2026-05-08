package me.miki.shindo.gui.modmenu.v2.navigation

import me.miki.shindo.gui.modmenu.v2.style.ModMenuMotion
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.SmoothStepAnimation
import kotlin.math.max

/**
 * Unified slide transition coordinator for ModMenu overlay layers.
 *
 * Replaces:
 * - [ModMenuDetailLayerTransitionCoordinator]
 * - [SettingSceneTransitionCoordinator]
 * - [LayoutSceneStateCoordinator]
 *
 * State model: IDLE → ENTERING → ACTIVE → LEAVING → IDLE
 */
class ModMenuSlideTransitionCoordinator {

    enum class State {
        IDLE,
        ENTERING,
        ACTIVE,
        LEAVING
    }

    private lateinit var anim: Animation
    private var state: State = State.IDLE
    private var tag: Any? = null

    fun init() {
        anim = SmoothStepAnimation(ModMenuMotion.DETAILS_PANEL_ANIMATION_MS, 1.0)
        anim.setValue(1.0)
        state = State.IDLE
        tag = null
    }

    fun reset() {
        if (!::anim.isInitialized) {
            init()
            return
        }
        anim.setValue(1.0)
        state = State.IDLE
        tag = null
    }

    fun update(onComplete: () -> Unit = {}) {
        when (state) {
            State.ENTERING -> if (anim.isDone(Direction.BACKWARDS)) state = State.ACTIVE
            State.LEAVING -> {
                if (anim.isDone(Direction.FORWARDS)) {
                    state = State.IDLE
                    tag = null
                    onComplete()
                }
            }
            else -> {}
        }
    }

    fun open(extra: Any? = null) {
        tag = extra
        state = State.ENTERING
        anim.setDirection(Direction.BACKWARDS)
    }

    fun close() {
        if (state == State.IDLE) return
        state = State.LEAVING
        anim.setDirection(Direction.FORWARDS)
    }

    fun getState(): State = state

    fun isInteractive(): Boolean = state == State.IDLE || state == State.ACTIVE
    fun isListInteractive(): Boolean =state == State.IDLE

    fun isSceneInteractive(): Boolean = state == State.ACTIVE
    fun isActiveSceneOpen(): Boolean = state == State.ACTIVE
    fun isActive(): Boolean = state == State.ACTIVE
    fun isTransitioning(): Boolean = state == State.ENTERING || state == State.LEAVING
    fun isSceneVisible(): Boolean = state != State.IDLE

    fun getTag(): Any? = tag

    fun getActiveScene(): Any? = tag

    fun getProgress(): Float = anim.getValueFloat().coerceIn(0f, 1f)

    fun getSlideOffset(slideDistance: Float): Float = getProgress() * max(0f, slideDistance)

    fun getEnterTranslateX(slideDistance: Float): Float {
        val dist = max(0f, slideDistance)
        return -(dist - getProgress() * dist)
    }

    fun getLeaveTranslateX(slideDistance: Float): Float = getProgress() * slideDistance

    fun getListTranslateX(contentWidth: Float): Float = getEnterTranslateX(contentWidth)
    fun getSceneTranslateX(contentWidth: Float): Float = getSlideOffset(contentWidth)
}