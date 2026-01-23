package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion

/**
 * Detecta se o jogador não recebe dano de queda (NoFall hack)
 */
class NoFallCheck : Check() {
    
    override fun getCheatName(): String = "NoFall"
    
    override fun getCheatDescription(): String = "O jogador não recebe dano de queda"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.noFallVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (player.isRiding) return false
        if (!data.serverPosYList.hasCollected()) return false
        
        // Verifica se tem efeito de resistência a queda
        if (player.isPotionActive(Potion.resistance)) {
            return false
        }
        
        // Verifica se está em água ou escada (não recebe dano de queda)
        if (player.isInWater || player.isOnLadder) {
            data.noFallVL.subtract(1)
            return false
        }
        
        // Calcula a velocidade de queda
        val speedY = data.speedYList.get(0)
        
        // Verifica se estava caindo muito rápido e parou no chão sem dano
        if (player.onGround && data.airTime > 10) {
            // Verifica a velocidade de queda anterior
            var maxFallSpeed = 0.0
            for (i in 0 until Math.min(20, data.speedYList.size())) {
                val fallSpeed = data.speedYList.get(i)
                if (fallSpeed < maxFallSpeed) {
                    maxFallSpeed = fallSpeed
                }
            }
            
            // Se caiu de mais de 3 blocos (velocidade ~-7.7) e não recebeu dano
            if (maxFallSpeed < -7.0 && player.hurtTime == 0 && player.fallDistance < 3.0) {
                data.noFallVL.add(5)
                if (HackerDetectorAddon.instance.debugLoggingSetting) {
                    log(player, data, data.noFallVL,
                        " | maxFallSpeed ${String.format("%.2f", maxFallSpeed)}" +
                        " | fallDistance ${String.format("%.2f", player.fallDistance)}" +
                        " | airTime ${data.airTime}" +
                        " | hurtTime ${player.hurtTime}"
                    )
                }
                return true
            }
        }
        
        // Verifica se está caindo mas não está acelerando (possível nofall ativo)
        if (!player.onGround && speedY > -0.1 && data.airTime > 5) {
            data.noFallVL.add(1)
            if (HackerDetectorAddon.instance.debugLoggingSetting && data.noFallVL.getViolationLevel() > 3) {
                log(player, data, data.noFallVL,
                    " | speedY ${String.format("%.2f", speedY)}" +
                    " | airTime ${data.airTime}" +
                    " | possible nofall active"
                )
            }
            return true
        }
        
        if (player.onGround) {
            data.noFallVL.subtract(1)
        }
        
        return false
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(2, 1, 15)
    }
}
