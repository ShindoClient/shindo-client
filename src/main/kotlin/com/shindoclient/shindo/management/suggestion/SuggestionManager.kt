package com.shindoclient.shindo.management.suggestion

class SuggestionManager {

    private val providers = mutableListOf<SuggestionProvider>()
    private var currentProvider: SuggestionProvider? = null
    private var suggestions = emptyList<Suggestion>()
    private var currentPrefix = ""
    var selectedIndex = 0
        private set

    val currentSuggestions: List<Suggestion> get() = suggestions
    val hasSuggestions: Boolean get() = suggestions.isNotEmpty()
    val selectedSuggestion: Suggestion? get() = suggestions.getOrNull(selectedIndex)
    val currentIndicator: Char? get() = currentProvider?.triggerPrefix

    init {
        addProvider(ClientSuggestionProvider())
        addProvider(MinecraftSuggestionProvider())
    }

    fun addProvider(provider: SuggestionProvider) {
        providers.add(provider)
    }

    fun update(text: String) {
        if (text.isEmpty()) {
            clear()
            return
        }

        val indicator = text[0]
        val provider = providers.firstOrNull { it.triggerPrefix == indicator }

        if (provider == null) {
            clear()
            return
        }

        val rest = text.substring(1)
        val prefix = rest.split(" ").firstOrNull() ?: ""

        if (currentProvider !== provider || prefix != currentPrefix) {
            currentProvider = provider
            currentPrefix = prefix
            suggestions = provider.getSuggestions(prefix)
            selectedIndex = 0
        }
    }

    fun moveUp() {
        if (suggestions.isEmpty()) return
        selectedIndex = if (selectedIndex <= 0) suggestions.size - 1 else selectedIndex - 1
    }

    fun moveDown() {
        if (suggestions.isEmpty()) return
        selectedIndex = if (selectedIndex >= suggestions.size - 1) 0 else selectedIndex + 1
    }

    fun acceptSelected(): String? {
        val suggestion = suggestions.getOrNull(selectedIndex) ?: return null
        val indicator = currentProvider?.triggerPrefix ?: return null
        return "$indicator${suggestion.text} "
    }

    fun clear() {
        currentProvider = null
        suggestions = emptyList()
        currentPrefix = ""
        selectedIndex = 0
    }
}
