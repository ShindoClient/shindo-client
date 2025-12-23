package me.miki.shindo.discord.ipc.entities

class Callback(
    private val success: ((Packet) -> Unit)? = null,
    private val failure: ((String?) -> Unit)? = null
) {
    fun isEmpty(): Boolean = success == null && failure == null

    fun succeed(packet: Packet) {
        success?.invoke(packet)
    }

    fun fail(message: String?) {
        failure?.invoke(message)
    }
}
