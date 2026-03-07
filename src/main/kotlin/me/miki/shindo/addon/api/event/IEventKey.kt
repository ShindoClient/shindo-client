package me.miki.shindo.addon.api.event

/**
 * Evento de tecla pressionada.
 */
interface IEventKey : IEvent {
    fun getKeyCode(): Int
}
