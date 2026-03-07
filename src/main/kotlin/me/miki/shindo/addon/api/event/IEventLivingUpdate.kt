package me.miki.shindo.addon.api.event

/** Living update. */
interface IEventLivingUpdate : IEvent {
    fun getEntity(): Any
}
