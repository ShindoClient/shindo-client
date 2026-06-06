package com.shindoclient.shindo.management.suggestion

import com.shindoclient.shindo.Shindo

class ClientSuggestionProvider : SuggestionProvider {

    override val triggerPrefix: Char = '$'

    override fun getSuggestions(prefix: String): List<Suggestion> {
        if (prefix.isEmpty()) {
            return Shindo.getInstance().getCommandManager().getCommands().map {
                Suggestion(it.getPrefix(), "Shindo command")
            }
        }
        return Shindo.getInstance().getCommandManager().getCommandsStartingWith(prefix).map {
            Suggestion(it.getPrefix(), "Shindo command")
        }
    }
}
