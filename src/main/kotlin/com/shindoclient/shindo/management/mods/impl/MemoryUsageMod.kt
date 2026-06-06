package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.extensions.ui.animation.setAnimation
import com.shindoclient.shindo.Shindo.Companion.getInstance
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.NanoVGManager
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation

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
