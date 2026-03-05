package me.miki.shindo.injection.mixin.interfaces.client.renderer

/**
 * Interface mixin para RenderGlobal. Usa Any para tipos do Minecraft para evitar LinkageError.
 */
interface IMixinRenderGlobal {
    fun getWorldClient(): Any
}
