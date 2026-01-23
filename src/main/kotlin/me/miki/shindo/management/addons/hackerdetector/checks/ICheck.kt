package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import net.minecraft.entity.player.EntityPlayer

/**
 * Interface para checks de anti-cheat
 */
interface ICheck {
    
    /**
     * Retorna o nome do cheat que este check detecta
     */
    fun getCheatName(): String
    
    /**
     * Retorna a descrição do cheat que este check detecta
     */
    fun getCheatDescription(): String
    
    /**
     * Retorna o tipo de flag (opcional, vazio por padrão)
     */
    fun getFlagType(): String = ""
    
    /**
     * Se este check deve enviar um report para o servidor
     */
    fun canSendReport(): Boolean
    
    /**
     * Executa o check no jogador e imprime uma mensagem se o jogador flagar
     */
    fun performCheck(player: EntityPlayer, data: PlayerDataSamples)
    
    /**
     * Executa o check no jogador e retorna true se o jogador falhar o check
     * Se você quiser gerenciar os níveis de violação manualmente, deve atualizá-los neste método
     */
    fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean
}
