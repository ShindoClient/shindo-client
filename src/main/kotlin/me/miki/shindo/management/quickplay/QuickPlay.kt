package me.miki.shindo.management.quickplay

import net.minecraft.util.ResourceLocation

abstract class QuickPlay(
    private val name: String,
    private val icon: ResourceLocation
) {
    private val commands = ArrayList<QuickPlayCommand>()

    init {
        addCommands()
    }

    abstract fun addCommands()

    fun getName(): String = name
    fun getCommands(): ArrayList<QuickPlayCommand> = commands
    fun setCommands(list: ArrayList<QuickPlayCommand>) {
        commands.clear()
        commands.addAll(list)
    }
    fun getIcon(): ResourceLocation = icon
}
