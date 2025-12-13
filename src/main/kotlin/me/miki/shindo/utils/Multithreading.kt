package me.miki.shindo.utils

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Multithreading {

    private val runnablePool: ScheduledExecutorService = Executors.newScheduledThreadPool(8) { Thread(it) }

    @JvmStatic
    var POOL = Executors.newCachedThreadPool { Thread(it) }

    @JvmStatic
    fun schedule(runnable: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit) {
        runnablePool.scheduleAtFixedRate(runnable, initialDelay, delay, unit)
    }

    @JvmStatic
    fun schedule(runnable: Runnable, delay: Long, unit: TimeUnit) {
        runnablePool.schedule(runnable, delay, unit)
    }

    @JvmStatic
    fun getTotal(): Int = (POOL as ThreadPoolExecutor).activeCount

    @JvmStatic
    fun runAsync(runnable: Runnable) {
        POOL.execute(runnable)
    }
}
