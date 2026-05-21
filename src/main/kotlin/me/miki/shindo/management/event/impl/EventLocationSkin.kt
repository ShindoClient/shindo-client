package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.ResourceLocation

class EventLocationSkin(
    private val playerInfo: NetworkPlayerInfo,
) : Event() {
    private var skin: ResourceLocation? = null

    fun getPlayerInfo(): NetworkPlayerInfo = playerInfo

    fun getSkin(): ResourceLocation? = skin

    fun setSkin(skin: ResourceLocation) {
        this.skin = skin
    }
}
