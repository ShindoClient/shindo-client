package me.miki.shindo.addon.api.event

/**
 * Evento de render 2D (HUD).
 */
interface IEventRender2D : IEvent {
    fun getPartialTicks(): Float
}
