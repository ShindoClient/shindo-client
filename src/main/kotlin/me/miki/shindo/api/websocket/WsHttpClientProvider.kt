package me.miki.shindo.api.websocket

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

internal object WsHttpClientProvider {

    val instance: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)    // no read deadline
            .pingInterval(0, TimeUnit.MILLISECONDS)   // manual heartbeat
            .retryOnConnectionFailure(false)           // ShindoWebsocket owns retry
            .build()
    }
}
