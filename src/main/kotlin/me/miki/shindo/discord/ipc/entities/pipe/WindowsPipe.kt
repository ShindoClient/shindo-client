package me.miki.shindo.discord.ipc.entities.pipe

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import me.miki.shindo.discord.ipc.IPCClient
import me.miki.shindo.discord.ipc.entities.Callback
import me.miki.shindo.discord.ipc.entities.Packet
import me.miki.shindo.discord.ipc.entities.serialize.PacketDeserializer
import org.apache.logging.log4j.LogManager
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

class WindowsPipe(
    ipcClient: IPCClient,
    callbacks: HashMap<String, Callback>,
    location: String,
) : Pipe(ipcClient, callbacks) {
    private val file: RandomAccessFile =
        try {
            RandomAccessFile(location, "rw")
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        file.write(b)
    }

    @Throws(IOException::class)
    override fun read(): Packet {
        while (file.length() == 0L && getStatus() == PipeStatus.CONNECTED) {
            try {
                Thread.sleep(50)
            } catch (_: InterruptedException) {
            }
        }

        if (getStatus() == PipeStatus.DISCONNECTED) {
            throw IOException("Disconnected!")
        }

        if (getStatus() == PipeStatus.CLOSED) {
            return Packet(Packet.OpCode.CLOSE, JsonObject())
        }

        val op = Packet.OpCode.values()[Integer.reverseBytes(file.readInt())]
        val len = Integer.reverseBytes(file.readInt())
        val d = ByteArray(len)
        file.readFully(d)

        val gson =
            GsonBuilder()
                .registerTypeAdapter(Packet::class.java, PacketDeserializer(op))
                .create()
        val jsonObject = gson.fromJson(String(d), JsonObject::class.java)
        val p = gson.fromJson(jsonObject, Packet::class.java)

        LOGGER.debug("Received packet: {}", p.toString())
        getListener()?.onPacketReceived(ipcClient, p)
        return p
    }

    @Throws(IOException::class)
    override fun close() {
        LOGGER.debug("Closing IPC pipe...")
        send(Packet.OpCode.CLOSE, JsonObject(), null)
        setStatus(PipeStatus.CLOSED)
        file.close()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(WindowsPipe::class.java)
    }
}
