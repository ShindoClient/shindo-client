package me.miki.shindo.management.addons.nocheaters.data

import java.util.*

object WdrData {

    private var dataInstance: NoCheatersData? = null

    fun initialize(data: NoCheatersData) {
        dataInstance = data
    }

    fun getWDR(uuid: UUID?, playername: String?): WDR? {
        return dataInstance?.getWDR(uuid, playername)
    }

    fun getAllWDRs(): Map<Any, WDR> {
        return dataInstance?.getAllWDRs() ?: emptyMap()
    }

    fun put(uuid: UUID?, playername: String?, wdr: WDR) {
        dataInstance?.put(uuid, playername, wdr)
    }

    fun remove(uuid: UUID?, playername: String?): WDR? {
        return dataInstance?.remove(uuid, playername)
    }
}
