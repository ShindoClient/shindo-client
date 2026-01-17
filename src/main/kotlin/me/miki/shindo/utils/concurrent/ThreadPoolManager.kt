package me.miki.shindo.utils.concurrent

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Gerenciador centralizado de thread pools.
 * Cria e gerencia pools especializados para diferentes tipos de operações.
 */
object ThreadPoolManager {
    
    private val ioPool: ThreadPoolExecutor
    private val cpuPool: ThreadPoolExecutor
    private val networkPool: ThreadPoolExecutor
    private val scheduledPool: ScheduledExecutorService
    private val generalPool: ThreadPoolExecutor
    
    private val threadCounter = AtomicInteger(0)
    
    init {
        val cpuCount = Runtime.getRuntime().availableProcessors()
        
        // IO Pool - muitas threads para operações bloqueantes
        ioPool = ThreadPoolExecutor(
            4,
            32,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            ThreadFactory { runnable ->
                Thread(runnable, "Shindo-IO-${threadCounter.incrementAndGet()}").apply {
                    isDaemon = true
                    priority = Thread.NORM_PRIORITY
                }
            }
        )
        
        // CPU Pool - poucas threads (número de cores)
        cpuPool = ThreadPoolExecutor(
            cpuCount,
            cpuCount,
            0L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            ThreadFactory { runnable ->
                Thread(runnable, "Shindo-CPU-${threadCounter.incrementAndGet()}").apply {
                    isDaemon = true
                    priority = Thread.NORM_PRIORITY
                }
            }
        )
        
        // Network Pool - muitas threads para operações de rede
        networkPool = ThreadPoolExecutor(
            4,
            16,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            ThreadFactory { runnable ->
                Thread(runnable, "Shindo-Network-${threadCounter.incrementAndGet()}").apply {
                    isDaemon = true
                    priority = Thread.NORM_PRIORITY
                }
            }
        )
        
        // Scheduled Pool - para tarefas agendadas
        scheduledPool = Executors.newScheduledThreadPool(
            cpuCount.coerceAtLeast(4),
            ThreadFactory { runnable ->
                Thread(runnable, "Shindo-Scheduled-${threadCounter.incrementAndGet()}").apply {
                    isDaemon = true
                    priority = Thread.NORM_PRIORITY
                }
            }
        )
        
        // General Pool - pool genérico
        generalPool = ThreadPoolExecutor(
            2,
            16,
            60L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            ThreadFactory { runnable ->
                Thread(runnable, "Shindo-General-${threadCounter.incrementAndGet()}").apply {
                    isDaemon = true
                    priority = Thread.NORM_PRIORITY
                }
            }
        )
    }
    
    /**
     * Obtém o executor apropriado para o tipo de pool.
     */
    @JvmStatic
    fun getExecutor(type: ThreadPoolType): ExecutorService = when (type) {
        ThreadPoolType.IO -> ioPool
        ThreadPoolType.CPU -> cpuPool
        ThreadPoolType.NETWORK -> networkPool
        ThreadPoolType.SCHEDULED -> scheduledPool
        ThreadPoolType.GENERAL -> generalPool
    }
    
    /**
     * Obtém o scheduled executor.
     */
    @JvmStatic
    fun getScheduledExecutor(): ScheduledExecutorService = scheduledPool
    
    /**
     * Obtém estatísticas de um pool.
     */
    @JvmStatic
    fun getPoolStats(type: ThreadPoolType): PoolStats {
        val executor = getExecutor(type) as? ThreadPoolExecutor ?: return PoolStats(0, 0, 0, 0)
        return PoolStats(
            activeCount = executor.activeCount,
            poolSize = executor.poolSize,
            queueSize = executor.queue.size,
            completedTaskCount = executor.completedTaskCount.toInt()
        )
    }
    
    /**
     * Obtém estatísticas de todos os pools.
     */
    @JvmStatic
    fun getAllStats(): Map<ThreadPoolType, PoolStats> {
        return ThreadPoolType.values().associateWith { getPoolStats(it) }
    }
    
    /**
     * Encerra todos os pools. Deve ser chamado no shutdown do cliente.
     */
    @JvmStatic
    fun shutdown() {
        ioPool.shutdown()
        cpuPool.shutdown()
        networkPool.shutdown()
        scheduledPool.shutdown()
        generalPool.shutdown()
    }
    
    /**
     * Encerra todos os pools agressivamente.
     */
    @JvmStatic
    fun shutdownNow() {
        ioPool.shutdownNow()
        cpuPool.shutdownNow()
        networkPool.shutdownNow()
        scheduledPool.shutdownNow()
        generalPool.shutdownNow()
    }
    
    /**
     * Estatísticas de um pool.
     */
    data class PoolStats(
        val activeCount: Int,
        val poolSize: Int,
        val queueSize: Int,
        val completedTaskCount: Int
    )
}
