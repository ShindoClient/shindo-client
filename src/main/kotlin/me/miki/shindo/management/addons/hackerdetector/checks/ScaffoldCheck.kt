package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.data.SampleListD
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import kotlin.math.abs

class ScaffoldCheck : Check() {

    override fun getCheatName(): String = "Scaffold"

    override fun getCheatDescription(): String =
        "O jogador coloca blocos automaticamente sob os pés enquanto ganha altura rapidamente"

    override fun canSendReport(): Boolean = true

    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.scaffoldVL)
    }

    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (player.isRiding || data.serverPosYList.size() < 4) return false

        if (player.isSwingInProgress && player.hurtTime == 0 && data.serverPitchList.get(0) > 50f && data.getSpeedXZSq() > 9.0) {
            val itemStack: ItemStack? = player.heldItem
            if (itemStack != null && itemStack.item is ItemBlock) {
                val angleDiff = abs(data.getMoveLookAngleDiff())
                val speedXZSq = data.getSpeedXZSq()
                if (angleDiff > 165.0 && speedXZSq < 100.0) {
                    val speedY = data.speedYList.get(0)
                    val avgAccelY = avgAccel(data.serverPosYList)
                    if (isAlmostZero(avgAccelY)) return false

                    if (speedY < 15.0 && speedY > 4.0 && avgAccelY > -25.0) {
                        if (HackerDetectorAddon.instance.debugLoggingSetting) {
                            val msg = " | pitch ${String.format("%.2f", data.serverPitchList.get(0))}" +
                                    " | speedXZ ${String.format("%.2f", data.getSpeedXZ())}" +
                                    " | angleDiff ${String.format("%.2f", angleDiff)}" +
                                    " | speedY ${String.format("%.2f", speedY)}" +
                                    " | avgAccelY ${String.format("%.2f", avgAccelY)}"
                            log(player, data, data.scaffoldVL, msg)
                        }
                        return true
                    } else if (speedY < 4.0 && speedY > -1.0 && abs(speedY) > 0.005 && speedXZSq > 25.0) {
                        if (HackerDetectorAddon.instance.debugLoggingSetting) {
                            val msg = " | pitch ${String.format("%.2f", data.serverPitchList.get(0))}" +
                                    " | speedXZ ${String.format("%.2f", data.getSpeedXZ())}" +
                                    " | angleDiff ${String.format("%.2f", angleDiff)}" +
                                    " | speedY ${String.format("%.2f", speedY)}" +
                                    " | avgAccelY ${String.format("%.2f", avgAccelY)}"
                            log(player, data, data.scaffoldVL, msg)
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun avgAccel(list: SampleListD): Double {
        return 50.0 * (list.get(3) - list.get(2) - list.get(1) + list.get(0))
    }

    private fun isAlmostZero(d: Double): Boolean = abs(d) < 0.001

    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(2, 1, 24)
    }
}
