package me.miki.shindo

import com.google.gson.JsonObject
import me.miki.shindo.api.roles.RoleManager
import me.miki.shindo.api.websocket.AccountType
import me.miki.shindo.api.websocket.ShindoWebsocket
import me.miki.shindo.api.websocket.WsIdentity
import me.miki.shindo.api.websocket.message.MessageType
import me.miki.shindo.api.websocket.presence.PresenceTracker
import me.miki.shindo.gui.GuiNavigationHub
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.file.FileManager
import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Supplier

@Suppress("unused")
class ShindoAPI {

    private val roleManager = RoleManager()
    val presence = PresenceTracker()

    private val firstLoginFile: File
    var launchTime: Long = 0
        private set
    lateinit var modMenu: GuiModMenu
        private set
    lateinit var mainMenu: GuiShindoMainMenu
        private set
    lateinit var navigationHub: GuiNavigationHub
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
        navigationHub = GuiNavigationHub()
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
                resolveAccountType(Minecraft.getMinecraft()).name
            } catch (e: Exception) {
                "LOCAL"
            }
        }

        ws = ShindoWebsocket(URI.create("wss://ws.shindoclient.com/websocket"), true, presence).apply {
            roleManager = this@ShindoAPI.roleManager
            provider = object : ShindoWebsocket.IdentityProvider {
                override fun player(): WsIdentity {
                    val rawUuid = safeTrim(uuidSup.get())
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
            messageHandler.addObserver(BiConsumer<MessageType, JsonObject?> { type, payload ->
                Shindo.getInstance().profileShareManager.handleMessage(type, payload)
            })
            messageHandler.addObserver(BiConsumer<MessageType, JsonObject?> { type, payload ->
                Shindo.getInstance().chatManager.handleMessage(type, payload)
            })
            messageHandler.addObserver(BiConsumer<MessageType, JsonObject?> { type, payload ->
                Shindo.getInstance().broadcastManager.handleMessage(type, payload)
            })
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

    fun getAccountType(): AccountType {
        return resolveAccountType(Minecraft.getMinecraft())
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

    private fun resolveAccountType(mc: Minecraft): AccountType {
        val session = mc.session ?: return AccountType.LOCAL

        try {
            val sessionType = session.sessionType
            if (sessionType != null && (sessionType.equals(Session.Type.MOJANG) || sessionType.equals(Session.Type.LEGACY))) {
                return AccountType.MICROSOFT
            }
        } catch (e: Exception) {
            ShindoLogger.error("An error occurred while trying to get the session type", e)
        }

        try {
            val token = session.token
            if (token.isNotEmpty()) {
                return AccountType.MICROSOFT
            }
        } catch (e: Exception) {
            ShindoLogger.error("An error occurred while trying to get the token", e)
        }

        return AccountType.OFFLINE
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
