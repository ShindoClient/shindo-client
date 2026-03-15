package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import java.awt.Color

class ArrayListMod : HUDMod(TranslateText.ARRAY_LIST, TranslateText.ARRAY_LIST_DESCRIPTION, LegacyIcon.MOD_ARRAY_LIST) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    private val backgroundEnabled = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HUD)
    private val includeHudMods = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.RENDER)
    private val includeRenderMods = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PLAYER)
    private val includePlayerMods = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.OTHER)
    private val includeOtherMods = false

    @Property(type = PropertyType.COMBO, translate = TranslateText.MODE)
    private val modeSetting = Mode.RIGHT


    @EventTarget
    fun onRender2D(event: EventNVG?) {
        val instance = getInstance()
        val currentColor = instance.colorManager.getCurrentColor()

        val enabledMods = ArrayList<Mod>()
        var maxWidth = 0

        for (m in instance.modManager.getMods()) {
            if (!includeHudMods && m.getCategory() == ModCategory.HUD) {
                continue
            }

            if (!includeRenderMods && m.getCategory() == ModCategory.RENDER) {
                continue
            }

            if (!includePlayerMods && m.getCategory() == ModCategory.PLAYER) {
                continue
            }

            if (!includeOtherMods && m.getCategory() == ModCategory.OTHER) {
                continue
            }

            if (m.isToggled() && !m.isHide()) {
                val nameWidth: Float = this.getTextWidth(m.getName(), 8.5f, getHudFont(1))!!

                enabledMods.add(m)

                if (maxWidth < nameWidth) {
                    maxWidth = nameWidth.toInt()
                }
            }
        }

        enabledMods.sortWith(Comparator { m1: Mod?, m2: Mod? ->
            (getTextWidth(
                m2!!.getName(),
                8.5f,
                getHudFont(1)
            )!! - getTextWidth(m1!!.getName(), 8.5f, getHudFont(1))!!).toString().toInt()
        })

        var y = 0
        var colorIndex = 0
        val isRight = modeSetting == Mode.RIGHT

        for (m in enabledMods) {
            val nameWidth: Float = this.getTextWidth(m.getName(), 8.5f, getHudFont(1))!!

            if (backgroundEnabled) {
                this.drawRect(
                    (if (isRight) (maxWidth - nameWidth) else 0f),
                    y.toFloat(),
                    nameWidth + 5,
                    12f,
                    Color(0, 0, 0, 100)
                )
            }

            this.drawText(
                m.getName(), 3 + (if (isRight) (maxWidth - nameWidth) else 0f),
                y + 2.5f, 8.5f, getHudFont(1), currentColor.getInterpolateColor(colorIndex)
            )

            y += 12
            colorIndex -= 10
        }

        this.setWidth(maxWidth + 4)
        this.setHeight(y)
    }

    private enum class Mode(private val translate: TranslateText) : PropertyEnum {
        RIGHT(TranslateText.RIGHT),
        LEFT(TranslateText.LEFT);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}




