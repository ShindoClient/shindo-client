package me.miki.shindo.addon.api.event

/**
 * Evento de entrada em servidor.
 */
interface IEventJoinServer : IEvent {
    fun getIp(): String
}
