package me.miki.shindo.addon.api.event

/** Rotação cabeça do jogador. */
interface IEventPlayerHeadRotation : IEvent {
    fun getYaw(): Float
    fun getPitch(): Float
}
