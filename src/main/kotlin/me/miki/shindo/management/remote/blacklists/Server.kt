package me.miki.shindo.management.remote.blacklists

import java.util.concurrent.CopyOnWriteArrayList

class Server(
    val serverIp: String,
    val mods: CopyOnWriteArrayList<String>
)
