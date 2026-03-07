package me.miki.shindo.addon.api.event

/** Cape do jogador. */
interface IEventLocationCape : IEvent {
    fun getPlayerInfo(): Any
    fun getCape(): Any?
    fun setCape(cape: Any?)
}
