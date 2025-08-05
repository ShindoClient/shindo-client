package me.miki.shindo.utils;

import java.util.concurrent.*;

public class Multithreading {

    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(8, new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    });

    public static ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    });

    public static void schedule(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
        return Multithreading.RUNNABLE_POOL.schedule(r, delay, unit);
    }

    public static int getTotal() {
        return ((ThreadPoolExecutor) Multithreading.POOL).getActiveCount();
    }

    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }
}