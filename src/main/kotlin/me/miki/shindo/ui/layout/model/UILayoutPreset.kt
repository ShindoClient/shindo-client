package me.miki.shindo.ui.layout.model

/**
 * A reusable preset that can apply a full [UILayoutState].
 *
 * themeId/accentName are optional integration points with ColorManager.
 * When null, current color theme/accent is preserved.
 */
data class UILayoutPreset(
    val id: String,
    val title: String,
    val description: String,
    val state: UILayoutState,
    val themeId: Int? = null,
    val accentName: String? = null,
    val userDefined: Boolean = false
)

