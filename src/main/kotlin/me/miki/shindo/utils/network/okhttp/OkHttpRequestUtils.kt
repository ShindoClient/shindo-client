package me.miki.shindo.utils.network.okhttp

import me.miki.shindo.utils.network.PunycodeUtils
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

object OkHttpRequestUtils {

    private val jsonMediaType: MediaType = MediaType.parse("application/json; charset=utf-8")
        ?: MediaType.parse("application/json")
        ?: throw IllegalStateException("Unable to create JSON media type")

    @JvmStatic
    fun buildGetRequest(
        url: String,
        userAgent: String,
        headers: Map<String, String>?
    ): Request {
        val builder = Request.Builder()
            .url(normalizeUrl(url))
            .get()
            .header("User-Agent", userAgent)
            .header("Accept-Language", "en-US")
            .header("Accept-Charset", "UTF-8")

        appendHeaders(builder, headers)
        return builder.build()
    }

    @JvmStatic
    fun buildJsonPostRequest(
        url: String,
        jsonBody: String,
        userAgent: String,
        headers: Map<String, String>?
    ): Request {
        val builder = Request.Builder()
            .url(normalizeUrl(url))
            .post(RequestBody.create(jsonMediaType, jsonBody))
            .header("User-Agent", userAgent)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Accept-Language", "en-US")
            .header("Accept-Charset", "UTF-8")

        appendHeaders(builder, headers)
        return builder.build()
    }

    private fun appendHeaders(builder: Request.Builder, headers: Map<String, String>?) {
        if (headers.isNullOrEmpty()) {
            return
        }
        for ((key, value) in headers) {
            builder.header(key, value)
        }
    }

    private fun normalizeUrl(url: String): String {
        return PunycodeUtils.punycode(url.trim().replace(" ", "%20"))
    }
}
