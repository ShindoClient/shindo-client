package me.miki.shindo.utils.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.logger.ShindoLogger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.sink
import java.io.File
import java.util.concurrent.TimeUnit

object HttpUtils {
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()

    private val gson = Gson()

    // Shared client — connection pooling, keep-alive, and redirect following are automatic
    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

    // Variant with custom timeout for callers that need it (e.g. large downloads)
    private fun clientWithTimeout(timeoutMs: Long): OkHttpClient =
        client
            .newBuilder()
            .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .build()

    @JvmStatic
    fun postJson(
        url: String,
        request: Any,
    ): JsonObject? = postJson(url, request, null)

    @JvmStatic
    fun postJson(
        url: String,
        request: Any,
        headers: Map<String, String>?,
    ): JsonObject? {
        val body = gson.toJson(request).toRequestBody(JSON_TYPE)

        val req =
            Request
                .Builder()
                .url(url)
                .post(body)
                .header("User-Agent", UserAgents.MOZILLA)
                .header("Accept", "application/json")
                .apply { headers?.forEach { (k, v) -> addHeader(k, v) } }
                .build()

        return executeForJson(req)
    }

    @JvmStatic
    fun readJson(
        url: String,
        headers: Map<String, String>?,
    ): JsonObject? = readJson(url, headers, UserAgents.MOZILLA)

    @JvmStatic
    fun readJson(
        url: String,
        headers: Map<String, String>?,
        userAgent: String,
    ): JsonObject? {
        val req =
            Request
                .Builder()
                .url(url)
                .get()
                .header("User-Agent", userAgent)
                .header("Accept-Language", "en-US")
                .header("Accept-Charset", "UTF-8")
                .apply { headers?.forEach { (k, v) -> addHeader(k, v) } }
                .build()

        return executeForJson(req)
    }

    @JvmStatic
    fun downloadFile(
        url: String,
        outputFile: File,
    ): Boolean = downloadFile(url, outputFile, UserAgents.MOZILLA, 5000, false)

    @JvmStatic
    fun downloadFile(
        url: String,
        outputFile: File,
        userAgent: String,
    ): Boolean = downloadFile(url, outputFile, userAgent, 5000, false)

    @JvmStatic
    fun downloadFile(
        url: String,
        outputFile: File,
        userAgent: String,
        timeoutMs: Int,
        @Suppress("UNUSED_PARAMETER") useCaches: Boolean, // OkHttp handles caching via Cache; flag kept for API compat
    ): Boolean {
        val req =
            Request
                .Builder()
                .url(url)
                .get()
                .header("User-Agent", userAgent)
                .build()

        return try {
            clientWithTimeout(timeoutMs.toLong()).newCall(req).execute().use { response ->
                if (!response.isSuccessful) {
                    ShindoLogger.error("Download failed — HTTP ${response.code} for $url")
                    return false
                }
                // Stream directly to disk without buffering the entire body in memory
                val body = response.body ?: return false
                outputFile.sink().use { fileSink ->
                    body.source().readAll(fileSink)
                }
                true
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to download file", e)
            false
        }
    }

    @JvmStatic
    fun encodeURL(url: String): String = java.net.URLEncoder.encode(url, "UTF-8")

    @JvmStatic
    fun decodeURL(url: String): String = java.net.URLDecoder.decode(url, "UTF-8")

    // --- Private helpers ---

    private fun executeForJson(request: Request): JsonObject? {
        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    if (!response.isSuccessful) {
                        ShindoLogger.error("HTTP ${response.code} with empty body for ${request.url}")
                    }
                    return null
                }
                gson.fromJson(body, JsonObject::class.java)
            }
        } catch (e: Exception) {
            ShindoLogger.error("HTTP request failed for ${request.url}", e)
            null
        }
    }
}
