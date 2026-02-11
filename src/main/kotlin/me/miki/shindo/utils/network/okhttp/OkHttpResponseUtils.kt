package me.miki.shindo.utils.network.okhttp

import okhttp3.Response

data class HttpResponseData(
    val code: Int,
    val body: String,
    val successful: Boolean
)

object OkHttpResponseUtils {

    @JvmStatic
    fun toResponseData(response: Response): HttpResponseData {
        val body = response.body()?.string() ?: ""
        return HttpResponseData(
            code = response.code(),
            body = body,
            successful = response.isSuccessful
        )
    }
}
