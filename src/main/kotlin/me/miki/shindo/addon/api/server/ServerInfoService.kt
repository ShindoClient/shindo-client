package me.miki.shindo.addon.api.server

/**
 * Informações sobre o servidor atual em que o jogador está conectado.
 *
 * Implementado pelo Shindo Client e exposto para addons via ServiceRegistry.
 */
interface ServerInfoService {

    /**
     * IP bruto do servidor atual (ex.: "mc.hypixel.net"), ou "Single Player"
     * quando não estiver conectado a um servidor.
     */
    fun getServerIp(): String

    /**
     * Se o jogador está em um servidor multiplayer (não singleplayer).
     */
    fun isOnServer(): Boolean

    /**
     * Se o jogador está em qualquer instância da Hypixel.
     */
    fun isOnHypixel(): Boolean
}

