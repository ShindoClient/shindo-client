package me.miki.shindo.utils.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.network.okhttp.HttpResponseData
import me.miki.shindo.utils.network.okhttp.OkHttpClientPool
import me.miki.shindo.utils.network.okhttp.OkHttpRequestUtils
import me.miki.shindo.utils.network.okhttp.OkHttpResponseUtils
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HttpUtils {

    private const val DEFAULT_TIMEOUT = 5000
    private val gson = Gson()

    @JvmStatic
    fun get(url: String, headers: Map<String, String>?, userAgent: String, timeout: Int): HttpResponseData? {
        return try {
            val request = OkHttpRequestUtils.buildGetRequest(url, userAgent, headers)
            val client = OkHttpClientPool.get(timeout)
            client.newCall(request).execute().use { response ->
                OkHttpResponseUtils.toResponseData(response)
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to execute GET request: $url", e)
            null
        }
    }

    @JvmStatic
    fun get(url: String, headers: Map<String, String>?): HttpResponseData? {
        return get(url, headers, UserAgents.MOZILLA, DEFAULT_TIMEOUT)
    }

    @JvmStatic
    fun postJson(url: String, request: Any): JsonObject? {
        return postJson(url, request, null)
    }

    @JvmStatic
    fun postJson(url: String, request: Any, headers: Map<String, String>?): JsonObject? {
        val response = postJsonRaw(url, request, headers, UserAgents.MOZILLA, DEFAULT_TIMEOUT) ?: return null
        if (!response.successful || response.body.isBlank()) {
            return null
        }
        return parseJson(response.body)
    }

    @JvmStatic
    fun postJsonRaw(
        url: String,
        request: Any,
        headers: Map<String, String>?,
        userAgent: String,
        timeout: Int
    ): HttpResponseData? {
        return try {
            val payload = gson.toJson(request)
            val httpRequest = OkHttpRequestUtils.buildJsonPostRequest(url, payload, userAgent, headers)
            val client = OkHttpClientPool.get(timeout)
            client.newCall(httpRequest).execute().use { response ->
                OkHttpResponseUtils.toResponseData(response)
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to POST JSON: $url", e)
            null
        }
    }

    @JvmStatic
    fun readJson(url: String, headers: Map<String, String>?, userAgents: String): JsonObject? {
        val response = get(url, headers, userAgents, DEFAULT_TIMEOUT) ?: return null
        if (response.body.isBlank()) {
            return null
        }
        return parseJson(response.body)
    }

    @JvmStatic
    fun readJson(url: String, headers: Map<String, String>?): JsonObject? {
        return readJson(url, headers, UserAgents.MOZILLA)
    }

    @JvmStatic
    fun readJson(response: HttpResponseData): JsonObject? {
        if (response.body.isBlank()) {
            return null
        }
        return parseJson(response.body)
    }

    @JvmStatic
    fun readJson(connection: HttpURLConnection): JsonObject? {
        return parseJson(readResponse(connection))
    }

    @JvmStatic
    fun downloadFile(url: String, outputFile: File, userAgent: String, timeout: Int, useCaches: Boolean): Boolean {
        return try {
            val request = OkHttpRequestUtils.buildGetRequest(url, userAgent, null)
            val client = OkHttpClientPool.get(timeout)
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return false
                }

                val body = response.body() ?: return false
                outputFile.parentFile?.mkdirs()
                FileOutputStream(outputFile).use { output ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            output.write(buffer, 0, read)
                        }
                    }
                }
                true
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to download file: $url", e)
            false
        }
    }

    @JvmStatic
    fun downloadFile(url: String, outputFile: File, userAgents: String): Boolean {
        return downloadFile(url, outputFile, userAgents, DEFAULT_TIMEOUT, false)
    }

    @JvmStatic
    fun downloadFile(url: String, outputFile: File) {
        downloadFile(url, outputFile, UserAgents.MOZILLA, DEFAULT_TIMEOUT, false)
    }

    @JvmStatic
    @Deprecated("Legacy API. Prefer HttpUtils.get()/postJsonRaw()", ReplaceWith("HttpUtils.get(url, null, userAgent, timeout)"))
    fun setupConnection(url: String, userAgent: String, timeout: Int, useCaches: Boolean): HttpURLConnection? {
        return try {
            val connection = URL(PunycodeUtils.punycode(url)).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.useCaches = useCaches
            connection.addRequestProperty("User-Agent", userAgent)
            connection.setRequestProperty("Accept-Language", "en-US")
            connection.setRequestProperty("Accept-Charset", "UTF-8")
            connection.readTimeout = timeout
            connection.connectTimeout = timeout
            connection.doOutput = false
            connection
        } catch (e: Exception) {
            ShindoLogger.error("Failed to setup legacy HttpURLConnection for $url", e)
            null
        }
    }

    @JvmStatic
    fun readResponse(connection: HttpURLConnection): String {
        return try {
            BufferedReader(
                InputStreamReader(
                    if (connection.responseCode >= 400) connection.errorStream else connection.inputStream,
                    StandardCharsets.UTF_8
                )
            ).use { reader ->
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append('\n')
                }
                sb.toString()
            }
        } catch (e: IOException) {
            ShindoLogger.error("Failed to read response from legacy HttpURLConnection", e)
            ""
        }
    }

    @JvmStatic
    fun encodeURL(url: String): String {
        return try {
            URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
        } catch (_: UnsupportedEncodingException) {
            url
        }
    }

    @JvmStatic
    fun decodeURL(url: String): String {
        return try {
            URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
        } catch (_: UnsupportedEncodingException) {
            url
        }
    }

    private fun parseJson(body: String): JsonObject? {
        return try {
            gson.fromJson(body, JsonObject::class.java)
        } catch (e: Exception) {
            ShindoLogger.error("Failed to parse JSON response", e)
            null
        }
    }
}
