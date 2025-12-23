package me.miki.shindo.discord

import me.miki.shindo.Shindo
import me.miki.shindo.discord.ipc.IPCClient
import me.miki.shindo.discord.ipc.IPCListener
import me.miki.shindo.discord.ipc.entities.RichPresence
import me.miki.shindo.discord.ipc.exceptions.NoDiscordClientException
import me.miki.shindo.logger.ShindoLogger
import java.time.OffsetDateTime

class DiscordRPC {

    var client: IPCClient? = null
        private set

    fun start() {
        client = IPCClient(978250675576258610L).apply {
            setListener(object : IPCListener {
                override fun onReady(client: IPCClient) {
                    val builder = RichPresence.Builder()
                        .setState("Playing Shindo Client v${Shindo.getInstance().version}")
                        .setStartTimestamp(OffsetDateTime.now())
                        .setLargeImage("large")

                    client.sendRichPresence(builder.build())
                }
            })

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
