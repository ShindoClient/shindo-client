package me.miki.shindo.utils

import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.TaskPriority
import me.miki.shindo.utils.concurrent.ThreadPoolType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@Deprecated(
    "Use TaskExecutor for better control and features",
    ReplaceWith("TaskExecutor.runAsync(ThreadPoolType.GENERAL) { }")
)
object Multithreading {

    @JvmStatic
    var POOL = TaskExecutor

    @JvmStatic
    fun schedule(runnable: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
        TaskExecutor.scheduleAtFixedRate(ThreadPoolType.SCHEDULED, initialDelay, delay, unit, runnable)
    }

    @JvmStatic
    fun schedule(runnable: Runnable, delay: Long, unit: TimeUnit) {
        TaskExecutor.schedule(ThreadPoolType.SCHEDULED, delay, unit, runnable)
    }

    @JvmStatic
    fun getTotal(): Int {
        val stats = me.miki.shindo.utils.concurrent.ThreadPoolManager.getAllStats()
        var total = 0
        for (stat in stats.values) {
            total += stat.activeCount
        }
        return total
    }

    @JvmStatic
    fun runAsync(runnable: Runnable) {
        TaskExecutor.runAsync(ThreadPoolType.GENERAL, TaskPriority.NORMAL, runnable)
    }
    fun runAsync(task: () -> Unit): CompletableFuture<Void> {
        return TaskExecutor.runAsync(ThreadPoolType.GENERAL, TaskPriority.NORMAL, Runnable { task() })
    }
}
