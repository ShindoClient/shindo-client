package me.miki.shindo.addon.api.event

/** Receber chat. Modificar via setMessage para alterar. */
interface IEventReceiveChat : IEvent {
    fun getMessage(): Any
    fun setMessage(message: Any)
}
