package me.miki.shindo.addon.api.event

/** Entity entrou no mundo. */
interface IEventEntityJoinWorld : IEvent {
    fun getEntity(): Any
}
