package me.miki.shindo.management.mods.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation

class MemoryUsageMod : SimpleHUDMod(TranslateText.MEMORY_USAGE, TranslateText.MEMORY_USAGE_DESCRIPTION, Shinconic.MOD_MEMORY_USAGE) {
    private val circleAnimation = SimpleAnimation()

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.SIMPLE

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        val nvg = getInstance().nanoVGManager

        if (design == Design.SIMPLE) {
            this.draw()
        } else {
            drawNanoVG(nvg)
        }
    }

    private fun drawNanoVG(nvg: NanoVGManager?) {
        circleAnimation.setAnimation(((this.usingMemory / 100f) * 360), 16)

        this.drawBackground(54f, 60f)
        this.drawCenteredText("Memory", 54 / 2f, 6f, 9f, getHudFont(1))
        this.drawCenteredText(this.usingMemory.toString() + "%", 54 / 2f, 32f, 9f, getHudFont(1))

        this.drawArc(27f, 35.5f, 16.5f, -90f, 360f, 1.6f, this.getFontColor(120))
        this.drawArc(27f, 35.5f, 16.5f, -90f, circleAnimation.getValue() - 90, 1.6f, this.getFontColor())

        this.setWidth(54)
        this.setHeight(60)
    }

    override fun getText(): String = "Mem: " + this.usingMemory + "%"

    override fun getIcon(): String? = if (iconSetting) Lucide.SERVER else null

    private val usingMemory: Long
        get() {
            val runtime = Runtime.getRuntime()

            return (runtime.totalMemory() - runtime.freeMemory()) * 100L / runtime.maxMemory()
        }

    private enum class Design(
        private val translate: TranslateText,
    ) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}
