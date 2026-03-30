@file:JvmName("OkHttpRequestExtensions")

package me.miki.extensions.network.http.okhttp

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

/**
 * Applies headers from a map into a Request.Builder.
 */
fun Request.Builder.applyHeaders(headers: Map<String, String>): Request.Builder {
    for ((key, value) in headers) {
        header(key, value)
    }
    return this
}

/**
 * Sets a JSON body and POST method in one call.
 */
fun Request.Builder.postJson(
    json: String,
    mediaType: String = "application/json; charset=utf-8"
): Request.Builder {
    val parsed = MediaType.parse(mediaType)
    return post(RequestBody.create(parsed, json))
}
