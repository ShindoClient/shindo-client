package me.miki.shindo.addon.api.event

/** FOV. */
interface IEventFovUpdate : IEvent {
    fun getFov(): Float
    fun setFov(fov: Float)
    fun getEntity(): Any
}
