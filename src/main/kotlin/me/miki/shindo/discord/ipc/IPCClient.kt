package me.miki.shindo.discord.ipc

import com.google.gson.JsonObject
import me.miki.shindo.discord.ipc.entities.*
import me.miki.shindo.discord.ipc.entities.Packet.OpCode
import me.miki.shindo.discord.ipc.entities.pipe.Pipe
import me.miki.shindo.discord.ipc.entities.pipe.PipeStatus
import me.miki.shindo.discord.ipc.exceptions.NoDiscordClientException
import org.apache.logging.log4j.LogManager
import java.io.Closeable
import java.io.IOException
import java.lang.management.ManagementFactory

class IPCClient(
    private val clientId: Long,
) : Closeable {
    @Volatile
    private var pipe: Pipe? = null
    private var listener: IPCListener? = null
    private var readThread: Thread? = null
    private val callbacks = HashMap<String, Callback>()

    fun setListener(listener: IPCListener) {
        this.listener = listener
        pipe?.setListener(listener)
    }

    @Throws(NoDiscordClientException::class)
    fun connect(vararg preferredOrder: DiscordBuild) {
        if (isConnected(false)) return

        callbacks.clear()
        pipe = Pipe.openPipe(this, clientId, callbacks, *preferredOrder)

        LOGGER.debug("Client is now connected and ready!")
        listener?.onReady(this)
        startReading()
    }

    fun sendRichPresence(presence: RichPresence?) {
        sendRichPresence(presence, null)
    }

    fun sendRichPresence(
        presence: RichPresence?,
        callback: Callback?,
    ) {
        if (isConnected(true)) return

        LOGGER.debug("Sending RichPresence to discord: ${presence?.toJson()?.toString()}")

        val argsObject =
            JsonObject().apply {
                addProperty("pid", pid)
                add("activity", presence?.toJson())
            }

        val jsonObject =
            JsonObject().apply {
                addProperty("cmd", "SET_ACTIVITY")
                add("args", argsObject)
            }

        pipe?.send(OpCode.FRAME, jsonObject, callback)
    }

    fun subscribe(sub: Event) {
        subscribe(sub, null)
    }

    fun subscribe(
        sub: Event,
        callback: Callback?,
    ) {
        if (isConnected(true)) return
        if (!sub.isSubscribable) throw IllegalStateException("Cannot subscribe to $sub event!")

        LOGGER.debug("Subscribing to Event: ${sub.name}")
        val jsonObject = JsonObject()
        jsonObject.addProperty("cmd", "SUBSCRIBE")
        jsonObject.addProperty("evt", sub.name)

        pipe?.send(OpCode.FRAME, jsonObject, callback)
    }

    val status: PipeStatus
        get() = pipe?.getStatus() ?: PipeStatus.UNINITIALIZED

    override fun close() {
        if (isConnected(true)) return
        try {
            pipe?.close()
        } catch (e: IOException) {
            LOGGER.debug("Failed to close pipe", e)
        }
    }

    val discordBuild: DiscordBuild?
        get() = pipe?.discordBuild

    private fun isConnected(connected: Boolean): Boolean =
        if (connected) {
            status != PipeStatus.CONNECTED
        } else {
            status == PipeStatus.CONNECTED
        }

    private fun startReading() {
        readThread =
            Thread({
                try {
                    var p: Packet
                    while (pipe?.read().also { p = it!! }!!.op != OpCode.CLOSE) {
                        val json = p.json()
                        val event = Event.of(if (json.has("evt")) json.get("evt").asString else null)
                        val nonce = if (json.has("nonce")) json.get("nonce").asString else null

                        when (event) {
                            Event.NULL -> {
                                if (nonce != null && callbacks.containsKey(nonce)) {
                                    callbacks.remove(nonce)?.succeed(p)
                                }
                            }

                            Event.ERROR -> {
                                if (nonce != null && callbacks.containsKey(nonce)) {
                                    val data = json.getAsJsonObject("data")
                                    callbacks
                                        .remove(nonce)
                                        ?.fail(if (data.has("message")) data.get("message").asString else null)
                                }
                            }

                            Event.ACTIVITY_JOIN -> {
                                LOGGER.debug("Reading thread received a 'join' event.")
                            }

                            Event.ACTIVITY_SPECTATE -> {
                                LOGGER.debug("Reading thread received a 'spectate' event.")
                            }

                            Event.ACTIVITY_JOIN_REQUEST -> {
                                LOGGER.debug("Reading thread received a 'join request' event.")
                            }

                            Event.UNKNOWN -> {
                                LOGGER.debug(
                                    "Reading thread encountered an event with an unknown type: ${json.get(
                                        "evt",
                                    ).asString}",
                                )
                            }

                            else -> {}
                        }

                        if (listener != null && json.has("cmd") && json.get("cmd").asString == "DISPATCH") {
                            try {
                                val data = json.getAsJsonObject("data")
                                when (Event.of(json.get("evt").asString)) {
                                    Event.ACTIVITY_JOIN -> {
                                        listener?.onActivityJoin(this, data.get("secret").asString)
                                    }

                                    Event.ACTIVITY_SPECTATE -> {
                                        listener?.onActivitySpectate(
                                            this,
                                            data.get("secret").asString,
                                        )
                                    }

                                    Event.ACTIVITY_JOIN_REQUEST -> {
                                        val u = data.getAsJsonObject("user")
                                        val user =
                                            User(
                                                u.get("username").asString,
                                                u.get("discriminator").asString,
                                                u.get("id").asLong,
                                                if (u.has("avatar")) u.get("avatar").asString else null,
                                            )
                                        listener?.onActivityJoinRequest(
                                            this,
                                            if (data.has("secret")) data.get("secret").asString else null,
                                            user,
                                        )
                                    }

                                    else -> {}
                                }
                            } catch (e: Exception) {
                                LOGGER.error("Exception when handling event: ", e)
                            }
                        }
                    }

                    pipe?.setStatus(PipeStatus.DISCONNECTED)
                    listener?.onClose(this, p.json())
                } catch (ex: IOException) {
                    LOGGER.error("Reading thread encountered an IOException", ex)
                    pipe?.setStatus(PipeStatus.DISCONNECTED)
                    listener?.onDisconnect(this, ex)
                }
            }, "IPC-Reader")

        LOGGER.debug("Starting IPCClient reading thread!")
        readThread?.start()
    }

    enum class Event(
        val isSubscribable: Boolean,
    ) {
        NULL(false),
        READY(false),
        ERROR(false),
        ACTIVITY_JOIN(true),
        ACTIVITY_SPECTATE(true),
        ACTIVITY_JOIN_REQUEST(true),
        UNKNOWN(false),
        ;

        companion object {
            fun of(str: String?): Event {
                if (str == null) return NULL
                return values().firstOrNull { it != UNKNOWN && it.name.equals(str, ignoreCase = true) } ?: UNKNOWN
            }
        }
    }

    private val pid: Int
        get() {
            val pr = ManagementFactory.getRuntimeMXBean().name
            return Integer.parseInt(pr.substring(0, pr.indexOf('@')))
        }

    companion object {
        private val LOGGER = LogManager.getLogger(IPCClient::class.java)
    }
}
