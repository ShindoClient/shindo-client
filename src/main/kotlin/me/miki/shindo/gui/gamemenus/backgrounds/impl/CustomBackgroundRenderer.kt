package me.miki.shindo.gui.gamemenus.backgrounds.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.profile.mainmenu.impl.Background
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground
import me.miki.shindo.ui.animation.value.SimpleAnimation
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse

class CustomBackgroundRenderer : AbstractBackground() {
    private val backgroundParallaxAnimations: Array<SimpleAnimation?> = arrayOfNulls<SimpleAnimation>(2)
    override fun draw(sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager, partialTicks: Float) {
        val currentBackground: Background = instance.profileManager.backgroundManager.getCurrentBackground()!!
        val bg: CustomBackground = currentBackground as CustomBackground
        nvg.setupAndDraw {
            nvg.drawImage(
                bg.getImage(),
                -21 + backgroundParallaxAnimations[0]!!.value / 90f,
                backgroundParallaxAnimations[1]!!.value * -1 / 90f,
                sr.scaledWidth + 21f,
                sr.scaledHeight + 20f
            )
        }
    }

    override fun init() {
        for (i in backgroundParallaxAnimations.indices) {
            backgroundParallaxAnimations[i] = SimpleAnimation()
        }
    }

    override fun update(width: Float, height: Float) {
        backgroundParallaxAnimations[0]!!.setAnimation(Mouse.getX().toFloat(), 16.0)
        backgroundParallaxAnimations[1]!!.setAnimation(Mouse.getY().toFloat(), 16.0)
    }
}