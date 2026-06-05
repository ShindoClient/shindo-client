package me.miki.shindo.management.suggestion

class MinecraftSuggestionProvider : SuggestionProvider {

    override val triggerPrefix: Char = '/'

    override fun getSuggestions(prefix: String): List<Suggestion> {
        if (prefix.isEmpty()) {
            return knownCommands.map { Suggestion(it, "Minecraft command") }
        }
        return knownCommands
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .map { Suggestion(it, "Minecraft command") }
    }

    companion object {
        private val knownCommands = listOf(
            "achievement", "ban", "ban-ip", "banlist",
            "blockdata", "clear", "clone", "debug",
            "defaultgamemode", "deop", "difficulty", "effect",
            "enchant", "entitydata", "execute", "fill",
            "gamemode", "gamerule", "give", "help",
            "kick", "kill", "list", "me",
            "op", "pardon", "pardon-ip", "particle",
            "playsound", "publish", "say", "scoreboard",
            "seed", "setblock", "setidletime", "setworldspawn",
            "spawnpoint", "spreadplayers", "statistics", "stopsound",
            "summon", "tell", "tellraw", "testfor",
            "testforblock", "testforblocks", "time", "title",
            "tp", "trigger", "weather", "whitelist",
            "worldborder", "w", "xp",
        )
    }
}
