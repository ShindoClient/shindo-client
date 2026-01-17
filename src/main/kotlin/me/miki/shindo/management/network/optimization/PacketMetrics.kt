package me.miki.shindo.management.network.optimization

import java.util.concurrent.atomic.AtomicLong

/**
 * Métricas de processamento de pacotes.
 * Rastreia estatísticas sobre o processamento de pacotes para análise e otimização.
 */
data class PacketMetrics(
    /**
     * Total de pacotes processados.
     */
    val totalProcessed: Long = 0,
    
    /**
     * Pacotes processados em paralelo.
     */
    val parallelProcessed: Long = 0,
    
    /**
     * Pacotes processados sequencialmente.
     */
    val sequentialProcessed: Long = 0,
    
    /**
     * Pacotes críticos processados.
     */
    val criticalProcessed: Long = 0,
    
    /**
     * Erros durante processamento paralelo.
     */
    val parallelErrors: Long = 0,
    
    /**
     * Erros durante processamento sequencial.
     */
    val sequentialErrors: Long = 0,
    
    /**
     * Tempo total gasto em processamento paralelo (nanosegundos).
     */
    val parallelTimeNs: Long = 0,
    
    /**
     * Tempo total gasto em processamento sequencial (nanosegundos).
     */
    val sequentialTimeNs: Long = 0
) {
    /**
     * Taxa de pacotes processados em paralelo (0.0 a 1.0).
     */
    val parallelRate: Double
        get() = if (totalProcessed > 0) parallelProcessed.toDouble() / totalProcessed else 0.0
    
    /**
     * Taxa de erros em processamento paralelo (0.0 a 1.0).
     */
    val parallelErrorRate: Double
        get() = if (parallelProcessed > 0) parallelErrors.toDouble() / parallelProcessed else 0.0
    
    /**
     * Tempo médio de processamento paralelo (nanosegundos).
     */
    val averageParallelTimeNs: Long
        get() = if (parallelProcessed > 0) parallelTimeNs / parallelProcessed else 0
    
    /**
     * Tempo médio de processamento sequencial (nanosegundos).
     */
    val averageSequentialTimeNs: Long
        get() = if (sequentialProcessed > 0) sequentialTimeNs / sequentialProcessed else 0
    
    companion object {
        /**
         * Cria métricas vazias.
         */
        fun empty(): PacketMetrics = PacketMetrics()
    }
}

/**
 * Builder thread-safe para métricas de pacotes.
 */
class PacketMetricsBuilder {
    private val totalProcessed = AtomicLong(0)
    private val parallelProcessed = AtomicLong(0)
    private val sequentialProcessed = AtomicLong(0)
    private val criticalProcessed = AtomicLong(0)
    private val parallelErrors = AtomicLong(0)
    private val sequentialErrors = AtomicLong(0)
    private val parallelTimeNs = AtomicLong(0)
    private val sequentialTimeNs = AtomicLong(0)
    
    fun incrementTotal() = totalProcessed.incrementAndGet()
    fun incrementParallel() = parallelProcessed.incrementAndGet()
    fun incrementSequential() = sequentialProcessed.incrementAndGet()
    fun incrementCritical() = criticalProcessed.incrementAndGet()
    fun incrementParallelError() = parallelErrors.incrementAndGet()
    fun incrementSequentialError() = sequentialErrors.incrementAndGet()
    fun addParallelTime(ns: Long) = parallelTimeNs.addAndGet(ns)
    fun addSequentialTime(ns: Long) = sequentialTimeNs.addAndGet(ns)
    
    fun build(): PacketMetrics = PacketMetrics(
        totalProcessed = totalProcessed.get(),
        parallelProcessed = parallelProcessed.get(),
        sequentialProcessed = sequentialProcessed.get(),
        criticalProcessed = criticalProcessed.get(),
        parallelErrors = parallelErrors.get(),
        sequentialErrors = sequentialErrors.get(),
        parallelTimeNs = parallelTimeNs.get(),
        sequentialTimeNs = sequentialTimeNs.get()
    )
    
    fun reset() {
        totalProcessed.set(0)
        parallelProcessed.set(0)
        sequentialProcessed.set(0)
        criticalProcessed.set(0)
        parallelErrors.set(0)
        sequentialErrors.set(0)
        parallelTimeNs.set(0)
        sequentialTimeNs.set(0)
    }
}
