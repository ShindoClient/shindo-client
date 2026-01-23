package me.miki.shindo.management.addons.hackerdetector.utils

/**
 * Rastreador de nível de violação para checks de anti-cheat
 * 
 * Gerencia automaticamente o nível de violação baseado em checks falhados/bem-sucedidos
 * ou permite gerenciamento manual do nível
 */
class ViolationLevelTracker(
    private val failedCheckWeight: Int = 0,
    private val successfulCheckWeight: Int = 0,
    private val flagLevel: Int
) {
    
    private var violationLevel = 0
    
    /**
     * Construtor para gerenciamento manual do nível de violação
     */
    constructor(flagLevel: Int) : this(0, 0, flagLevel)
    
    /**
     * Retorna true se o jogador agora excede o nível de flag
     */
    fun isFlagging(failedCheck: Boolean): Boolean {
        return if (failedCheck) {
            onCheckFail()
        } else {
            onCheckSuccess()
            false
        }
    }
    
    private fun onCheckSuccess(): Boolean {
        subtract(successfulCheckWeight)
        if (violationLevel < 0) {
            violationLevel = 0
        }
        return false
    }
    
    private fun onCheckFail(): Boolean {
        add(failedCheckWeight)
        if (violationLevel >= flagLevel) {
            violationLevel = 0
            return true
        }
        return false
    }
    
    /**
     * Adiciona ao nível de violação (gerenciamento manual)
     */
    fun add(amount: Int) {
        violationLevel += amount
    }
    
    /**
     * Subtrai do nível de violação (gerenciamento manual)
     */
    fun subtract(amount: Int) {
        violationLevel -= amount
    }
    
    fun getViolationLevel(): Int = violationLevel
}
