package me.miki.shindo.addon.api.event

/** Hitbox entities. */
interface IEventRenderHitbox : IEvent {
    fun getEntity(): Any
    fun getX(): Double
    fun getY(): Double
    fun getZ(): Double
    fun getEntityYaw(): Float
    fun getPartialTicks(): Float
}
