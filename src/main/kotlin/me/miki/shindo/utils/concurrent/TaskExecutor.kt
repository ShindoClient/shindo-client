package me.miki.shindo.utils.concurrent

import net.minecraft.client.Minecraft
import java.util.concurrent.*
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Executor de tarefas assíncronas com suporte a callbacks, prioridades e integração com Minecraft.
 * 
 * Exemplos de uso:
 * 
 * Kotlin:
 * ```kotlin
 * TaskExecutor.runAsync(ThreadPoolType.IO) {
 *     // código assíncrono
 *     "resultado"
 * }.thenOnMainThread { result ->
 *     // código no thread principal do Minecraft
 *     println(result)
 * }
 * ```
 * 
 * Java:
 * ```java
 * TaskExecutor.runAsync(ThreadPoolType.IO, () -> {
 *     // código assíncrono
 *     return "resultado";
 * }).thenOnMainThread(result -> {
 *     // código no thread principal do Minecraft
 *     System.out.println(result);
 * });
 * ```
 */
object TaskExecutor {
    
    /**
     * Executa uma tarefa assíncrona e retorna um CompletableFuture.
     * 
     * @param type Tipo de pool a ser usado
     * @param priority Prioridade da tarefa (opcional, padrão: NORMAL)
     * @param task Tarefa a ser executada
     * @return CompletableFuture com o resultado
     */
    @JvmStatic
    fun <T> runAsync(
        type: ThreadPoolType = ThreadPoolType.GENERAL,
        priority: TaskPriority = TaskPriority.NORMAL,
        task: () -> T
    ): CompletableFuture<T> {
        val executor = ThreadPoolManager.getExecutor(type)
        val future = CompletableFuture.supplyAsync(Supplier { task() }, executor)
        return future
    }
    
    /**
     * Versão Java-friendly usando Supplier.
     */
    @JvmStatic
    fun <T> runAsync(
        type: ThreadPoolType,
        priority: TaskPriority,
        task: Supplier<T>
    ): CompletableFuture<T> {
        return runAsync(type, priority) { task.get() }
    }
    
    /**
     * Executa uma tarefa assíncrona sem retorno.
     */
    @JvmStatic
    fun runAsync(
        type: ThreadPoolType = ThreadPoolType.GENERAL,
        priority: TaskPriority = TaskPriority.NORMAL,
        task: Runnable
    ): CompletableFuture<Void> {
        val executor = ThreadPoolManager.getExecutor(type)
        return CompletableFuture.runAsync(task, executor)
    }
    
    /**
     * Executa uma tarefa no thread principal do Minecraft.
     * 
     * @param task Tarefa a ser executada
     * @return CompletableFuture que completa quando a tarefa é executada
     */
    @JvmStatic
    fun runOnMainThread(task: Runnable): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val mc = Minecraft.getMinecraft()
        
        // Verifica se já estamos no thread principal
        if (mc.isCallingFromMinecraftThread) {
            // Já estamos no thread principal
            try {
                task.run()
                future.complete(null)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        } else {
            // Agendar no thread principal
            mc.addScheduledTask {
                try {
                    task.run()
                    future.complete(null)
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
        }
        
        return future
    }
    
    /**
     * Executa uma tarefa no thread principal do Minecraft com retorno.
     */
    @JvmStatic
    fun <T> runOnMainThread(task: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        val mc = Minecraft.getMinecraft()
        
        // Verifica se já estamos no thread principal
        if (mc.isCallingFromMinecraftThread) {
            // Já estamos no thread principal
            try {
                future.complete(task())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        } else {
            // Agendar no thread principal
            mc.addScheduledTask {
                try {
                    future.complete(task())
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
        }
        
        return future
    }
    
    /**
     * Agenda uma tarefa para ser executada após um delay.
     */
    @JvmStatic
    fun schedule(
        type: ThreadPoolType = ThreadPoolType.SCHEDULED,
        delay: Long,
        unit: TimeUnit,
        task: Runnable
    ): ScheduledFuture<*> {
        val executor = ThreadPoolManager.getScheduledExecutor()
        return executor.schedule(task, delay, unit)
    }
    
    /**
     * Agenda uma tarefa para ser executada periodicamente.
     */
    @JvmStatic
    fun scheduleAtFixedRate(
        type: ThreadPoolType = ThreadPoolType.SCHEDULED,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        task: Runnable
    ): ScheduledFuture<*> {
        val executor = ThreadPoolManager.getScheduledExecutor()
        return executor.scheduleAtFixedRate(task, initialDelay, period, unit)
    }
}

/**
 * Extensões para CompletableFuture para facilitar o uso.
 */

/**
 * Executa uma ação no thread principal do Minecraft quando o CompletableFuture completa com sucesso.
 */
fun <T> CompletableFuture<T>.thenOnMainThread(action: (T) -> Unit): CompletableFuture<T> {
    return this.thenCompose { result ->
        TaskExecutor.runOnMainThread { action(result) }.thenApply { result }
    }
}

/**
 * Executa uma ação no thread principal do Minecraft quando o CompletableFuture completa (sucesso ou falha).
 */
fun <T> CompletableFuture<T>.whenCompleteOnMainThread(action: (T?, Throwable?) -> Unit): CompletableFuture<T> {
    return this.whenComplete { result, exception ->
        TaskExecutor.runOnMainThread { action(result, exception) }
    }
}

/**
 * Trata erros no thread principal do Minecraft.
 */
fun <T> CompletableFuture<T>.onErrorOnMainThread(action: (Throwable) -> Unit): CompletableFuture<T> {
    return this.exceptionally { exception ->
        TaskExecutor.runOnMainThread { action(exception) }
        throw exception
    }
}
