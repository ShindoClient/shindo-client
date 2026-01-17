package me.miki.shindo.utils.concurrent

/**
 * Prioridades de tarefas. Tarefas com maior prioridade são executadas primeiro.
 */
enum class TaskPriority(val value: Int) {
    /**
     * Prioridade mais alta - tarefas críticas que devem ser executadas imediatamente.
     */
    CRITICAL(100),
    
    /**
     * Prioridade alta - tarefas importantes que devem ser executadas rapidamente.
     */
    HIGH(75),
    
    /**
     * Prioridade normal - tarefas padrão.
     */
    NORMAL(50),
    
    /**
     * Prioridade baixa - tarefas que podem esperar.
     */
    LOW(25),
    
    /**
     * Prioridade mais baixa - tarefas que podem esperar muito tempo.
     */
    IDLE(0);
    
    companion object {
        /**
         * Compara duas prioridades. Retorna true se a primeira é maior que a segunda.
         */
        @JvmStatic
        fun isHigherThan(a: TaskPriority, b: TaskPriority): Boolean = a.value > b.value
    }
}
