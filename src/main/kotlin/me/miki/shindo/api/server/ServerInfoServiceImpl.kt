package me.miki.shindo.api.server

import me.miki.client_api.server.ServerInfoService
import me.miki.shindo.utils.ServerUtils

/**
 * Implementação de [ServerInfoService] baseada em [ServerUtils].
 */
class ServerInfoServiceImpl : ServerInfoService {

    override fun getServerIp(): String = ServerUtils.getServerIP()

    override fun isOnServer(): Boolean = ServerUtils.isJoinServer()

    override fun isOnHypixel(): Boolean = ServerUtils.isHypixel()
}

