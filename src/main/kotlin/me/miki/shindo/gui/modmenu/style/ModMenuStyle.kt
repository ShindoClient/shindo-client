package me.miki.shindo.gui.modmenu.style

/**
 * Central style tokens for ModMenu shell rendering.
 *
 * This object keeps spacing/radius/sizing values in one place so the ModMenu
 * visual language stays coherent while internal categories are migrated.
 */
object ModMenuStyle {
    const val DEFAULT_WIDTH = 500
    const val DEFAULT_HEIGHT = 320

    const val ROOT_RADIUS = 12f
    const val ROOT_SHADOW_RADIUS = 12f

    const val SIDEBAR_WIDTH = 32f
    const val SIDEBAR_ITEM_SIZE = 21f
    const val SIDEBAR_ITEM_X = 5.5f
    const val SIDEBAR_TOP_Y = 34.5f
    const val SIDEBAR_ITEM_GAP = 22f
    const val SIDEBAR_MIN_ITEM_GAP = 18f
    const val SIDEBAR_BOTTOM_PADDING = 10f
    const val SIDEBAR_ICON_SIZE = 14f

    const val BRAND_X = 5f
    const val BRAND_Y = 7f
    const val BRAND_SIZE = 22f
    const val BRAND_RADIUS = 11f
    const val BRAND_ICON_X = 8f
    const val BRAND_ICON_Y = 10f
    const val BRAND_ICON_SIZE = 16f

    const val CATEGORY_TITLE_X = 32f
    const val CATEGORY_TITLE_Y = 10f
    const val CATEGORY_TITLE_SIZE = 15f
    const val CATEGORY_TITLE_SIZE_COMPACT = 14f
    const val HEADER_SEPARATOR_Y = 31f
    const val HEADER_SEPARATOR_SIDE_INSET = 10f

    const val CONTENT_MIN_TOP_WITH_TITLE = 31f
    const val CONTENT_ENTRY_SLIDE = 50f

    const val HUD_BUTTON_SIZE = 21f
    const val HUD_BUTTON_X = 5.5f
    const val HUD_BUTTON_BOTTOM_MARGIN = 30f
}
