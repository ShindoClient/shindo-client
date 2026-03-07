package me.miki.shindo.addon.api.event

/**
 * Interface base para eventos. Os eventos do Shindo Client implementam estas interfaces,
 * permitindo que addons recebam eventos via @EventTarget sem depender do JAR do client.
 */
interface IEvent {
    fun isCancelled(): Boolean
    fun setCancelled(cancelled: Boolean)
}
