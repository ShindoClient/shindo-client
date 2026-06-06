package com.shindoclient.shindo.logger

object ShindoLogger {
    @JvmStatic
    fun info(message: String) {
        ShindoLogManager.log(LogLevel.INFO, message, LogCategory.AUTO, null)
    }

    @JvmStatic
    fun warn(message: String) {
        ShindoLogManager.log(LogLevel.WARN, message, LogCategory.AUTO, null)
    }

    @JvmStatic
    fun warn(
        message: String,
        e: Exception,
    ) {
        ShindoLogManager.log(LogLevel.WARN, message, LogCategory.AUTO, e)
    }

    @JvmStatic
    fun error(message: String) {
        ShindoLogManager.log(LogLevel.ERROR, message, LogCategory.AUTO, null)
    }

    @JvmStatic
    fun error(
        message: String,
        e: Exception,
    ) {
        ShindoLogManager.log(LogLevel.ERROR, message, LogCategory.AUTO, e)
    }

    @JvmStatic
    fun error(
        message: String,
        t: Throwable,
    ) {
        ShindoLogManager.log(LogLevel.ERROR, message, LogCategory.AUTO, t)
    }
}
