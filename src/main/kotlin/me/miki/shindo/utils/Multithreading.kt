package me.miki.shindo.utils

import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.TaskPriority
import me.miki.shindo.utils.concurrent.ThreadPoolType
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Wrapper simplificado para o sistema de multithreading.
 * Mantém compatibilidade com o código existente enquanto usa o novo sistema.
 * 
 * @deprecated Use TaskExecutor diretamente para melhor controle e funcionalidades.
 */
@Deprecated("Use TaskExecutor for better control and features", ReplaceWith("TaskExecutor.runAsync(ThreadPoolType.GENERAL) { }"))
object Multithreading {

    @JvmStatic
    var POOL = TaskExecutor // Mantido para compatibilidade, mas não é mais usado diretamente

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
        return stats.values.sumOf { it.activeCount }
    }

    @JvmStatic
    fun runAsync(runnable: Runnable) {
        TaskExecutor.runAsync(ThreadPoolType.GENERAL, TaskPriority.NORMAL, runnable)
    }
    
    /**
     * Extensão Kotlin-friendly.
     */
    fun runAsync(task: () -> Unit): CompletableFuture<Void> {
        return TaskExecutor.runAsync(ThreadPoolType.GENERAL, TaskPriority.NORMAL, Runnable { task() })
    }
}
