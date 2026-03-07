package me.miki.shindo.addon.api.event

/** Skin do jogador. */
interface IEventLocationSkin : IEvent {
    fun getPlayerInfo(): Any
    fun getSkin(): Any?
    fun setSkin(skin: Any?)
}
