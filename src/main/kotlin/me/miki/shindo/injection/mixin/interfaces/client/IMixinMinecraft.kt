package me.miki.shindo.injection.mixin.interfaces.client

import java.io.File

/**
 * Interface mixin para Minecraft. Usa Any para tipos do Minecraft para evitar
 * LinkageError quando a interface é carregada por um classloader diferente.
 */
interface IMixinMinecraft {
    fun isRunning(): Boolean

    fun getTimer(): Any

    fun setSession(session: Any)

    fun callClickMouse()

    fun callRightClickMouse()

    fun getMcDefaultResourcePack(): Any

    fun resizeWindow(width: Int, height: Int)

    fun getRenderViewEntity(): Any

    fun getFileResourcepacks(): File

    fun getMcResourcePackRepository(): Any

    fun setMcResourcePackRepository(repo: Any)
}
