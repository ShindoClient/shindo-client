package com.shindoclient.shindo.discord

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.discord.ipc.IPCClient
import com.shindoclient.shindo.discord.ipc.IPCListener
import com.shindoclient.shindo.discord.ipc.entities.RichPresence
import com.shindoclient.shindo.discord.ipc.exceptions.NoDiscordClientException
import com.shindoclient.shindo.logger.ShindoLogger
import java.time.OffsetDateTime

class DiscordRPC {
    var client: IPCClient? = null
        private set

    fun start() {
        client =
            IPCClient(978250675576258610L).apply {
                setListener(
                    object : IPCListener {
                        override fun onReady(client: IPCClient) {
                            val builder =
                                RichPresence
                                    .Builder()
                                    .setState("Playing Shindo Client v${Shindo.getInstance().getVersion()}")
                                    .setStartTimestamp(OffsetDateTime.now())
                                    .setLargeImage("large")

                            client.sendRichPresence(builder.build())
                        }
                    },
                )

                try {
                    connect()
                } catch (e: NoDiscordClientException) {
                    ShindoLogger.error("An error occurred while connecting to the Discord IPC Client", e)
                }
            }
    }

    fun stop() {
        client?.close()
    }

    fun isStarted(): Boolean = client != null
}
