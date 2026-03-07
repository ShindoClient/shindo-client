package me.miki.shindo.addon.api.event

/**
 * Evento de render 3D (world render).
 */
interface IEventRender3D : IEvent {
    fun getPartialTicks(): Float
}
