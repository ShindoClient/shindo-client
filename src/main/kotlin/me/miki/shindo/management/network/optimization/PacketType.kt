package me.miki.shindo.management.network.optimization

/**
 * Tipo de processamento que um pacote pode receber.
 */
enum class PacketType {
    /**
     * Pacote crítico que DEVE ser processado sequencialmente.
     * Afeta estado crítico do jogo (chunks, login, spawn, etc.)
     */
    CRITICAL,
    
    /**
     * Pacote que pode ser processado em paralelo de forma segura.
     * Não afeta estado crítico e é independente de outros pacotes.
     */
    PARALLEL_SAFE,
    
    /**
     * Pacote que requer processamento sequencial por padrão,
     * mas pode ser otimizado com cuidado.
     */
    SEQUENTIAL
}
