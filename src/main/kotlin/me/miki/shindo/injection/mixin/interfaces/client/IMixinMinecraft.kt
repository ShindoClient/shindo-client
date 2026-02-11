package me.miki.shindo.injection.mixin.interfaces.client

import net.minecraft.client.resources.DefaultResourcePack
import net.minecraft.client.resources.ResourcePackRepository
import net.minecraft.entity.Entity
import net.minecraft.util.Session
import net.minecraft.util.Timer
import java.io.File

interface IMixinMinecraft {
    fun isRunning(): Boolean

    fun getTimer(): Timer

    fun setSession(session: Session)

    fun callClickMouse()

    fun callRightClickMouse()

    fun getMcDefaultResourcePack(): DefaultResourcePack

    fun resizeWindow(width: Int, height: Int)

    fun getRenderViewEntity(): Entity

    fun getFileResourcepacks(): File

    fun getMcResourcePackRepository(): ResourcePackRepository

    fun setMcResourcePackRepository(repo: ResourcePackRepository)
}
