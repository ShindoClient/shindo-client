package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventRenderItemInFirstPerson
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.renderer.GlStateManager

class CustomHeldItemsMod : Mod(
    TranslateText.CUSTOM_HELD_ITEMS,
    TranslateText.CUSTOM_HELD_ITEMS_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_CUSTOM_HELD_ITEMS
) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.X,
        category = "Offset",
        min = -1.0,
        max = 1.0,
        current = 0.75
    )
    private val xSetting = 0.75

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.Y,
        category = "Offset",
        min = -1.0,
        max = 1.0,
        current = -0.15
    )
    private val ySetting = -0.15

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.Z,
        category = "Offset",
        min = -1.0,
        max = 1.0,
        current = -1.0
    )
    private val zSetting = -1.0

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.X_SCALE,
        category = "Scale",
        min = 0.0,
        max = 1.0,
        current = 1.0
    )
    private val xScaleSetting = 1.0

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.Y_SCALE,
        category = "Scale",
        min = 0.0,
        max = 1.0,
        current = 1.0
    )
    private val yScaleSetting = 1.0

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.Z_SCALE,
        category = "Scale",
        min = 0.0,
        max = 1.0,
        current = 1.0
    )
    private val zScaleSetting = 1.0

    @EventTarget
    fun onRenderItemInFirstPerson(event: EventRenderItemInFirstPerson?) {
        GlStateManager.translate(xSetting.toFloat(), ySetting.toFloat(), zSetting.toFloat())
        GlStateManager.scale(xScaleSetting.toFloat(), yScaleSetting.toFloat(), zScaleSetting.toFloat())
    }
}




