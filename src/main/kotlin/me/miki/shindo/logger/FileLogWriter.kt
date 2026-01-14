package me.miki.shindo.logger

object FileLogWriter {

    fun chat(message: String) {
        ShindoLogManager.log(LogLevel.INFO, message, LogCategory.CHAT, null)
    }

    fun notification(message: String) {
        ShindoLogManager.log(LogLevel.INFO, message, LogCategory.NOTIFICATIONS, null)
    }

    fun websocket(message: String) {
        ShindoLogManager.log(LogLevel.INFO, message, LogCategory.WEBSOCKET, null)
    }
}
