package me.miki.shindo.addon.api.event

/** Receber pacote. Cancelar para bloquear. */
interface IEventReceivePacket : IEvent {
    fun getPacket(): Any
    fun setPacket(packet: Any)
}
