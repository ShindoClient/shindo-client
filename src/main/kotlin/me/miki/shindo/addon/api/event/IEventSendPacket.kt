package me.miki.shindo.addon.api.event

/** Enviar pacote. Cancelar para bloquear. */
interface IEventSendPacket : IEvent {
    fun getPacket(): Any
    fun setPacket(packet: Any)
}
