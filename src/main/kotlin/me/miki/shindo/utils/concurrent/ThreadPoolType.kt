package me.miki.shindo.utils.concurrent

/**
 * Tipos de thread pools especializados para diferentes tipos de operações.
 * Cada tipo é otimizado para seu caso de uso específico.
 */
enum class ThreadPoolType {
    /**
     * Pool para operações I/O (leitura/escrita de arquivos, downloads, etc).
     * Muitas threads, pois I/O é bloqueante.
     */
    IO,
    
    /**
     * Pool para operações CPU-intensivas (processamento, cálculos, etc).
     * Poucas threads (geralmente número de cores).
     */
    CPU,
    
    /**
     * Pool para operações de rede (HTTP requests, WebSocket, etc).
     * Muitas threads, pois network é bloqueante.
     */
    NETWORK,
    
    /**
     * Pool para tarefas agendadas (scheduled tasks, timers, etc).
     * Threads suficientes para múltiplas tarefas agendadas.
     */
    SCHEDULED,
    
    /**
     * Pool genérico para tarefas que não se encaixam em outras categorias.
     */
    GENERAL
}
