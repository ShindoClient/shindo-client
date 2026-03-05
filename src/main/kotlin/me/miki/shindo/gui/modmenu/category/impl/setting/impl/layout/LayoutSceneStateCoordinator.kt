package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.style.ModMenuMotion
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.curve.SmoothStepAnimation

/**
 * Tracks active layout sub-scene and transition state.
 */
class LayoutSceneStateCoordinator {

    private lateinit var sceneTransition: Animation
    private var activeController: LayoutAreaController? = null

    fun init() {
        sceneTransition = SmoothStepAnimation(ModMenuMotion.DETAILS_PANEL_ANIMATION_MS, 1.0)
        sceneTransition.setValue(1.0)
        activeController = null
    }

    fun getTransition(): Animation {
        return sceneTransition
    }

    fun getProgress(): Float {
        return sceneTransition.getValueFloat().coerceIn(0f, 1f)
    }

    fun getSlideOffset(baseWidth: Float): Float {
        return getProgress() * baseWidth
    }

    fun onFrame() {
        if (sceneTransition.isDone(Direction.FORWARDS)) {
            activeController = null
        }
    }

    fun open(controller: LayoutAreaController) {
        activeController = controller
        sceneTransition.setDirection(Direction.BACKWARDS)
    }

    fun close() {
        sceneTransition.setDirection(Direction.FORWARDS)
    }

    fun isSubSceneOpen(): Boolean {
        return activeController != null
    }

    fun isChildInteractive(): Boolean {
        return activeController != null && sceneTransition.isDone(Direction.BACKWARDS)
    }

    fun getActiveController(): LayoutAreaController? {
        return activeController
    }

    fun getActiveScene(): LayoutAreaScene? {
        return activeController?.scene
    }
}
