package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventText(
    private val _text: String,
) : Event() {
    private var _outputText: String = _text

    fun getText(): String = _text

    fun getOutputText(): String = _outputText

    fun setOutputText(text: String) {
        _outputText = text
    }

    fun replace(
        src: String,
        target: String,
    ): String {
        _outputText = _text.replace(src, target)
        return _outputText
    }
}
