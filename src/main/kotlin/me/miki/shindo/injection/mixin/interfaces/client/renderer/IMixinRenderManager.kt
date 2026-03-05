package me.miki.shindo.injection.mixin.interfaces.client.renderer

/**
 * Interface mixin para RenderManager. Usa Any para tipos do Minecraft para evitar LinkageError.
 */
interface IMixinRenderManager {
    fun getRenderPosX(): Double
    fun getRenderPosY(): Double
    fun getRenderPosZ(): Double
    fun getPlayerRenderer(): Any
}
