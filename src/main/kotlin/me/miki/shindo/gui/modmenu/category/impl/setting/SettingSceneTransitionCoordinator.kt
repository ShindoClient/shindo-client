package me.miki.shindo.gui.modmenu.category.impl.setting

import me.miki.shindo.gui.modmenu.style.ModMenuMotion
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.curve.SmoothStepAnimation
import kotlin.math.max

/**
 * Coordinates transitions between the settings scene index and the active scene content.
 *
 * State model:
 * - [State.IDLE]: index list visible and interactive;
 * - [State.ENTERING]: index list leaving while scene enters;
 * - [State.ACTIVE]: scene visible and interactive;
 * - [State.LEAVING]: scene leaving while index list returns.
 */
class SettingSceneTransitionCoordinator {

    enum class State {
        IDLE,
        ENTERING,
        ACTIVE,
        LEAVING
    }

    private lateinit var slideAnimation: Animation
    private var state: State = State.IDLE
    private var activeScene: SettingScene? = null

    fun reset() {
        slideAnimation = SmoothStepAnimation(ModMenuMotion.DETAILS_PANEL_ANIMATION_MS, 1.0)
        slideAnimation.setValue(1.0)
        slideAnimation.setDirection(Direction.FORWARDS)
        state = State.IDLE
        activeScene = null
    }

    fun update() {
        when (state) {
            State.ENTERING -> {
                if (slideAnimation.isDone(Direction.BACKWARDS)) {
                    state = State.ACTIVE
                }
            }

            State.LEAVING -> {
                if (slideAnimation.isDone(Direction.FORWARDS)) {
                    state = State.IDLE
                    activeScene = null
                }
            }

            else -> {
            }
        }
    }

    fun open(scene: SettingScene) {
        activeScene = scene
        state = State.ENTERING
        slideAnimation.setDirection(Direction.BACKWARDS)
    }

    fun close() {
        if (state == State.IDLE) {
            return
        }
        state = State.LEAVING
        slideAnimation.setDirection(Direction.FORWARDS)
    }

    fun getState(): State {
        return state
    }

    fun isListInteractive(): Boolean {
        return state == State.IDLE
    }

    fun isSceneInteractive(): Boolean {
        return state == State.ACTIVE
    }

    fun isSceneVisible(): Boolean {
        return state != State.IDLE && activeScene != null
    }

    fun isTransitioning(): Boolean {
        return state == State.ENTERING || state == State.LEAVING
    }

    fun getActiveScene(): SettingScene? {
        return activeScene
    }

    fun getListTranslateX(contentWidth: Float): Float {
        val slideDistance = resolveSlideDistance(contentWidth)
        val slide = getSceneTranslateForDistance(slideDistance)
        return -(slideDistance - slide)
    }

    fun getSceneTranslateX(contentWidth: Float): Float {
        val slideDistance = resolveSlideDistance(contentWidth)
        return getSceneTranslateForDistance(slideDistance)
    }

    private fun getSceneTranslateForDistance(slideDistance: Float): Float {
        return (slideAnimation.getValue() * max(0f, slideDistance)).toFloat()
    }

    private fun resolveSlideDistance(contentWidth: Float): Float {
        return max(ModMenuMotion.SETTINGS_SCENE_SLIDE_MIN, contentWidth + ModMenuMotion.SETTINGS_SCENE_SLIDE_EXTRA)
    }
}
