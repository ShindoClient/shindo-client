package me.miki.shindo.ui.layout.integration

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.Theme
import me.miki.shindo.ui.layout.interfaces.UILayoutAppearanceIntegration

/**
 * ColorManager bridge used by layout presets.
 */
class ColorManagerLayoutAppearanceIntegration : UILayoutAppearanceIntegration {

    override fun getCurrentThemeId(): Int? {
        return try {
            Shindo.getInstance().colorManager.getTheme().getId()
        } catch (ignored: Throwable) {
            null
        }
    }

    override fun getCurrentAccentName(): String? {
        return try {
            Shindo.getInstance().colorManager.getCurrentColor().getName()
        } catch (ignored: Throwable) {
            null
        }
    }

    override fun applyAppearance(themeId: Int?, accentName: String?) {
        val colorManager = try {
            Shindo.getInstance().colorManager
        } catch (ignored: Throwable) {
            return
        }

        if (themeId != null) {
            colorManager.setTheme(Theme.getThemeById(themeId))
        }
        if (!accentName.isNullOrEmpty()) {
            colorManager.setCurrentColor(colorManager.getColorByName(accentName))
        }
    }
}

