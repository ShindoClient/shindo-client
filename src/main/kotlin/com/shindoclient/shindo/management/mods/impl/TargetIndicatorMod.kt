package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventRender3D
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.ColorUtils.applyAlpha
import com.shindoclient.shindo.utils.Render3DUtils
import com.shindoclient.shindo.utils.TargetUtils.target
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
