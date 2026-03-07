package me.miki.shindo.management.mods.impl.rearview

import me.miki.shindo.injection.interfaces.IMixinRenderGlobal
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderGlobal

class RenderGlobalHelper {
    var mc: Minecraft
    var rg: RenderGlobal?
    var orig: RenderGlobal?
    var fancy_graphics: Boolean = false
    var ambient_occlusion: Int = 0

    init {
        mc = Minecraft.getMinecraft()
        rg = RenderGlobal(mc)
        orig = null
    }

    val settings: Unit
        get() {
            fancy_graphics = mc.gameSettings.fancyGraphics
            ambient_occlusion = mc.gameSettings.ambientOcclusion
        }

    fun settingsChanged(): Boolean {
        return fancy_graphics != mc.gameSettings.fancyGraphics ||
                ambient_occlusion != mc.gameSettings.ambientOcclusion
    }

    fun switchTo() {
        if (orig == null) orig = mc.renderGlobal
        val origWorld = (orig as IMixinRenderGlobal).worldClient as net.minecraft.client.multiplayer.WorldClient
        val rgWorld = (rg as IMixinRenderGlobal).worldClient as net.minecraft.client.multiplayer.WorldClient
        if (origWorld !== rgWorld) {
            rg!!.setWorldAndLoadRenderers(origWorld)
            this.settings
        } else if (settingsChanged()) {
            rg!!.loadRenderers()
            this.settings
        }
        mc.renderGlobal = rg
    }

    fun switchFrom() {
        if (orig != null) {
            mc.renderGlobal = orig
        }
        orig = null
    }
}