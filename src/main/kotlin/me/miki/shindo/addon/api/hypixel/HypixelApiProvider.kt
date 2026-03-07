package me.miki.shindo.addon.api.hypixel

import java.util.UUID

/**
 * Serviço de alto nível para acesso à API do Hypixel relacionado ao The Pit.
 *
 * Toda comunicação HTTP, cache e rate limit é responsabilidade do Shindo Client.
 * Addons apenas consomem estes métodos e modelos de domínio.
 */
interface HypixelApiProvider {

    /**
     * Obtém estatísticas do Pit para o jogador informado.
     *
     * Pode retornar null em caso de falha de API, player que nunca jogou ou
     * quando os dados estiverem temporariamente indisponíveis.
     */
    fun getPlayerPitStats(uuid: UUID): PitPlayerStats?

    /**
     * Lista eventos futuros/relevantes do Pit.
     *
     * A lista pode estar vazia quando não houver eventos conhecidos ou em caso
     * de fallback da API.
     */
    fun getUpcomingPitEvents(): List<PitEvent>

    /**
     * Obtém a visão do EnderChest de um jogador.
     *
     * O formato é uma coleção de itens já interpretados para renderização
     * de HUD/GUI pelos addons.
     */
    fun getEnderChest(uuid: UUID): List<PitItem>
}

