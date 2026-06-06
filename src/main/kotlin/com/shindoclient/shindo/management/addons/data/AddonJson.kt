package com.shindoclient.shindo.management.addons.data

data class AddonJson(
    val id: String = "",
    val name: String = "",
    val version: String = "",
    val description: String = "",
    val author: String = "",
    val icon: String = "",
    val type: String = "OTHER",
    val main: String = "",
    val minClientVersion: String = "",
    val clientVersion: String = "",
    val minecraft: MinecraftVersionJson? = null,
)
