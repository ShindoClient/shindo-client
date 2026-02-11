package me.miki.shindo.utils.network.okhttp

import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object OkHttpClientPool {

    private val connectionPool = ConnectionPool(8, 5, TimeUnit.MINUTES)
    private val clientCache = ConcurrentHashMap<Int, OkHttpClient>()

    @JvmStatic
    fun get(timeoutMs: Int): OkHttpClient {
        val sanitizedTimeout = timeoutMs.coerceAtLeast(1000)
        return clientCache.getOrPut(sanitizedTimeout) {
            OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(sanitizedTimeout.toLong(), TimeUnit.MILLISECONDS)
                .readTimeout(sanitizedTimeout.toLong(), TimeUnit.MILLISECONDS)
                .writeTimeout(sanitizedTimeout.toLong(), TimeUnit.MILLISECONDS)
                .build()
        }
    }
}
