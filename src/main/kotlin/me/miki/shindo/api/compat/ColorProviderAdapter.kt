package me.miki.shindo.api.compat

import me.miki.shindo.Shindo
import me.miki.client_api.color.IColorProvider
import me.miki.client_api.render.AddonColor
import java.awt.Color

class ColorProviderAdapter : IColorProvider {

    private val colorManager get() = Shindo.getInstance().colorManager
    private val theme get() = colorManager.getTheme()

    override fun getAccentColor(): AddonColor {
        val c = colorManager.getCurrentColor().getColor1()
        return toAddonColor(c)
    }

    override fun getAccentColorSecondary(): AddonColor {
        val c = colorManager.getCurrentColor().getColor2()
        return toAddonColor(c)
    }

    override fun getAccentInterpolated(index: Int): AddonColor {
        val c = colorManager.getCurrentColor().getInterpolateColor(index)
        return toAddonColor(c)
    }

    override fun getThemeId(): Int = theme.getId()

    override fun getThemeDarkBackground(): AddonColor = toAddonColor(theme.getDarkBackgroundColor())

    override fun getThemeNormalBackground(): AddonColor = toAddonColor(theme.getNormalBackgroundColor())

    override fun getThemeDarkFontColor(): AddonColor = toAddonColor(theme.getDarkFontColor())

    override fun getThemeNormalFontColor(): AddonColor = toAddonColor(theme.getNormalFontColor())

    private fun toAddonColor(c: Color): AddonColor =
        AddonColor(c.red, c.green, c.blue, c.alpha)
}
