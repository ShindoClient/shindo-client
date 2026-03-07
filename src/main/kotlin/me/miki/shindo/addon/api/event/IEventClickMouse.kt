package me.miki.shindo.addon.api.event

/**
 * Evento de clique do mouse.
 */
interface IEventClickMouse : IEvent {
    fun getButton(): Int
}
