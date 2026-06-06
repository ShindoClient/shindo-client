package com.shindoclient.shindo.logger

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.file.FileManager
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object ShindoLogManager {
    private val logger = LogManager.getLogger("Shindo Client")
    private val lock = ReentrantLock()
    private val timeFormat = DateTimeFormatter.ISO_INSTANT

    @JvmStatic
    fun log(
        level: LogLevel,
        message: String,
        category: LogCategory = LogCategory.AUTO,
        t: Throwable? = null,
    ) {
        val resolved = if (category == LogCategory.AUTO) inferCategory() else category
        val line = formatLine(level, resolved, message, t)

        logToConsole(level, line, t)
        append(getMainLogFile(), line)
        append(getCategoryLogFile(resolved), line)
    }

    private fun logToConsole(
        level: LogLevel,
        line: String,
        t: Throwable?,
    ) {
        when (level) {
            LogLevel.TRACE -> logger.trace(line, t)
            LogLevel.DEBUG -> logger.debug(line, t)
            LogLevel.INFO -> logger.info(line, t)
            LogLevel.WARN -> logger.warn(line, t)
            LogLevel.ERROR -> logger.error(line, t)
        }
    }

    private fun formatLine(
        level: LogLevel,
        category: LogCategory,
        message: String,
        t: Throwable?,
    ): String {
        val timestamp = timeFormat.format(Instant.now())
        val base = "$timestamp [${level.name}] [${category.name}] $message"
        if (t == null) {
            return base
        }
        val writer = StringWriter()
        t.printStackTrace(PrintWriter(writer))
        return base + "\n" + writer.toString().trimEnd()
    }

    private fun append(
        file: File?,
        message: String,
    ) {
        if (file == null) {
            return
        }
        TaskExecutor.runAsync(ThreadPoolType.IO) {
            lock.withLock {
                try {
                    file.appendText(message + "\n", Charsets.UTF_8)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun inferCategory(): LogCategory {
        val stack = Thread.currentThread().stackTrace
        for (element in stack) {
            val name = element.className
            if (!name.startsWith("com.shindoclient.shindo.")) {
                continue
            }
            if (name.startsWith("com.shindoclient.shindo.logger.")) {
                continue
            }
            return when {
                name.startsWith("com.shindoclient.shindo.management.mods.") -> LogCategory.MODS
                name.startsWith("com.shindoclient.shindo.api.websocket.") -> LogCategory.WEBSOCKET
                name.startsWith("com.shindoclient.shindo.management.music.") -> LogCategory.MUSIC
                name.startsWith("com.shindoclient.shindo.discord.") -> LogCategory.DISCORD
                name.startsWith("com.shindoclient.shindo.gui.") || name.startsWith("com.shindoclient.shindo.ui.") -> LogCategory.UI
                name.startsWith("com.shindoclient.shindo.management.security.") -> LogCategory.SECURITY
                name.startsWith("com.shindoclient.shindo.management.addons.") -> LogCategory.ADDONS
                name.startsWith("com.shindoclient.shindo.management.profile.") -> LogCategory.PROFILE
                else -> LogCategory.CORE
            }
        }
        return LogCategory.CORE
    }

    private fun getMainLogFile(): File? = getFile { it.mainLogFile }

    private fun getCategoryLogFile(category: LogCategory): File? =
        getFile { manager ->
            when (category) {
                LogCategory.CORE -> manager.coreLogFile
                LogCategory.MODS -> manager.modsLogFile
                LogCategory.WEBSOCKET -> manager.websocketLogFile
                LogCategory.CHAT -> manager.chatLogFile
                LogCategory.NOTIFICATIONS -> manager.notificationLogFile
                LogCategory.MUSIC -> manager.musicLogFile
                LogCategory.DISCORD -> manager.discordLogFile
                LogCategory.UI -> manager.uiLogFile
                LogCategory.SECURITY -> manager.securityLogFile
                LogCategory.ADDONS -> manager.addonsLogFile
                LogCategory.PROFILE -> manager.profileLogFile
                LogCategory.AUTO -> manager.coreLogFile
            }
        }

    private fun getFile(block: (FileManager) -> File): File? =
        try {
            block(Shindo.getInstance().getFileManager())
        } catch (ignored: Exception) {
            null
        }
}
