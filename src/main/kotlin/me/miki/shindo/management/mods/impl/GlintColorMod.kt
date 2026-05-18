package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.ColorUtils.getRainbow
import java.awt.Color

class GlintColorMod :
    Mod(
        TranslateText.GLINT_COLOR,
        TranslateText.GLINT_COLOR_DESCRIPTION,
        ModCategory.RENDER,
        LegacyIcon.MOD_GLINT_COLOR,
        "changeru",
    ) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private val glintType = GlintType.SYNC

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private val colorSetting: Color = Color.RED

    init {
        instance = this
    }

    val glintColor: Color
        get() {
            when (glintType) {
                GlintType.SYNC -> {
                    val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()
                    return currentColor.getInterpolateColor()
                }

                GlintType.RAINBOW -> {
                    return getRainbow(0, 25.0, 255)
                }

                GlintType.CUSTOM -> {
                    return applyAlpha(colorSetting, 255)
                }
            }
        }

    private enum class GlintType(
        private val translate: TranslateText,
    ) : PropertyEnum {
        SYNC(TranslateText.SYNC),
        RAINBOW(TranslateText.RAINBOW),
        CUSTOM(TranslateText.CUSTOM),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    companion object {
        @JvmField
        var instance: GlintColorMod? = null
    }
}
