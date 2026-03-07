package me.miki.shindo.addon.api.event

/** TNT. */
interface IEventRenderTNT : IEvent {
    fun getTntRenderer(): Any
    fun getEntity(): Any
    fun getX(): Double
    fun getY(): Double
    fun getZ(): Double
    fun getEntityYaw(): Float
    fun getPartialTicks(): Float
}
