package me.miki.shindo.gui.gamemenus.backgrounds.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import net.minecraft.client.gui.ScaledResolution


open class AbstractBackground {
    open fun init() {}
    open fun update(width: Float, height: Float) {}
    open fun draw(sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager, partialTicks: Float) {}
}