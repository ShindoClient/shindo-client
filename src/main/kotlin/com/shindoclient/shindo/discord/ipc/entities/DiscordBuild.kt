package com.shindoclient.shindo.discord.ipc.entities

enum class DiscordBuild(
    val endpoint: String?,
) {
    CANARY("//canary.discordapp.com/api"),
    PTB("//ptb.discordapp.com/api"),
    STABLE("//discordapp.com/api"),
    ANY(null),
    ;

    companion object {
        fun from(endpoint: String?): DiscordBuild = values().firstOrNull { it.endpoint != null && it.endpoint == endpoint } ?: ANY
    }
}
