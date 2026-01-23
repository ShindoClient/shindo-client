package me.miki.shindo.management.addons.nocheaters.data

/**
 * Singleton para acesso aos dados do NoCheaters
 * Facilita acesso global aos dados
 */
object WdrData {
    
    private var dataInstance: NoCheatersData? = null
    
    fun initialize(data: NoCheatersData) {
        dataInstance = data
    }
    
    fun getWDR(uuid: java.util.UUID?, playername: String?): WDR? {
        return dataInstance?.getWDR(uuid, playername)
    }
    
    fun getAllWDRs(): Map<Any, WDR> {
        return dataInstance?.getAllWDRs() ?: emptyMap()
    }
    
    fun put(uuid: java.util.UUID?, playername: String?, wdr: WDR) {
        dataInstance?.put(uuid, playername, wdr)
    }
    
    fun remove(uuid: java.util.UUID?, playername: String?): WDR? {
        return dataInstance?.remove(uuid, playername)
    }
}
