package me.miki.shindo.management.addons.resourcify.net

import com.google.gson.Gson
import com.google.gson.JsonParseException
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.network.HttpUtils
import me.miki.shindo.utils.network.UserAgents
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

object ResourcifyHttp {

    private val gson = Gson()

    fun <T> getJson(url: String, type: Type, headers: Map<String, String>? = null): T? {
        val connection = HttpUtils.setupConnection(url, UserAgents.MOZILLA, 8000, false) ?: return null
        applyHeaders(connection, headers)
        val response = HttpUtils.readResponse(connection)
        return parse(response, type)
    }

    fun <T> postJson(url: String, body: Any, type: Type, headers: Map<String, String>? = null): T? {
        val connection = HttpUtils.setupConnection(url, UserAgents.MOZILLA, 8000, false) ?: return null
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        applyHeaders(connection, headers)
        try {
            OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { writer ->
                gson.toJson(body, writer)
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to post json to $url", e)
            return null
        }
        val response = HttpUtils.readResponse(connection)
        return parse(response, type)
    }

    private fun applyHeaders(connection: HttpURLConnection, headers: Map<String, String>?) {
        if (headers.isNullOrEmpty()) return
        for ((key, value) in headers) {
            connection.setRequestProperty(key, value)
        }
    }

    private fun <T> parse(response: String?, type: Type): T? {
        if (response == null) return null
        return try {
            gson.fromJson(response, type)
        } catch (e: JsonParseException) {
            ShindoLogger.error("Failed to parse json response", e)
            null
        }
    }
}
