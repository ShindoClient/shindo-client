package me.miki.shindo.gui.gamemenus.backgrounds

import me.miki.shindo.Shindo
import me.miki.shindo.gui.gamemenus.backgrounds.impl.AbstractBackground
import me.miki.shindo.gui.gamemenus.backgrounds.impl.CustomBackgroundRenderer
import me.miki.shindo.gui.gamemenus.backgrounds.impl.DefaultBackgroundRenderer
import me.miki.shindo.gui.gamemenus.backgrounds.impl.PanoramaBackgroundRenderer
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.profile.mainmenu.impl.Background
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground
import me.miki.shindo.management.profile.mainmenu.impl.PanoramaBackground
import net.minecraft.client.gui.ScaledResolution


class BackgroundsHandler {
    private lateinit var currentBackground: AbstractBackground
    private var lastBackground: Background? = null
    private fun selectBackground(instance: Shindo) {
        val background: Background = instance.profileManager.backgroundManager.getCurrentBackground()!!

        when (background) {
            is DefaultBackground -> currentBackground = DefaultBackgroundRenderer()
            is CustomBackground -> currentBackground = CustomBackgroundRenderer()
            is PanoramaBackground -> currentBackground = PanoramaBackgroundRenderer()
        }
        currentBackground.init()
        lastBackground = background
    }

    fun update(width: Float, height: Float) {
        currentBackground.update(width, height)
    }

    fun draw(sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager, partialTicks: Float) {
        val currentBg: Background = instance.profileManager.backgroundManager.getCurrentBackground()!!
        if (currentBg !== lastBackground) {
            selectBackground(instance)
        }
        currentBackground.draw(sr, instance, nvg, partialTicks)
    }

    init {
        selectBackground(Shindo.getInstance())
    }
}