package com.shindoclient.shindo

import com.google.gson.JsonObject
import com.shindoclient.shindo.api.roles.RoleManager
import com.shindoclient.shindo.api.websocket.AccountType
import com.shindoclient.shindo.api.websocket.ShindoWebsocket
import com.shindoclient.shindo.api.websocket.WsIdentity
import com.shindoclient.shindo.api.websocket.message.MessageType
import com.shindoclient.shindo.api.websocket.presence.PresenceTracker
import com.shindoclient.shindo.gui.GuiNavigationHub
import com.shindoclient.shindo.gui.mainmenu.GuiShindoMainMenu
import com.shindoclient.shindo.gui.modmenu.v2.GuiModMenu
import com.shindoclient.shindo.management.file.FileManager
import com.shindoclient.shindo.utils.AccountUtil
import net.minecraft.client.Minecraft
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.function.BiConsumer

@Suppress("unused", "RedundantSamConstructor")
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
        val fileManager: FileManager = Shindo.getInstance().getFileManager()
        firstLoginFile = File(fileManager.cacheDir, "first.tmp")
    }

    fun init() {
        launchTime = System.currentTimeMillis()
        modMenu = GuiModMenu()
        mainMenu = GuiShindoMainMenu()
        navigationHub = GuiNavigationHub()
    }

    fun start() {
        ws =
            ShindoWebsocket("wss://ws.shindoclient.com/websocket", presence).apply {
                roleManager = this@ShindoAPI.roleManager
                provider =
                    object : ShindoWebsocket.IdentityProvider {
                        override fun player(): WsIdentity = buildIdentity()
                    }

                messageHandler.addObserver(
                    BiConsumer<MessageType, JsonObject?> { type, payload ->
                        Shindo.getInstance().getProfileShareManager().handleMessage(type, payload)
                    },
                )
                messageHandler.addObserver(
                    BiConsumer<MessageType, JsonObject?> { type, payload ->
                        Shindo.getInstance().getChatManager().handleMessage(type, payload)
                    },
                )
                messageHandler.addObserver(
                    BiConsumer<MessageType, JsonObject?> { type, payload ->
                        Shindo.getInstance().getBroadcastManager().handleMessage(type, payload)
                    },
                )

                connect()
            }
    }

    fun stop() {
        ws?.disconnect()
        ws = null
    }

    fun createFirstLoginFile() {
        Shindo.getInstance().getFileManager().createFile(firstLoginFile)
    }

    fun isFirstLogin(): Boolean = !firstLoginFile.exists()

    fun getEffectiveUuid(): UUID = resolveEffectiveUuid(Minecraft.getMinecraft())

    fun getAccountType(): AccountType = resolveAccountType(Minecraft.getMinecraft())

    private fun buildIdentity(): WsIdentity {
        val mc = Minecraft.getMinecraft()
        val session = mc.session

        val name = session?.username?.trim()?.takeIf { it.isNotEmpty() } ?: "Player"

        // Profile UUID is the authoritative source; fall back to entity UUID.
        val profileUuid: UUID? = session?.profile?.id ?: mc.thePlayer?.uniqueID

        val accountType = AccountUtil.detectAccountTypeFromUuid(profileUuid)

        val uuidString =
            profileUuid?.toString()
                ?: UUID
                    .nameUUIDFromBytes(
                        ("OfflinePlayer:$name").toByteArray(StandardCharsets.UTF_8),
                    ).toString()

        return WsIdentity(uuidString, name, null, accountType)
    }

    private fun resolveEffectiveUuid(mc: Minecraft): UUID {
        mc.session
            ?.profile
            ?.id
            ?.let { return it }
        mc.thePlayer?.uniqueID?.let { return it }
        val name = mc.session?.username ?: "Player"
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:$name").toByteArray(StandardCharsets.UTF_8),
        )
    }

    private fun resolveAccountType(mc: Minecraft): AccountType = AccountUtil.detectAccountTypeFromUuid(resolveEffectiveUuid(mc))
}
