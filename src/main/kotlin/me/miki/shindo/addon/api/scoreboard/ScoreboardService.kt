package me.miki.shindo.addon.api.scoreboard

/**
 * Serviço de leitura do scoreboard atual do jogador.
 *
 * Implementado pelo Shindo Client, exposto para addons via ServiceRegistry.
 */
interface ScoreboardService {

    /**
     * Retorna as linhas atuais do scoreboard, já normalizadas (sem códigos
     * de cor proprietários e na ordem em que aparecem na tela).
     *
     * Em cenários onde não há scoreboard disponível, deve retornar uma
     * lista vazia.
     */
    fun getCurrentLines(): List<String>
}

