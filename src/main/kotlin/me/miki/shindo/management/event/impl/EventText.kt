package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventText(
    private val text: String,
) : Event() {
    private var outputText: String = text

    fun getText(): String = text

    fun getOutputText(): String = outputText

    fun setOutputText(text: String) {
        this.outputText = text
    }

    fun replace(
        src: String,
        target: String,
    ): String {
        this.outputText = text.replace(src, target)
        return outputText
    }
}
