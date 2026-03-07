package me.miki.shindo.addon.api.event

/**
 * Evento de scroll do mouse.
 */
interface IEventScrollMouse : IEvent {
    fun getAmount(): Int
}
