@file:JvmName("OkHttpResponseExtensions")

package me.miki.extensions.network.http.okhttp

import okhttp3.Response

/**
 * Reads response body safely and returns empty string when unavailable.
 */
fun Response.bodyStringOrEmpty(): String {
    val body = body() ?: return ""
    return try {
        body.string()
    } catch (ignored: Throwable) {
        ""
    }
}
