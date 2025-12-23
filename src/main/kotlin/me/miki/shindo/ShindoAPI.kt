package me.miki.shindo

import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.api.websocket.AccountType
import me.miki.shindo.api.websocket.ShindoWebsocket
import me.miki.shindo.api.websocket.WsIdentity
import me.miki.shindo.api.websocket.presence.PresenceTracker
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.file.FileManager
import net.minecraft.client.Minecraft
import java.io.File
import java.lang.reflect.Method
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.function.Supplier

class ShindoAPI {

    val roleManager = RoleManager()
    val presence = PresenceTracker()

    val firstLoginFile: File
    var launchTime: Long = 0
        private set
    lateinit var modMenu: GuiModMenu
        private set
    lateinit var mainMenu: GuiShindoMainMenu
        private set
    var ws: ShindoWebsocket? = null
        private set

    init {
        val fileManager: FileManager = Shindo.getInstance().fileManager
        firstLoginFile = File(fileManager.cacheDir, "first.tmp")
    }

    fun init() {
        launchTime = System.currentTimeMillis()
        modMenu = GuiModMenu()
        mainMenu = GuiShindoMainMenu()
    }

    fun start() {
        val uuidSup: Supplier<String> = Supplier {
            try {
                getEffectiveUuid().toString()
            } catch (e: Exception) {
                ""
            }
        }

        val nameSup: Supplier<String> = Supplier {
            try {
                Minecraft.getMinecraft().session.username
            } catch (e: Exception) {
                ""
            }
        }

        val typeSup: Supplier<String> = Supplier {
            try {
                val mc = Minecraft.getMinecraft()
                val session = mc.session ?: return@Supplier "LOCAL"
                val uuid = session.profile.id
                if (uuid == null) return@Supplier "LOCAL"

                try {
                    val getSessionType: Method = session.javaClass.getMethod("getSessionType")
                    val sessionType = getSessionType.invoke(session) as String
                    if (sessionType != null && (sessionType == "msa" || sessionType.contains("microsoft"))) {
                        return@Supplier "MICROSOFT"
                    }
                } catch (e: Exception) {
                    ShindoLogger.error("An error occurred while trying to get the session type", e)
                }

                try {
                    val getToken: Method = session.javaClass.getMethod("getToken")
                    val token = getToken.invoke(session) as String
                    if (!token.isNullOrEmpty()) {
                        return@Supplier "MICROSOFT"
                    }
                } catch (e: Exception) {
                    ShindoLogger.error("An error occurred while trying to get the token", e)
                }

                "OFFLINE"
            } catch (e: Exception) {
                "LOCAL"
            }
        }

        ws = ShindoWebsocket(URI.create("wss://ws.shindoclient.com/websocket"), true, presence).apply {
            roleManager = this@ShindoAPI.roleManager
            provider = object : ShindoWebsocket.IdentityProvider {
                override fun player(): WsIdentity? {
                    var rawUuid = safeTrim(uuidSup.get())
                    var rawName = safeTrim(nameSup.get())
                    if (rawName.isEmpty()) rawName = "Player"

                    val parsed = safeUUID(rawUuid)
                    var effectiveUuid = rawUuid
                    var accountType = AccountType.from(typeSup.get())

                    if (parsed == null) {
                        effectiveUuid = generateOfflineUuid(rawName)
                        accountType = AccountType.LOCAL
                    }

                    return WsIdentity(
                        effectiveUuid,
                        rawName,
                        null,
                        accountType
                    )
                }
            }
            connect()
        }
    }

    fun stop() {
        ws?.disconnect()
        ws = null
    }

    fun createFirstLoginFile() {
        Shindo.getInstance().fileManager.createFile(firstLoginFile)
    }

    fun isFirstLogin(): Boolean = !firstLoginFile.exists()

    fun getEffectiveUuid(): UUID {
        return resolveEffectiveUuid(Minecraft.getMinecraft())
    }

    private fun safeTrim(value: String?): String = value?.trim() ?: ""

    private fun safeUUID(value: String?): UUID? = try {
        if (value.isNullOrEmpty()) null else UUID.fromString(value)
    } catch (ignored: Exception) {
        null
    }

    private fun generateOfflineUuid(name: String?): String {
        val baseName = if (name.isNullOrEmpty()) "Player" else name
        return UUID.nameUUIDFromBytes(("OfflinePlayer:$baseName").toByteArray(StandardCharsets.UTF_8)).toString()
    }

    private fun resolveEffectiveUuid(mc: Minecraft): UUID {
        val session = mc.session
        val profileId = session?.profile?.id
        if (profileId != null) {
            return profileId
        }
        val playerId = mc.thePlayer?.uniqueID
        if (playerId != null) {
            return playerId
        }
        val name = session?.username ?: "Player"
        return UUID.nameUUIDFromBytes(("OfflinePlayer:$name").toByteArray(StandardCharsets.UTF_8))
    }
}
