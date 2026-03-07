package me.miki.shindo.addon.api.event

/** Enviar chat. Cancelar para bloquear. */
interface IEventSendChat : IEvent {
    fun getMessage(): String
}
