package me.miki.shindo.injection.mixin.interfaces.client.renderer

import net.minecraft.client.multiplayer.WorldClient

interface IMixinRenderGlobal {
    fun getWorldClient(): WorldClient
}
