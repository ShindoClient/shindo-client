package me.miki.shindo.ui.layout.interfaces

/**
 * Integration contract for optional color theme/accent application from presets.
 */
interface UILayoutAppearanceIntegration {
    fun getCurrentThemeId(): Int?
    fun getCurrentAccentName(): String?
    fun applyAppearance(themeId: Int?, accentName: String?)
}

