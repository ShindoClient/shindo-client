package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import kotlin.math.abs

open class KeepSprintACheck : Check() {

    override fun getCheatName(): String = "KeepSprint"

    override fun getCheatDescription(): String = "O sprint do jogador não desliga ao usar itens (bloquear espada, comer, beber, usar arco...)"

    override fun getFlagType(): String = "A"

    override fun canSendReport(): Boolean = false

    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.keepsprintAVL)
    }

    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (data.isNotMovingXZ() || player.isRiding) return false

        if (data.useItemTime > 5 && data.sprintTime > 0) {
            if (abs(data.getMoveLookAngleDiff()) > 135.0) {
                data.keepsprintAVL.subtract(3)
                return false
            }

            val invalidSprint = if (data.usedItemIsConsumable) {
                if (data.useItemTime > 32) return false
                if (data.sprintTime > 32) {
                    true
                } else {
                    if (data.sprintTime == data.useItemTime && data.useItemTime < 12) return false
                    data.sprintTime > data.useItemTime + 3 || (data.lastEatTime > 32 && data.sprintTime > 5)
                }
            } else {
                val itemStack: ItemStack? = player.heldItem
                if (itemStack != null && itemStack.item is ItemSword) return false
                data.sprintTime > 5
            }

            if (invalidSprint && data.getSpeedXZSq() < 6.25) {
                data.keepsprintAVL.add(2)
                if (HackerDetectorAddon.instance.debugLoggingSetting) {
                    logKeepSprint(player, data, data.keepsprintAVL, null)
                }
                return true
            }
        } else if (data.useItemTime > 5 && data.sprintTime == 0) {
            data.keepsprintAVL.subtract(3)
        }

        return false
    }

    private fun logKeepSprint(
        player: EntityPlayer,
        data: PlayerDataSamples,
        vl: ViolationLevelTracker,
        extramsg: String?
    ) {
        val itemStack: ItemStack? = player.heldItem
        val item: Item? = itemStack?.item
        log(
            player, data, vl,
            " | sprintTime ${data.sprintTime}" +
                    " | useItemTime ${data.useItemTime}" +
                    " | lastEatTime ${data.lastEatTime}" +
                    " | speedXZ ${String.format("%.2f", data.getSpeedXZ())}" +
                    (item?.let { " | item held ${it.unlocalizedName}" } ?: "") +
                    " | moveDiff ${String.format("%.2f", abs(data.getMoveLookAngleDiff()))}" +
                    (extramsg ?: "")
        )
    }

    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(48)
    }
}
