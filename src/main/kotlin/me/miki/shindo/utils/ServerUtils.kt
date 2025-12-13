package me.miki.shindo.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.Entity

object ServerUtils {

    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun isInTabList(entity: Entity): Boolean {
        if (isJoinServer()) {
            for (item in mc.netHandler.playerInfoMap) {
                if (item != null && item.gameProfile != null && item.gameProfile.name.contains(entity.name)) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun isJoinServer(): Boolean = !mc.isSingleplayer && mc.currentServerData != null

    @JvmStatic
    fun getServerIP(): String {
        return if (isJoinServer()) {
            mc.currentServerData.serverIP
        } else {
            "Single Player"
        }
    }

    @JvmStatic
    fun getPing(): Int {
        return if (mc.isSingleplayer || !isJoinServer()) {
            0
        } else {
            mc.currentServerData.pingToServer.toInt()
        }
    }

    @JvmStatic
    fun isHypixel(): Boolean = getServerIP().contains("hypixel")

    @JvmStatic
    fun isMinemen(): Boolean = getServerIP().contains("minemen")
}
