package me.miki.shindo.addon.api.event

/** Living entity render. */
interface IEventRendererLivingEntity : IEvent {
    fun getRenderer(): Any
    fun getEntity(): Any
    fun getX(): Double
    fun getY(): Double
    fun getZ(): Double
}
