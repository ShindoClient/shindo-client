package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword

/**
 * Detecta se o jogador está correndo enquanto usa itens (bloquear espada, comer, beber, usar arco...)
 */
class NoSlowdownCheck : Check() {
    
    override fun getCheatName(): String = "NoSlowdown"
    
    override fun getCheatDescription(): String = "O jogador está correndo enquanto usa itens (bloquear espada, comer, beber, usar arco...)"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.noSlowdownVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        // Se o jogador está se movendo mais devagar que a velocidade base de corrida, consideramos que é keepsprint
        if (data.isNotMovingXZ() || player.isRiding) return false
        
        if (data.useItemTime > 5 && data.sprintTime > 0) {
            if (Math.abs(data.getMoveLookAngleDiff()) > 135.0) {
                data.noSlowdownVL.subtract(3)
                return false // rubber band
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
                data.sprintTime > 5
            }
            
            val itemStack: ItemStack? = player.heldItem
            if (invalidSprint && (data.getSpeedXZSq() >= 6.25 || (itemStack != null && itemStack.item is ItemSword))) {
                data.noSlowdownVL.add(2)
                if (HackerDetectorAddon.instance.debugLoggingSetting) {
                    logNoSlowdown(player, data, data.noSlowdownVL, null)
                }
                return true
            }
        } else if (data.useItemTime > 5 && data.sprintTime == 0) {
            data.noSlowdownVL.subtract(3)
        }
        
        return false
    }
    
    protected fun logNoSlowdown(player: EntityPlayer, data: PlayerDataSamples, vl: ViolationLevelTracker, extramsg: String?) {
        val itemStack: ItemStack? = player.heldItem
        val item: Item? = itemStack?.item
        log(player, data, vl,
            " | sprintTime ${data.sprintTime}" +
            " | useItemTime ${data.useItemTime}" +
            " | lastEatTime ${data.lastEatTime}" +
            " | speedXZ ${String.format("%.2f", data.getSpeedXZ())}" +
            (item?.let { " | item held ${it.unlocalizedName}" } ?: "") +
            " | moveDiff ${String.format("%.2f", Math.abs(data.getMoveLookAngleDiff()))}" +
            (extramsg ?: "")
        )
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(48)
    }
}
