package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventMotionUpdate
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.Render3DUtils
import net.minecraft.util.Vec3
import java.awt.Color

class BreadcrumbsMod : Mod(
    TranslateText.BREADCRUMBS,
    TranslateText.BREADCRUMBS_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_BREADCRUMBS,
    "playertrails"
) {
    private val path: MutableList<Vec3> = ArrayList<Vec3>()

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    private val customColor = false

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private val trailColor: Color = Color.RED

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.TIMEOUT)
    private val timeoutEnabled = true

    @Property(type = PropertyType.NUMBER, translate = TranslateText.TIME, min = 1.0, max = 15.00, step = 1.0)
    private val timeoutTicks = 15.0

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        val currentColor = getInstance().colorManager.currentColor

        Render3DUtils.renderBreadCrumbs(
            path.toList(),
            if (customColor) applyAlpha(trailColor, 255) else currentColor.interpolateColor
        )
    }

    @EventTarget
    fun onMotionUpdate(event: EventMotionUpdate?) {
        if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
            path.add(Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ))
        }

        if (timeoutEnabled) {
            val limit = timeoutTicks.toInt()
            while (path.size > limit) {
                path.removeAt(0)
            }
        }
    }
}




