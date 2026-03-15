package me.miki.shindo.utils.concurrent

import net.minecraft.client.Minecraft
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

object TaskExecutor {
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

    @JvmStatic
    fun <T> runAsync(
        type: ThreadPoolType,
        priority: TaskPriority,
        task: Supplier<T>
    ): CompletableFuture<T> {
        return runAsync(type, priority) { task.get() }
    }

    @JvmStatic
    fun runAsync(
        type: ThreadPoolType = ThreadPoolType.GENERAL,
        priority: TaskPriority = TaskPriority.NORMAL,
        task: Runnable
    ): CompletableFuture<Void> {
        val executor = ThreadPoolManager.getExecutor(type)
        return CompletableFuture.runAsync(task, executor)
    }

    @JvmStatic
    fun runOnMainThread(task: Runnable): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val mc = Minecraft.getMinecraft()

        if (mc.isCallingFromMinecraftThread) {

            try {
                task.run()
                future.complete(null)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        } else {

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

    @JvmStatic
    fun <T> runOnMainThread(task: () -> T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        val mc = Minecraft.getMinecraft()

        if (mc.isCallingFromMinecraftThread) {

            try {
                future.complete(task())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        } else {

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

fun <T> CompletableFuture<T>.thenOnMainThread(action: (T) -> Unit): CompletableFuture<T> {
    return this.thenCompose { result ->
        TaskExecutor.runOnMainThread { action(result) }.thenApply { result }
    }
}

fun <T> CompletableFuture<T>.whenCompleteOnMainThread(action: (T?, Throwable?) -> Unit): CompletableFuture<T> {
    return this.whenComplete { result, exception ->
        TaskExecutor.runOnMainThread { action(result, exception) }
    }
}

fun <T> CompletableFuture<T>.onErrorOnMainThread(action: (Throwable) -> Unit): CompletableFuture<T> {
    return this.exceptionally { exception ->
        TaskExecutor.runOnMainThread { action(exception) }
        throw exception
    }
}
