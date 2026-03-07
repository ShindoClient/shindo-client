package me.miki.shindo.addon.api.event

/** Zoom FOV. */
interface IEventZoomFov : IEvent {
    fun getFov(): Float
    fun setFov(fov: Float)
}
