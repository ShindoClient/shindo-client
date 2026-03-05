package me.miki.shindo.gui.modmenu.style

/**
 * Shared style tokens for the settings overlay used inside ModMenu categories.
 *
 * Module and Addon categories use the same overlay shell; only SettingsPanel
 * internals differ. Keeping these values centralized avoids drift.
 */
object ModMenuSettingsOverlayStyle {
    const val PANEL_MARGIN = 15f
    const val PANEL_RADIUS = 12f
    const val PANEL_INNER_RADIUS = 11f
    const val HEADER_HEIGHT = 38f
    const val HEADER_HIGHLIGHT_HEIGHT = 34f
    const val HEADER_ICON_SIZE = 13f
    const val HEADER_TITLE_X = 27f
    const val HEADER_BACK_X = 10f

    const val RESET_ICON_SIZE = 16f
    const val RESET_ICON_INSET_X = 26f
    const val HEADER_ACTION_HITBOX = 20f

    const val CONTENT_INSET_X = 10f
    const val CONTENT_TOP_GAP = 8f
    const val CONTENT_BOTTOM_GAP = 16f
    const val SCISSOR_INSET_X = 5f
}
