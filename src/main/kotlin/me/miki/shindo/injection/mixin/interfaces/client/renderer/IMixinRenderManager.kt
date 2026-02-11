package me.miki.shindo.injection.mixin.interfaces.client.renderer

import net.minecraft.client.renderer.entity.RenderPlayer

interface IMixinRenderManager {
    fun getRenderPosX(): Double

    fun getRenderPosY(): Double

    fun getRenderPosZ(): Double

    fun getPlayerRenderer(): RenderPlayer
}
