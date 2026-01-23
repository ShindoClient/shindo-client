package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

/**
 * Detecta se o jogador pode atacar enquanto come e bebe poções
 */
class KillAuraBCheck : Check() {
    
    override fun getCheatName(): String = "KillAura"
    
    override fun getCheatDescription(): String = "O jogador pode atacar enquanto come e bebe poções"
    
    override fun getFlagType(): String = "B"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.killAuraBVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (data.hasAttacked()) {
            if (data.useItemTime > 6 && data.timeEating < 33 && data.usedItemIsConsumable && data.lastEatTime > 32) {
                data.killAuraBVL.add(1)
                if (HackerDetectorAddon.instance.debugLoggingSetting) {
                    val itemStack: ItemStack? = player.heldItem
                    val item: Item? = itemStack?.item
                    log(player, data, data.killAuraBVL,
                        " | ${data.attackInfo?.attackType?.name}" +
                        " | useItemTime ${data.useItemTime}" +
                        " | lastEatTime ${data.lastEatTime}" +
                        (item?.let { " | item held ${it.unlocalizedName}" } ?: "")
                    )
                }
                return true
            }
        }
        return false
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(100, 1, 110)
    }
}
