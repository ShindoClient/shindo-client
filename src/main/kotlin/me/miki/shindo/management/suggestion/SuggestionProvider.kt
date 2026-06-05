package me.miki.shindo.management.suggestion

interface SuggestionProvider {

    val triggerPrefix: Char

    fun getSuggestions(prefix: String): List<Suggestion>
}
