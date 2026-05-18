package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.ResourceLocation

class EventLocationCape(
    private val _playerInfo: NetworkPlayerInfo,
) : Event() {
    private var _cape: ResourceLocation? = null

    fun getPlayerInfo(): NetworkPlayerInfo = _playerInfo

    fun getCape(): ResourceLocation? = _cape

    fun setCape(cape: Any?) {
        _cape = cape as? ResourceLocation
    }
}
