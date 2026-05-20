package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.Render3DUtils
import me.miki.shindo.utils.TargetUtils.target
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

class TargetIndicatorMod :
    Mod(
        TranslateText.TARGET_INDICATOR,
        TranslateText.TARGET_INDICATOR_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_TARGET_INDICATOR,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    private val customColorSetting = false

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private val colorSetting: Color = Color.RED

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()

        if (target != null && target != mc.thePlayer) {
            Render3DUtils.drawTargetIndicator(
                target!!,
                0.67,
                if (customColorSetting) applyAlpha(colorSetting, 255) else currentColor.getInterpolateColor(),
            )
            GlStateManager.enableBlend()
        }
    }
}
