package me.miki.shindo.utils.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.logger.ShindoLogger
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HttpUtils {

    private const val ACCEPTED_RESPONSE = "application/json"
    private val gson = Gson()

    @JvmStatic
    fun readJson(connection: HttpURLConnection): JsonObject? {
        return gson.fromJson(readResponse(connection), JsonObject::class.java)
    }

    @JvmStatic
    fun postJson(url: String, request: Any): JsonObject? {
        return postJson(url, request, null)
    }

    @JvmStatic
    fun postJson(url: String, request: Any, headers: Map<String, String>?): JsonObject? {
        val connection = setupConnection(url, UserAgents.MOZILLA, 5000, false)
        if (connection == null) {
            ShindoLogger.error("Failed to setup connection for post json")
            return null
        }

        connection.doOutput = true
        connection.addRequestProperty("Content-Type", ACCEPTED_RESPONSE)
        connection.addRequestProperty("Accept", ACCEPTED_RESPONSE)

        if (!headers.isNullOrEmpty()) {
            for ((key, value) in headers) {
                connection.addRequestProperty(key, value)
            }
        }

        try {
            connection.requestMethod = "POST"
            connection.outputStream.use { output ->
                output.write(gson.toJson(request).toByteArray(StandardCharsets.UTF_8))
            }
        } catch (e: IOException) {
            ShindoLogger.error("Failed to post json", e)
            return null
        }

        return readJson(connection)
    }

    @JvmStatic
    fun readResponse(connection: HttpURLConnection): String {
        val redirection = connection.getHeaderField("Location")
        if (redirection != null) {
            val redirected = setupConnection(redirection, UserAgents.MOZILLA, 5000, false)
            return readResponse(redirected ?: return "")
        }

        val response = StringBuilder()
        try {
            BufferedReader(
                InputStreamReader(
                    if (connection.responseCode >= 400) connection.errorStream else connection.inputStream
                )
            ).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    response.append(line).append('\n')
                }
            }
        } catch (e: IOException) {
            ShindoLogger.error("Failed to read response", e)
        }

        return response.toString()
    }

    @JvmStatic
    fun readJson(url: String, headers: Map<String, String>?, userAgents: String): JsonObject? {
        return try {
            val connection = setupConnection(url, userAgents, 5000, false)
            if (connection == null) {
                ShindoLogger.error("Failed to setup connection for read json")
                return null
            }

            if (!headers.isNullOrEmpty()) {
                for ((key, value) in headers) {
                    connection.addRequestProperty(key, value)
                }
            }

            val isr: InputStream =
                if (connection.responseCode != 200) connection.errorStream else connection.inputStream
            BufferedReader(InputStreamReader(isr, StandardCharsets.UTF_8)).use { reader ->
                gson.fromJson(readResponse(reader), JsonObject::class.java)
            }
        } catch (e: IOException) {
            ShindoLogger.error("Failed to read json", e)
            null
        }
    }

    @JvmStatic
    fun readJson(url: String, headers: Map<String, String>?): JsonObject? {
        return readJson(url, headers, UserAgents.MOZILLA)
    }

    private fun readResponse(br: BufferedReader): String? {
        return try {
            val sb = StringBuilder()
            var line: String?
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            sb.toString()
        } catch (e: IOException) {
            ShindoLogger.error("Failed to read response", e)
            null
        }
    }

    @JvmStatic
    fun downloadFile(url: String, outputFile: File, userAgent: String, timeout: Int, useCaches: Boolean): Boolean {
        val sanitized = url.replace(" ", "%20")
        return try {
            val connection = setupConnection(sanitized, userAgent, timeout, useCaches) ?: return false
            FileOutputStream(outputFile).use { fileOut ->
                BufferedInputStream(connection.inputStream).use { input ->
                    org.apache.commons.io.IOUtils.copy(input, fileOut)
                }
            }
            true
        } catch (e: Exception) {
            ShindoLogger.error("Failed to download file", e)
            false
        }
    }

    @JvmStatic
    fun downloadFile(url: String, outputFile: File, userAgents: String): Boolean {
        return downloadFile(url, outputFile, userAgents, 5000, false)
    }

    @JvmStatic
    fun downloadFile(url: String, outputFile: File) {
        downloadFile(url, outputFile, UserAgents.MOZILLA, 5000, false)
    }

    @JvmStatic
    fun setupConnection(url: String, userAgent: String, timeout: Int, useCaches: Boolean): HttpURLConnection? {
        return try {
            val punycodeUrl = PunycodeUtils.punycode(url)
            val connection = URL(punycodeUrl).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.useCaches = useCaches
            connection.addRequestProperty("User-Agent", userAgent)
            connection.setRequestProperty("Accept-Language", "en-US")
            connection.setRequestProperty("Accept-Charset", "UTF-8")
            connection.readTimeout = timeout
            connection.connectTimeout = timeout
            connection.doOutput = true

            connection
        } catch (e: Exception) {
            ShindoLogger.error("Failed to setup connection")
            null
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
}
