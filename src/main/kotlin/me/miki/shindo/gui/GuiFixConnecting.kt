package me.miki.shindo.gui

import me.miki.shindo.management.event.impl.EventJoinServer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.multiplayer.ServerAddress
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.network.NetHandlerLoginClient
import net.minecraft.client.resources.I18n
import net.minecraft.network.EnumConnectionState
import net.minecraft.network.NetworkManager
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.login.client.C00PacketLoginStart
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation
import org.apache.logging.log4j.LogManager
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicInteger

class GuiFixConnecting : GuiScreen {
    private val previousGuiScreen: GuiScreen
    private var networkManager: NetworkManager? = null
    private var cancel = false

    constructor(parent: GuiScreen, mcIn: Minecraft, serverData: ServerData) {
        mc = mcIn
        previousGuiScreen = parent
        mcIn.loadWorld(null)
        mcIn.setServerData(serverData)
        connectServerData(serverData)
    }

    constructor(parent: GuiScreen, mcIn: Minecraft, hostName: String, port: Int) {
        mc = mcIn
        previousGuiScreen = parent
        mcIn.loadWorld(null)
        connect(hostName, port)
    }

    private fun connectServerData(serverData: ServerData) {
        EventJoinServer(serverData.serverIP).call()

        object : Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
            override fun run() {
                var inetAddress: InetAddress? = null
                val serverAddress = ServerAddress.fromString(serverData.serverIP)
                val ip = serverAddress.ip
                val port = serverAddress.port

                try {
                    if (cancel) {
                        return
                    }

                    inetAddress = resolveAddress(ip, port)
                    networkManager =
                        NetworkManager.createNetworkManagerAndConnect(
                            inetAddress,
                            port,
                            mc.gameSettings.isUsingNativeTransport,
                        )
                    networkManager?.netHandler = NetHandlerLoginClient(networkManager, mc, previousGuiScreen)
                    networkManager?.sendPacket(C00Handshake(47, ip, port, EnumConnectionState.LOGIN))
                    networkManager?.sendPacket(C00PacketLoginStart(mc.session.profile))
                } catch (unknownhostexception: UnknownHostException) {
                    if (cancel) {
                        return
                    }

                    logger.error("Couldn't connect to server", unknownhostexception)
                    mc.displayGuiScreen(
                        GuiDisconnected(
                            previousGuiScreen,
                            "connect.failed",
                            ChatComponentTranslation("disconnect.genericReason", "Unknown host"),
                        ),
                    )
                } catch (exception: Exception) {
                    if (cancel) {
                        return
                    }

                    logger.error("Couldn't connect to server", exception)
                    var reason = exception.toString()

                    if (inetAddress != null) {
                        val address = "$inetAddress:$port"
                        reason = reason.replace(address, "")
                    }

                    mc.displayGuiScreen(
                        GuiDisconnected(
                            previousGuiScreen,
                            "connect.failed",
                            ChatComponentTranslation("disconnect.genericReason", reason),
                        ),
                    )
                }
            }
        }.start()
    }

    private fun connect(
        ip: String,
        port: Int,
    ) {
        EventJoinServer(ip).call()

        logger.info("Connecting to $ip, $port")
        object : Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
            override fun run() {
                var inetAddress: InetAddress? = null

                try {
                    if (cancel) {
                        return
                    }

                    inetAddress = resolveAddress(ip, port)
                    networkManager =
                        NetworkManager.createNetworkManagerAndConnect(
                            inetAddress,
                            port,
                            mc.gameSettings.isUsingNativeTransport,
                        )
                    networkManager?.netHandler = NetHandlerLoginClient(networkManager, mc, previousGuiScreen)
                    networkManager?.sendPacket(C00Handshake(47, ip, port, EnumConnectionState.LOGIN))
                    networkManager?.sendPacket(C00PacketLoginStart(mc.session.profile))
                } catch (unknownhostexception: UnknownHostException) {
                    if (cancel) {
                        return
                    }

                    logger.error("Couldn't connect to server", unknownhostexception)
                    mc.displayGuiScreen(
                        GuiDisconnected(
                            previousGuiScreen,
                            "connect.failed",
                            ChatComponentTranslation("disconnect.genericReason", "Unknown host"),
                        ),
                    )
                } catch (exception: Exception) {
                    if (cancel) {
                        return
                    }

                    logger.error("Couldn't connect to server", exception)
                    var reason = exception.toString()

                    if (inetAddress != null) {
                        val address = "$inetAddress:$port"
                        reason = reason.replace(address, "")
                    }

                    mc.displayGuiScreen(
                        GuiDisconnected(
                            previousGuiScreen,
                            "connect.failed",
                            ChatComponentTranslation("disconnect.genericReason", reason),
                        ),
                    )
                }
            }
        }.start()
    }

    override fun updateScreen() {
        if (networkManager != null) {
            if (networkManager!!.isChannelOpen) {
                networkManager!!.processReceivedPackets()
            } else {
                networkManager!!.checkDisconnected()
            }
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
    }

    override fun initGui() {
        buttonList.clear()
        buttonList.add(GuiButton(0, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            cancel = true

            if (networkManager != null) {
                networkManager!!.closeChannel(ChatComponentText("Aborted"))
            }

            mc.displayGuiScreen(previousGuiScreen)
        }
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        drawDefaultBackground()

        if (networkManager == null) {
            drawCenteredString(fontRendererObj, I18n.format("connect.connecting"), width / 2, height / 2 - 50, 16777215)
        } else {
            drawCenteredString(
                fontRendererObj,
                I18n.format("connect.authorizing"),
                width / 2,
                height / 2 - 50,
                16777215,
            )
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(UnknownHostException::class)
    private fun resolveAddress(
        host: String,
        port: Int,
    ): InetAddress = InetAddress.getByName(host)

    companion object {
        private val CONNECTION_ID = AtomicInteger(0)
        private val logger = LogManager.getLogger()
    }
}
