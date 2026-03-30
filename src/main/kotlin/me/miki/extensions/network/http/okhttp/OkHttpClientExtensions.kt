@file:JvmName("OkHttpClientExtensions")

package me.miki.extensions.network.http.okhttp

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Executes a request and returns null on IO failure.
 * Caller is responsible for closing the response.
 */
fun OkHttpClient.executeOrNull(request: Request): Response? {
    return try {
        newCall(request).execute()
    } catch (ignored: IOException) {
        null
    }
}

/**
 * Creates a derived client with specific timeout values in milliseconds.
 */
fun OkHttpClient.withTimeouts(
    connectTimeoutMs: Long = 8000L,
    readTimeoutMs: Long = 10000L,
    writeTimeoutMs: Long = 10000L
): OkHttpClient {
    return newBuilder()
        .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
        .build()
}
