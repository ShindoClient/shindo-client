package com.shindoclient.shindo.management.quickplay

class QuickPlayCommand(
    private val name: String,
    private val command: String,
) {
    fun getName(): String = name

    fun getCommand(): String = command
}
