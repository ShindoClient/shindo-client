package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.ResourceLocation

class EventLocationCape(
    private val playerInfo: NetworkPlayerInfo,
) : Event() {
    private var cape: ResourceLocation? = null

    fun getPlayerInfo(): NetworkPlayerInfo = playerInfo

    fun getCape(): ResourceLocation? = cape

    fun setCape(cape: ResourceLocation?) {
        this.cape = cape
    }
}
