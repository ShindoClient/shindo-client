package me.miki.shindo.addon.api.event

/** Texto (tradução, etc). */
interface IEventText : IEvent {
    fun getText(): String
    fun getOutputText(): String
    fun setOutputText(text: String)
}
