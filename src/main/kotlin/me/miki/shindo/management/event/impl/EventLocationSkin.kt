package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.ResourceLocation

class EventLocationSkin(
    private val _playerInfo: NetworkPlayerInfo,
) : Event() {
    private var _skin: ResourceLocation? = null

    fun getPlayerInfo(): NetworkPlayerInfo = _playerInfo

    fun getSkin(): ResourceLocation? = _skin

    fun setSkin(skin: Any?) {
        _skin = skin as? ResourceLocation
    }
}
