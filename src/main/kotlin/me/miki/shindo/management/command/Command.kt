package me.miki.shindo.management.command

import net.minecraft.client.Minecraft

abstract class Command(private val prefix: String) {

    val mc: Minecraft = Minecraft.getMinecraft()

    open fun onCommand(message: String) {}

    fun getPrefix(): String = prefix
}
