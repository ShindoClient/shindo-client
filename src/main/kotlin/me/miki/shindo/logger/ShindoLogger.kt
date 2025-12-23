package me.miki.shindo.logger

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object ShindoLogger {

    @JvmStatic
    val logger: Logger = LogManager.getLogger("Shindo Client")

    @JvmStatic
    fun info(message: String) {
        logger.info("[SC/INFO] {}", message)
    }

    @JvmStatic
    fun warn(message: String) {
        logger.warn("[SC/WARN] {}", message)
    }

    @JvmStatic
    fun warn(message: String, e: Exception) {
        logger.warn("[SC/WARN] {}", message, e)
    }

    @JvmStatic
    fun error(message: String) {
        logger.error("[SC/ERROR] {}", message)
    }

    @JvmStatic
    fun error(message: String, e: Exception) {
        logger.error("[SC/ERROR] {}", message, e)
    }

    @JvmStatic
    fun error(message: String, t: Throwable) {
        logger.error("[SC/ERROR] {}", message, t)
    }
}
