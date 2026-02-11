package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.Vector3D
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import me.miki.shindo.management.addons.nocheaters.data.WDR
import me.miki.shindo.management.addons.nocheaters.data.WdrData
import me.miki.shindo.management.addons.nocheaters.queue.ReportQueue
import me.miki.shindo.management.sound.Sound
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.world.World
import java.util.*
import java.util.function.Predicate


abstract class Check : ICheck {

    companion object {
        @JvmStatic
        protected val mc: Minecraft
            get() = Minecraft.getMinecraft()

        @JvmStatic
        private val flagMessages: MutableSet<String> = Collections.synchronizedSet(HashSet())

        @JvmStatic
        private var lastSoundTime = 0L

        @JvmStatic
        fun getLastSoundTime(): Long = lastSoundTime

        @JvmStatic
        fun setLastSoundTime(time: Long) {
            lastSoundTime = time
        }

        @JvmStatic
        fun getFlagMessages(): MutableSet<String> = flagMessages
    }

    protected fun isCheckEnabled(): Boolean {

        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled()) return false

        return try {
            when (getCheatName()) {
                "Autoblock" -> addon.enableAutoblockCheck
                "Fastbreak" -> addon.enableFastbreakCheck
                "Ghosthand" -> addon.enableGhosthandCheck
                "KeepSprint" -> addon.enableKeepSprintCheck
                "KillAura" -> addon.enableKillAuraCheck
                "NoSlowdown" -> addon.enableNoSlowdownCheck
                "Scaffold" -> addon.enableScaffoldCheck
                "Reach" -> addon.enableReachCheck
                else -> true
            }
        } catch (e: Exception) {
            false
        }
    }


    protected fun checkViolationLevel(
        player: EntityPlayer,
        failedCheck: Boolean,
        vararg trackers: ViolationLevelTracker
    ) {
        if (!isCheckEnabled()) return

        for (tracker in trackers) {
            if (tracker.isFlagging(failedCheck)) {
                playFlagSound()
                printFlagMessage(player)
                addToReportList(player)
                sendReport(player)
            }
        }
    }

    private fun playFlagSound() {

        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled()) return

        try {
            if (addon.soundWhenFlaggingSetting && System.currentTimeMillis() - getLastSoundTime() > 250) {
                try {
                    try {
                        Sound.play("shindo/sounds/notification.ogg", false)
                    } catch (_: Exception) {
                        mc.thePlayer?.playSound("random.orb", 1.0f, 1.0f)
                    }
                } catch (_: Exception) {
                }
                setLastSoundTime(System.currentTimeMillis())
            }
        } catch (_: Exception) {
        }
    }

    private fun printFlagMessage(player: EntityPlayer) {
        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled()) return

        val cheatType = getCheatName() + if (getFlagType().isEmpty()) "" else " (${getFlagType()})"
        val playername = player.name

        try {
            if (addon.debugLoggingSetting) {
                HackerDetectorAddon.log("$playername flags $cheatType")
            }

            if (!addon.showFlagMessagesSetting) return

            val flagKey = playername + if (addon.showFlagMessageTypeSetting) cheatType else getCheatName()

            val msg = buildString {
                append(EnumChatFormatting.GOLD)
                append("[HackerDetector] ")
                append(EnumChatFormatting.RESET)
                append(playername)
                append(EnumChatFormatting.YELLOW)
                append(" flags ")
                append(EnumChatFormatting.RED)
                append(if (addon.showFlagMessageTypeSetting) cheatType else getCheatName())
            }

            if (addon.oneFlagMessagePerGameSetting) {
                if (getFlagMessages().contains(flagKey)) return
                getFlagMessages().add(flagKey)
            }

            val imsg = ChatComponentText(msg).apply {
                chatStyle = ChatStyle().apply {
                    chatHoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText("${EnumChatFormatting.RED}${getCheatDescription()}")
                    )
                }
            }

            if (addon.showReportButtonOnFlagsSetting) {
                if (addon.addDetectedToReportListSetting) {
                    imsg.appendSibling(
                        ChatComponentText(" ${EnumChatFormatting.GREEN}[WDR]").apply {
                            chatStyle = ChatStyle().apply {
                                chatClickEvent = ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/wdr $playername ${getCheatName().toLowerCase(Locale.ROOT)}"
                                )
                            }
                        }
                    )
                }
            }

            mc.ingameGUI.chatGUI.printChatMessage(imsg)
        } catch (e: Exception) {
            ShindoLogger.error("An Error Occur.", e)
        }
    }

    private fun addToReportList(player: EntityPlayer) {
        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled()) return

        try {
            if (!addon.addDetectedToReportListSetting) return

            val cheat = "${getCheatName().toLowerCase(Locale.ROOT)}[H]"
            val uuid = player.uniqueID
            val wdr = WdrData.getWDR(uuid, player.name)

            if (wdr == null) {
                WdrData.put(uuid, player.name, WDR(listOf(cheat)))
            } else {
                wdr.addCheat(cheat)
            }
        } catch (_: Exception) {

        }
    }

    private fun sendReport(player: EntityPlayer) {
        val addon = HackerDetectorAddon.instance
        if (!addon.isToggled()) return

        try {
            if (canSendReport() && addon.autoReportFlaggedPlayersSetting) {
                ReportQueue.INSTANCE.addReportToQueue(player.name)
            }
        } catch (_: Exception) {

        }
    }

    protected fun fail(player: EntityPlayer) = fail(player, "")

    protected fun fail(player: EntityPlayer, extramsg: String) {
        val addon = HackerDetectorAddon.instance
        if (addon.debugLoggingSetting) {
            val msg = "${player.name}${EnumChatFormatting.GRAY} failed ${EnumChatFormatting.RED}${getCheatName()}" +
                    if (getFlagType().isEmpty()) "" else " (${getFlagType()})" +
                            "${EnumChatFormatting.GRAY} check$extramsg"
            HackerDetectorAddon.log(msg)
        }
    }

    protected fun log(player: EntityPlayer, data: PlayerDataSamples, vl: ViolationLevelTracker, extramsg: String?) {
        val addon = HackerDetectorAddon.instance
        if (addon.debugLoggingSetting) {
            val msg = "${player.name} failed ${getCheatName()}" +
                    if (getFlagType().isEmpty()) "" else " (${getFlagType()})" +
                            " check | vl ${vl.getViolationLevel()}" +
                            (extramsg ?: "")
            HackerDetectorAddon.log(msg)
        }
    }
    protected fun getBaseSprintingSpeed(player: EntityPlayer): Double {
        val speedAmplifier = if (player.isPotionActive(Potion.moveSpeed)) {
            (player.getActivePotionEffect(Potion.moveSpeed)?.amplifier ?: -1) + 1
        } else {
            0
        }
        return getBaseSprintingSpeed(speedAmplifier)
    }

    protected fun getBaseSprintingSpeed(speedAmplifier: Int): Double {
        return 0.2806 * (1.0 + 0.2 * speedAmplifier)
    }
    protected fun getTimeToHarvestBlock(
        state: IBlockState,
        player: EntityPlayer,
        world: World,
        pos: BlockPos
    ): Int {

        val block = state.block
        val hardness = block.getBlockHardness(world, pos)
        if (hardness < 0.0F) return -1

        val heldItem = player.heldItem
        var breakSpeed = if (heldItem != null) {
            heldItem.item.getStrVsBlock(heldItem, state.block)
        } else {
            1.0F
        }

        if (player.isPotionActive(Potion.digSpeed)) {
            val effect = player.getActivePotionEffect(Potion.digSpeed)
            breakSpeed *= 1.0F + ((effect?.amplifier ?: -1) + 1) * 0.2F
        }
        if (player.isPotionActive(Potion.digSlowdown)) {
            val effect = player.getActivePotionEffect(Potion.digSlowdown)
            val multiplier = when (effect?.amplifier ?: -1) {
                0 -> 0.3F
                1 -> 0.09F
                2 -> 0.0027F
                else -> 8.1E-4F
            }
            breakSpeed *= multiplier
        }

        val blockStrength = if (breakSpeed > 0) {
            breakSpeed / hardness / 30F
        } else {
            0F
        }

        return getTimeToHarvestBlock(blockStrength)
    }

    protected fun getTimeToHarvestBlock(blockStrength: Float): Int {
        if (blockStrength <= 0) return -1
        var i = 1
        if (blockStrength < 1.0F) {
            var breakProgression = 0F
            while (breakProgression < 1.0f) {
                i++
                breakProgression += blockStrength
            }
        }
        return if (i == 1) i else i + 5
    }

    protected fun isPlayerLookingAtBlock(player: EntityPlayer, data: PlayerDataSamples, pos: BlockPos): Boolean {
        val eyesToBlockCenter = if (player == mc.thePlayer) {
            Vector3D(
                pos.x + 0.5 - player.posX,
                pos.y + 0.5 - (player.posY + player.eyeHeight),
                pos.z + 0.5 - player.posZ
            )
        } else {
            getEyesToBlockVect(player, data, pos)
        }

        val distSq = eyesToBlockCenter.normSquared()
        if (distSq > 28.79422863) return false
        if (distSq < 0.25) return true

        val lookVect = if (player == mc.thePlayer) {
            Vector3D.getPlayersLookVec(player)
        } else {
            Vector3D.getVectorFromRotation(data.serverPitchList.get(0), data.serverYawHeadList.get(0))
        }

        val angleWithVector = lookVect.getAngleWithVector(eyesToBlockCenter)
        val maxAngle = Math.toDegrees(Math.atan(0.5 * Math.sqrt(3.0 / distSq))) * 1.33
        return angleWithVector < maxAngle
    }

    protected fun getEyesToBlockVect(player: EntityPlayer, data: PlayerDataSamples, pos: BlockPos): Vector3D {
        return Vector3D(
            pos.x + 0.5 - data.serverPosXList.get(0),
            pos.y + 0.5 - (data.serverPosYList.get(0) + player.eyeHeight),
            pos.z + 0.5 - data.serverPosZList.get(0)
        )
    }

    protected fun getPlayersInAABBexcluding(
        entity: Entity,
        aabb: AxisAlignedBB,
        predicate: Predicate<EntityPlayer>
    ): List<EntityPlayer> {
        val list = mutableListOf<EntityPlayer>()
        for (player in mc.theWorld.playerEntities) {
            if (player != entity && player.entityBoundingBox.intersectsWith(aabb) && predicate.test(player)) {
                list.add(player)
            }
        }
        return list
    }

    protected fun getHitVectOnBlock(pos: BlockPos, vecA: Vec3, vecB: Vec3): Vec3? {
        val boxMinX = pos.x.toDouble()
        val boxMaxX = pos.x + 1.0
        val boxMinY = pos.y.toDouble()
        val boxMaxY = pos.y + 1.0
        val boxMinZ = pos.z.toDouble()
        val boxMaxZ = pos.z + 1.0

        var interMinX = vecA.getIntermediateWithXValue(vecB, boxMinX)
        var interMaxX = vecA.getIntermediateWithXValue(vecB, boxMaxX)
        var interMinY = vecA.getIntermediateWithYValue(vecB, boxMinY)
        var interMaxY = vecA.getIntermediateWithYValue(vecB, boxMaxY)
        var interMinZ = vecA.getIntermediateWithZValue(vecB, boxMinZ)
        var interMaxZ = vecA.getIntermediateWithZValue(vecB, boxMaxZ)

        if (interMinX == null || interMinX.yCoord < boxMinY || interMinX.yCoord > boxMaxY || interMinX.zCoord < boxMinZ || interMinX.zCoord > boxMaxZ) {
            interMinX = null
        }
        if (interMaxX == null || interMaxX.yCoord < boxMinY || interMaxX.yCoord > boxMaxY || interMaxX.zCoord < boxMinZ || interMaxX.zCoord > boxMaxZ) {
            interMaxX = null
        }
        if (interMinY == null || interMinY.xCoord < boxMinX || interMinY.xCoord > boxMaxX || interMinY.zCoord < boxMinZ || interMinY.zCoord > boxMaxZ) {
            interMinY = null
        }
        if (interMaxY == null || interMaxY.xCoord < boxMinX || interMaxY.xCoord > boxMaxX || interMaxY.zCoord < boxMinZ || interMaxY.zCoord > boxMaxZ) {
            interMaxY = null
        }
        if (interMinZ == null || interMinZ.xCoord < boxMinX || interMinZ.xCoord > boxMaxX || interMinZ.yCoord < boxMinY || interMinZ.yCoord > boxMaxY) {
            interMinZ = null
        }
        if (interMaxZ == null || interMaxZ.xCoord < boxMinX || interMaxZ.xCoord > boxMaxX || interMaxZ.yCoord < boxMinY || interMaxZ.yCoord > boxMaxY) {
            interMaxZ = null
        }

        var closestHitVect: Vec3? = null
        if (interMinX != null) closestHitVect = interMinX
        if (interMaxX != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxX) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMaxX
        }
        if (interMinY != null && (closestHitVect == null || vecA.squareDistanceTo(interMinY) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMinY
        }
        if (interMaxY != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxY) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMaxY
        }
        if (interMinZ != null && (closestHitVect == null || vecA.squareDistanceTo(interMinZ) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMinZ
        }
        if (interMaxZ != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxZ) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMaxZ
        }

        return closestHitVect
    }

    protected fun getHitVectOnPlayer(player: EntityPlayer, vecA: Vec3, vecB: Vec3): Vec3? {
        return getHitVectOnPlayer(player.posX, player.posY, player.posZ, vecA, vecB)
    }

    protected fun getHitVectOnPlayer(playerX: Double, playerY: Double, playerZ: Double, vecA: Vec3, vecB: Vec3): Vec3? {
        val boxMinX = playerX - 0.3
        val boxMaxX = playerX + 0.3
        val boxMinY = playerY - 0.1
        val boxMaxY = playerY + 1.9
        val boxMinZ = playerZ - 0.3
        val boxMaxZ = playerZ + 0.3

        var interMinX = vecA.getIntermediateWithXValue(vecB, boxMinX)
        var interMaxX = vecA.getIntermediateWithXValue(vecB, boxMaxX)
        var interMinY = vecA.getIntermediateWithYValue(vecB, boxMinY)
        var interMaxY = vecA.getIntermediateWithYValue(vecB, boxMaxY)
        var interMinZ = vecA.getIntermediateWithZValue(vecB, boxMinZ)
        var interMaxZ = vecA.getIntermediateWithZValue(vecB, boxMaxZ)

        if (interMinX == null || interMinX.yCoord < boxMinY || interMinX.yCoord > boxMaxY || interMinX.zCoord < boxMinZ || interMinX.zCoord > boxMaxZ) {
            interMinX = null
        }
        if (interMaxX == null || interMaxX.yCoord < boxMinY || interMaxX.yCoord > boxMaxY || interMaxX.zCoord < boxMinZ || interMaxX.zCoord > boxMaxZ) {
            interMaxX = null
        }
        if (interMinY == null || interMinY.xCoord < boxMinX || interMinY.xCoord > boxMaxX || interMinY.zCoord < boxMinZ || interMinY.zCoord > boxMaxZ) {
            interMinY = null
        }
        if (interMaxY == null || interMaxY.xCoord < boxMinX || interMaxY.xCoord > boxMaxX || interMaxY.zCoord < boxMinZ || interMaxY.zCoord > boxMaxZ) {
            interMaxY = null
        }
        if (interMinZ == null || interMinZ.xCoord < boxMinX || interMinZ.xCoord > boxMaxX || interMinZ.yCoord < boxMinY || interMinZ.yCoord > boxMaxY) {
            interMinZ = null
        }
        if (interMaxZ == null || interMaxZ.xCoord < boxMinX || interMaxZ.xCoord > boxMaxX || interMaxZ.yCoord < boxMinY || interMaxZ.yCoord > boxMaxY) {
            interMaxZ = null
        }

        var closestHitVect: Vec3? = null
        if (interMinX != null) closestHitVect = interMinX
        if (interMaxX != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxX) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMaxX
        }
        if (interMinY != null && (closestHitVect == null || vecA.squareDistanceTo(interMinY) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMinY
        }
        if (interMaxY != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxY) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMaxY
        }
        if (interMinZ != null && (closestHitVect == null || vecA.squareDistanceTo(interMinZ) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMinZ
        }
        if (interMaxZ != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxZ) < vecA.squareDistanceTo(
                closestHitVect
            ))
        ) {
            closestHitVect = interMaxZ
        }

        return closestHitVect
    }

    protected fun isInsideHitbox(
        playerX: Double,
        playerY: Double,
        playerZ: Double,
        x: Double,
        y: Double,
        z: Double
    ): Boolean {
        return x > playerX - 0.3 && x < playerX + 0.3 &&
                y > playerY - 0.1 && y < playerY + 1.9 &&
                z > playerZ - 0.3 && z < playerZ + 0.3
    }

    protected fun isInsideHitbox(playerX: Double, playerY: Double, playerZ: Double, vec: Vec3): Boolean {
        return vec.xCoord > playerX - 0.3 && vec.xCoord < playerX + 0.3 &&
                vec.yCoord > playerY - 0.1 && vec.yCoord < playerY + 1.9 &&
                vec.zCoord > playerZ - 0.3 && vec.zCoord < playerZ + 0.3
    }

    protected fun isInsideHitbox(player: EntityPlayer, vec: Vec3): Boolean {
        return isInsideHitbox(player.posX, player.posY, player.posZ, vec)
    }

    protected fun isInsideHitbox(player: EntityPlayer, x: Double, y: Double, z: Double): Boolean {
        return isInsideHitbox(player.posX, player.posY, player.posZ, x, y, z)
    }

}



