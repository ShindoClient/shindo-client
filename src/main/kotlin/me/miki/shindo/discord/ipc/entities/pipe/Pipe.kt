package me.miki.shindo.discord.ipc.entities.pipe

import com.google.gson.JsonObject
import me.miki.shindo.discord.ipc.IPCClient
import me.miki.shindo.discord.ipc.IPCListener
import me.miki.shindo.discord.ipc.entities.Callback
import me.miki.shindo.discord.ipc.entities.DiscordBuild
import me.miki.shindo.discord.ipc.entities.Packet
import me.miki.shindo.discord.ipc.exceptions.NoDiscordClientException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.Locale
import java.util.UUID

abstract class Pipe(
    val ipcClient: IPCClient,
    private val callbacks: HashMap<String, Callback>,
) {
    private var statusInternal: PipeStatus = PipeStatus.CONNECTING
    private var listenerInternal: IPCListener? = null
    private var build: DiscordBuild? = null

    companion object {
        private val LOGGER = LogManager.getLogger(Pipe::class.java)
        private const val VERSION = 1
        private val unixPaths = arrayOf("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")

        fun openPipe(
            ipcClient: IPCClient,
            clientId: Long,
            callbacks: HashMap<String, Callback>,
            vararg preferredOrder: DiscordBuild,
        ): Pipe {
            val order = if (preferredOrder.isEmpty()) arrayOf(DiscordBuild.ANY) else preferredOrder
            var pipe: Pipe? = null
            val open = arrayOfNulls<Pipe>(DiscordBuild.values().size)

            for (i in 0 until 10) {
                try {
                    val location = getPipeLocation(i)
                    LOGGER.debug("Searching for IPC: {}", location)
                    pipe = createPipe(ipcClient, callbacks, location)

                    val handshakeJson = JsonObject()
                    handshakeJson.addProperty("v", VERSION)
                    handshakeJson.addProperty("client_id", clientId.toString())
                    pipe.send(Packet.OpCode.HANDSHAKE, handshakeJson, null)

                    val p = pipe.read()
                    val json = p.json()
                    var apiEndpoint = DiscordBuild.ANY.name

                    if (json.has("data")) {
                        val data = json.getAsJsonObject("data")
                        if (data.has("config")) {
                            val config = data.getAsJsonObject("config")
                            if (config.has("api_endpoint")) {
                                apiEndpoint = config.get("api_endpoint").asString
                            }
                        }
                    }

                    pipe.build = DiscordBuild.from(apiEndpoint)
                    LOGGER.debug("Found a valid client ({}) with packet: {}", pipe.build?.name, p)

                    if (pipe.build == order[0] || DiscordBuild.ANY == order[0]) {
                        LOGGER.info("Found preferred client: ${pipe.build?.name}")
                        break
                    }

                    val activePipe = pipe
                    val activeBuild = activePipe.build!!
                    open[activeBuild.ordinal] = activePipe
                    open[DiscordBuild.ANY.ordinal] = activePipe

                    activePipe.build = null
                    pipe = null
                } catch (_: Exception) {
                    pipe = null
                }
            }

            if (pipe == null) {
                for (i in 1 until order.size) {
                    val cb = order[i]
                    LOGGER.debug("Looking for client build: ${cb.name}")
                    val candidate = open[cb.ordinal]
                    if (candidate != null) {
                        pipe = candidate
                        open[cb.ordinal] = null
                        if (cb == DiscordBuild.ANY) {
                            for (k in open.indices) {
                                if (open[k] == pipe) {
                                    pipe.build = DiscordBuild.values()[k]
                                    open[k] = null
                                }
                            }
                        } else {
                            pipe!!.build = cb
                        }
                        LOGGER.info("Found preferred client: ${pipe.build?.name}")
                        break
                    }
                }
                if (pipe == null) {
                    throw NoDiscordClientException()
                }
            }

            for (i in open.indices) {
                if (i == DiscordBuild.ANY.ordinal) continue
                open[i]?.let {
                    try {
                        it.close()
                    } catch (ex: Exception) {
                        LOGGER.debug("Failed to close an open IPC pipe!", ex)
                    }
                }
            }

            pipe.statusInternal = PipeStatus.CONNECTED
            return pipe
        }

        private fun createPipe(
            ipcClient: IPCClient,
            callbacks: HashMap<String, Callback>,
            location: String,
        ): Pipe {
            val osName = System.getProperty("os.name").uppercase(Locale.ROOT)
            return if (osName.contains("win")) {
                WindowsPipe(ipcClient, callbacks, location)
            } else {
                throw RuntimeException("Unsupported OS: $osName")
            }
        }

        private fun generateNonce(): String = UUID.randomUUID().toString()

        private fun getPipeLocation(i: Int): String {
            if (System.getProperty("os.name").contains("Win")) {
                return "\\\\?\\pipe\\discord-ipc-$i"
            }

            var tmppath: String? = null
            for (str in unixPaths) {
                tmppath = System.getenv(str)
                if (tmppath != null) break
            }
            if (tmppath == null) tmppath = "/tmp"
            return "$tmppath/discord-ipc-$i"
        }
    }

    fun send(
        op: Packet.OpCode,
        data: JsonObject,
        callback: Callback?,
    ) {
        try {
            val nonce = generateNonce()
            data.addProperty("nonce", nonce)
            val p = Packet(op, data)
            if (callback != null && !callback.isEmpty()) {
                callbacks[nonce] = callback
            }
            write(p.toBytes())
            LOGGER.debug("Sent packet: {}", p)
            getListener()?.onPacketSent(ipcClient, p)
        } catch (ex: IOException) {
            LOGGER.error("Encountered an IOException while sending a packet and disconnected!", ex)
            statusInternal = PipeStatus.DISCONNECTED
        }
    }

    @Throws(IOException::class)
    abstract fun read(): Packet

    @Throws(IOException::class)
    abstract fun write(b: ByteArray)

    fun getStatus(): PipeStatus = statusInternal

    fun setStatus(status: PipeStatus) {
        this.statusInternal = status
    }

    fun getListener(): IPCListener? = listenerInternal

    fun setListener(listener: IPCListener?) {
        this.listenerInternal = listener
    }

    @Throws(IOException::class)
    abstract fun close()

    val discordBuild: DiscordBuild?
        get() = build
}
