package me.miki.shindo.addon.api.event

/** Gamma/brightness. */
interface IEventGamma : IEvent {
    fun getGamma(): Float
    fun setGamma(gamma: Float)
}
