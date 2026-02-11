package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword

class AutoblockCheck : Check() {

    override fun getCheatName(): String = "Autoblock"

    override fun getCheatDescription(): String = "O jogador pode atacar enquanto bloqueia com espada"

    override fun canSendReport(): Boolean = true

    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.autoblockAVL)
    }

    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (!data.hasSwung) return false

        val itemStack: ItemStack? = player.heldItem
        if (itemStack == null || itemStack.item !is ItemSword) return false

        if (data.useItemTime > 5) {
            data.autoblockAVL.add(5)
            if (HackerDetectorAddon.instance.debugLoggingSetting) {
                log(player, data, data.autoblockAVL, " | useItemTime ${data.useItemTime}")
            }
            return true
        } else if (data.useItemTime == 0) {
            data.autoblockAVL.subtract(2)
        }

        return false
    }

    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(2, 1, 10)
    }
}
