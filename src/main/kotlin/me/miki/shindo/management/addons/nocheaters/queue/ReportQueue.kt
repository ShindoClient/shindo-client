package me.miki.shindo.management.addons.nocheaters.queue

import me.miki.shindo.management.addons.nocheaters.NoCheatersAddon
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventTick
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
class ReportQueue {

    companion object {
        private const val TIME_ABORT_REPORT = 30_000L

        @JvmStatic
        lateinit var INSTANCE: ReportQueue
    }

    private var standStillCounter = 0
    private var standStillLimit = 22
    private var movingCounter = 0
    private var betweenReportCounter = 0
    private var prevItemHeld = 0

    val queueList: MutableList<ReportInQueue> = CopyOnWriteArrayList()
    private val playersReportedThisGame = mutableSetOf<String>()
    private val random = Random()

    init {
        INSTANCE = this
    }

    @EventTarget
    fun onTick(event: EventTick) {
        if (!NoCheatersAddon.instance.isToggled()) return
        if (!NoCheatersAddon.instance.autoReportQueueSetting) return

        val mc = Minecraft.getMinecraft()
        if (queueList.isEmpty() || mc.thePlayer == null) return

        val now = System.currentTimeMillis()
        queueList.removeIf { it.time + TIME_ABORT_REPORT < now }
        if (queueList.isEmpty()) return

        betweenReportCounter--

        if (isPlayerStandingStill(mc)) {
            standStillCounter++
            if (standStillCounter >= getStandStillLimit()) {
                movingCounter = 0
                val playername = queueList.removeAt(0).name
                val msg = "/wdr $playername"

                mc.thePlayer.sendChatMessage(msg)

                standStillLimit = 20 + random.nextInt(11)
                standStillCounter = 0
                betweenReportCounter = 50

                me.miki.shindo.logger.ShindoLogger.info("[NoCheaters] Auto-reported $playername")
            }
        } else {
            standStillCounter = 0
            movingCounter++
        }
    }

    fun getStandStillCounter(): Int = standStillCounter

    fun getStandStillLimit(): Int = standStillLimit + maxOf(0, betweenReportCounter)

    fun addReportToQueue(playername: String) {
        if (playersReportedThisGame.contains(playername)) return

        for (i in queueList.indices) {
            val report = queueList[i]
            if (report.name.equals(playername, ignoreCase = true)) {
                report.time = System.currentTimeMillis()
                queueList.sortBy { it.time }
                return
            }
        }

        queueList.add(ReportInQueue(playername))
        me.miki.shindo.logger.ShindoLogger.info("[NoCheaters] Added $playername to report queue")
    }

    fun addPlayerReportedThisGame(playername: String) {
        playersReportedThisGame.add(playername)
        removePlayerFromReportQueue(playername)
    }

    fun removePlayerFromReportQueue(playername: String) {
        queueList.removeIf { it.name.equals(playername, ignoreCase = true) }
    }

    fun clearReportedThisGame() {
        playersReportedThisGame.clear()
    }

    private fun isPlayerStandingStill(mc: Minecraft): Boolean {
        val player = mc.thePlayer ?: return false
        val sameItem = player.inventory.currentItem == prevItemHeld
        prevItemHeld = player.inventory.currentItem

        val isChatOpen = mc.currentScreen is GuiChat


        val chatEmpty = true

        return (mc.inGameHasFocus || (isChatOpen && chatEmpty))
                && player.movementInput.moveForward == 0.0F
                && player.movementInput.moveStrafe == 0.0F
                && !player.movementInput.jump
                && !player.movementInput.sneak
                && !mc.gameSettings.keyBindAttack.isKeyDown
                && !mc.gameSettings.keyBindUseItem.isKeyDown
                && player.prevRotationYawHead == player.rotationYawHead
                && sameItem
    }

    class ReportInQueue(val name: String) {
        var time: Long = System.currentTimeMillis()
    }
}
