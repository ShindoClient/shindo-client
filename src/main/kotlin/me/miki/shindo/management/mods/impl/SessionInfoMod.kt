package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventJoinServer
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ServerUtils.getServerIP
import me.miki.shindo.utils.ServerUtils.isHypixel
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.StringUtils
import java.util.*

class SessionInfoMod :
    HUDMod(TranslateText.SESSION_INFO, TranslateText.SESSION_INFO_DESCRIPTION, LegacyIcon.MOD_SESSION_INFO, "stats") {
    private val killTrigger = arrayOf<String?>("by *", "para *", "fue destrozado a manos de *")
    private var killCount = 0
    private var startTime: Long = 0

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        nvg!!.setupAndDraw(Runnable { this.drawNanoVG() })
    }

    private fun drawNanoVG() {
        val time: String?

        if (mc.isSingleplayer) {
            time = "Singleplayer"
        } else {
            val durationInMillis = System.currentTimeMillis() - startTime
            val second = (durationInMillis / 1000) % 60
            val minute = (durationInMillis / (1000 * 60)) % 60
            val hour = (durationInMillis / (1000 * 60 * 60)) % 24
            time = String.format("%02d:%02d:%02d", hour, minute, second)
        }

        this.drawBackground(140f, 64f)
        this.drawText("Session Info", 5.5f, 6f, 10.5f, getHudFont(1))
        this.drawRect(0f, 17.5f, 140f, 1f)

        this.drawText(LegacyIcon.CLOCK, 5.5f, 22.5f, 10f, Fonts.LEGACYICON)
        this.drawText(time, 18f, 24f, 9f, getHudFont(1))

        this.drawText(LegacyIcon.SERVER, 5.5f, 22.5f + 13, 10f, Fonts.LEGACYICON)
        this.drawText(getServerIP(), 18f, (24 + 12).toFloat(), 9f, getHudFont(1))

        this.drawText(LegacyIcon.USER, 5.5f, 22.5f + 26, 10f, Fonts.LEGACYICON)
        this.drawText(killCount.toString() + " kill", 18f, 24 + 26.5f, 9f, getHudFont(1))

        this.setWidth(140)
        this.setHeight(64)
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer?) {
        startTime = System.currentTimeMillis()
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        if (isHypixel() && event.getPacket() is S02PacketChat) {
            val chatPacket = event.getPacket() as S02PacketChat
            val chatMessage = chatPacket.chatComponent.unformattedText

            val message = StringUtils.stripControlCodes(chatMessage)

            if (!message.contains(":") && Arrays.stream<String?>(killTrigger)
                    .anyMatch { s: String? -> message.replace(mc.thePlayer.name, "*").contains(s!!) }
            ) {
                killCount++
            }
        }
    }

    override fun onEnable() {
        super.onEnable()
        startTime = System.currentTimeMillis()
    }
}



