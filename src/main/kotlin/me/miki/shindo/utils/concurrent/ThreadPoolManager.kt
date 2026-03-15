package me.miki.shindo.utils.concurrent

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object ThreadPoolManager {

    private val ioPool: ThreadPoolExecutor
    private val cpuPool: ThreadPoolExecutor
    private val networkPool: ThreadPoolExecutor
    private val scheduledPool: ScheduledExecutorService
    private val generalPool: ThreadPoolExecutor

    private val threadCounter = AtomicInteger(0)

    init {
        val cpuCount = Runtime.getRuntime().availableProcessors()

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

        scheduledPool = Executors.newScheduledThreadPool(
            cpuCount.coerceAtLeast(4),
            ThreadFactory { runnable ->
                Thread(runnable, "Shindo-Scheduled-${threadCounter.incrementAndGet()}").apply {
                    isDaemon = true
                    priority = Thread.NORM_PRIORITY
                }
            }
        )

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

    @JvmStatic
    fun getExecutor(type: ThreadPoolType): ExecutorService = when (type) {
        ThreadPoolType.IO -> ioPool
        ThreadPoolType.CPU -> cpuPool
        ThreadPoolType.NETWORK -> networkPool
        ThreadPoolType.SCHEDULED -> scheduledPool
        ThreadPoolType.GENERAL -> generalPool
    }

    @JvmStatic
    fun getScheduledExecutor(): ScheduledExecutorService = scheduledPool

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

    @JvmStatic
    fun getAllStats(): Map<ThreadPoolType, PoolStats> {
        val stats = LinkedHashMap<ThreadPoolType, PoolStats>()
        for (type in ThreadPoolType.values()) {
            stats[type] = getPoolStats(type)
        }
        return stats
    }

    @JvmStatic
    fun shutdown() {
        ioPool.shutdown()
        cpuPool.shutdown()
        networkPool.shutdown()
        scheduledPool.shutdown()
        generalPool.shutdown()
    }

    @JvmStatic
    fun shutdownNow() {
        ioPool.shutdownNow()
        cpuPool.shutdownNow()
        networkPool.shutdownNow()
        scheduledPool.shutdownNow()
        generalPool.shutdownNow()
    }

    data class PoolStats(
        val activeCount: Int,
        val poolSize: Int,
        val queueSize: Int,
        val completedTaskCount: Int
    )
}
