package me.miki.shindo.gui.modmenu.category.impl.home

import me.miki.shindo.utils.mouse.MouseUtils
import kotlin.math.max
import kotlin.math.min

/**
 * Click resolution for HomeCategory interactive controls.
 */
class HomeCategoryInputController {

    enum class MusicControlAction {
        PREVIOUS,
        TOGGLE_PLAYBACK,
        NEXT
    }

    fun isPrimaryClick(mouseButton: Int): Boolean {
        return mouseButton == 0
    }

    fun isJoinButtonClicked(
        mouseX: Int,
        mouseY: Int,
        joinButtonX: Float,
        joinButtonY: Float,
        joinButtonWidth: Float = 52f,
        joinButtonHeight: Float = 18f
    ): Boolean {
        return MouseUtils.isInside(mouseX, mouseY, joinButtonX, joinButtonY, joinButtonWidth, joinButtonHeight)
    }

    fun resolveMusicControlAction(
        mouseX: Int,
        mouseY: Int,
        controlsCenterX: Float,
        controlsY: Float,
        controlSize: Float,
        controlSpacing: Float
    ): MusicControlAction? {
        if (MouseUtils.isInside(
                mouseX,
                mouseY,
                controlsCenterX - controlSpacing - controlSize / 2f,
                controlsY,
                controlSize,
                controlSize
            )
        ) {
            return MusicControlAction.PREVIOUS
        }

        if (MouseUtils.isInside(
                mouseX,
                mouseY,
                controlsCenterX - controlSize / 2f,
                controlsY,
                controlSize,
                controlSize
            )
        ) {
            return MusicControlAction.TOGGLE_PLAYBACK
        }

        if (MouseUtils.isInside(
                mouseX,
                mouseY,
                controlsCenterX + controlSpacing - controlSize / 2f,
                controlsY,
                controlSize,
                controlSize
            )
        ) {
            return MusicControlAction.NEXT
        }

        return null
    }

    fun resolveSeekProgress(
        mouseX: Int,
        mouseY: Int,
        barX: Float,
        barY: Float,
        barWidth: Float,
        barHeight: Float = 2f
    ): Float? {
        if (!MouseUtils.isInside(mouseX, mouseY, barX, barY, barWidth, barHeight)) {
            return null
        }
        val relativeX = mouseX - barX
        return max(0f, min(1f, relativeX / barWidth))
    }
}
