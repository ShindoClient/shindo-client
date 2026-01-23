package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion

/**
 * Detecta se o jogador está se movendo mais rápido que o normal
 */
class SpeedCheck : Check() {
    
    override fun getCheatName(): String = "Speed"
    
    override fun getCheatDescription(): String = "O jogador está se movendo mais rápido que o normal"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.speedVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (player.isRiding) return false
        if (data.isNotMovingXZ()) return false
        if (!data.serverPosXList.hasCollected()) return false
        
        val speedXZ = data.getSpeedXZ()
        val baseSpeed = getBaseSprintingSpeed(player)
        
        // Calcula o multiplicador de velocidade
        val speedMultiplier = speedXZ / baseSpeed
        
        // Verifica se está em água ou escada (pode ser mais rápido)
        if (player.isInWater || player.isOnLadder) {
            data.speedVL.subtract(1)
            return false
        }
        
        // Verifica se está no chão
        if (!player.onGround) {
            // No ar, a velocidade deve ser menor
            if (speedXZ > baseSpeed * 0.8) {
                data.speedVL.subtract(1)
            }
            return false
        }
        
        // Verifica velocidade anormal no chão
        // Multiplicador máximo normal é ~1.5 (com speed potion level 2)
        val maxNormalMultiplier = 1.5
        
        if (speedMultiplier > maxNormalMultiplier) {
            val excess = speedMultiplier - maxNormalMultiplier
            val violation = Math.min(8, (excess * 20).toInt())
            data.speedVL.add(violation)
            
            if (HackerDetectorAddon.instance.debugLoggingSetting) {
                log(player, data, data.speedVL,
                    " | speedXZ ${String.format("%.2f", speedXZ)}" +
                    " | baseSpeed ${String.format("%.2f", baseSpeed)}" +
                    " | multiplier ${String.format("%.2f", speedMultiplier)}" +
                    " | onGround ${player.onGround}"
                )
            }
            return true
        } else {
            data.speedVL.subtract(1)
        }
        
        return false
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(2, 1, 12)
    }
}
