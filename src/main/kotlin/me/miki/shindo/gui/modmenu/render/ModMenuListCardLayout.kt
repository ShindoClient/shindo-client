package me.miki.shindo.gui.modmenu.render

import me.miki.shindo.utils.mouse.MouseUtils

/**
 * Shared geometry/hitbox model for ModMenu list-card controls.
 */
object ModMenuListCardLayout {

    data class ControlLayout(
        val settingsX: Float,
        val settingsY: Float,
        val settingsSize: Float,
        val toggleX: Float,
        val toggleY: Float,
        val toggleWidth: Float,
        val toggleHeight: Float
    ) {
        fun isSettingsHit(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, settingsX, settingsY, settingsSize, settingsSize)
        }

        fun isToggleHit(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, toggleX, toggleY, toggleWidth, toggleHeight)
        }

        fun isBodyHit(mouseX: Int, mouseY: Int, cardX: Float, cardY: Float, cardWidth: Float, cardHeight: Float): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight) &&
                    !isSettingsHit(mouseX, mouseY) &&
                    !isToggleHit(mouseX, mouseY)
        }

        fun withOffset(yOffset: Float): ControlLayout {
            if (yOffset == 0f) {
                return this
            }
            return copy(settingsY = settingsY + yOffset, toggleY = toggleY + yOffset)
        }
    }

    fun build(
        cardX: Float,
        cardY: Float,
        cardWidth: Float,
        cardHeight: Float,
        settingsSize: Float,
        settingsPaddingFromRight: Float,
        toggleWidth: Float,
        toggleHeight: Float,
        settingsGap: Float
    ): ControlLayout {
        val toggleX = cardX + cardWidth - settingsPaddingFromRight - toggleWidth
        val toggleY = cardY + (cardHeight - toggleHeight) / 2f
        val settingsX = toggleX - settingsGap - settingsSize
        val settingsY = cardY + (cardHeight - settingsSize) / 2f
        return ControlLayout(
            settingsX = settingsX,
            settingsY = settingsY,
            settingsSize = settingsSize,
            toggleX = toggleX,
            toggleY = toggleY,
            toggleWidth = toggleWidth,
            toggleHeight = toggleHeight
        )
    }
}
