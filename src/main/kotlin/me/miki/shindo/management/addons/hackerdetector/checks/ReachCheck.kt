package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

/**
 * Detecta se o jogador ataca com alcance maior que o normal (Reach hack)
 */
class ReachCheck : Check() {
    
    override fun getCheatName(): String = "Reach"
    
    override fun getCheatDescription(): String = "O jogador pode atacar com alcance maior que o normal"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.reachVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (!data.hasAttackedTarget()) return false
        if (player.isRiding) return false
        
        val target = data.attackInfo?.target ?: return false
        if (target !is IMixinEntityPlayer) return false
        
        val targetData = target.getPlayerDataSamples()
        if (targetData.posXList.size() < 5) return false
        
        // Calcula a distância entre o atacante e o alvo
        val attackerEyePos = data.getPositionEyesServer(player)
        val targetPos = Vec3(
            targetData.serverPosXList.get(0),
            targetData.serverPosYList.get(0),
            targetData.serverPosZList.get(0)
        )
        
        val distance = attackerEyePos.distanceTo(targetPos)
        
        // Alcance máximo normal é ~3.0 blocos (com hitbox)
        val maxReach = 3.15
        
        if (distance > maxReach) {
            val excess = distance - maxReach
            // Adiciona violação baseada no excesso de alcance
            val violation = Math.min(10, (excess * 20).toInt())
            data.reachVL.add(violation)
            
            if (HackerDetectorAddon.instance.debugLoggingSetting) {
                log(player, data, data.reachVL,
                    " | distance ${String.format("%.2f", distance)}" +
                    " | maxReach $maxReach" +
                    " | excess ${String.format("%.2f", excess)}"
                )
            }
            return true
        } else {
            data.reachVL.subtract(1)
        }
        
        return false
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(2, 1, 15)
    }
}
