package me.miki.shindo.addon.api.event

/** Dano em entity. */
interface IEventDamageEntity : IEvent {
    fun getEntity(): Any
}
