package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion

/**
 * Detecta se o jogador está voando ou fazendo airwalk
 */
class FlyCheck : Check() {
    
    override fun getCheatName(): String = "Fly"
    
    override fun getCheatDescription(): String = "O jogador pode voar ou caminhar no ar"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.flyVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (player.isRiding) return false
        if (!data.serverPosYList.hasCollected()) return false
        
        // Verifica se o jogador está no chão
        if (player.onGround) {
            data.flyVL.subtract(2)
            return false
        }
        
        // Verifica se tem efeito de levitação (não existe em 1.8.9, mas verifica outros efeitos)
        // Levitation não existe em 1.8.9
        
        // Calcula a velocidade vertical
        val speedY = data.speedYList.get(0)
        val prevSpeedY = if (data.speedYList.size() > 1) data.speedYList.get(1) else speedY
        
        // Verifica se está caindo normalmente (gravidade)
        if (speedY < -0.1 && speedY < prevSpeedY) {
            data.flyVL.subtract(1)
            return false
        }
        
        // Verifica se está subindo sem motivo (sem estar em água, sem elytra, etc.)
        // Elytra não existe em 1.8.9
        if (speedY > 0.1 && !player.isInWater && !player.isOnLadder) {
            // Verifica se não está em um bloco que empurra para cima
            val blockBelow = mc.theWorld?.getBlockState(player.position.down())?.block
            if (blockBelow?.material?.isSolid == false) {
                data.flyVL.add(3)
                if (HackerDetectorAddon.instance.debugLoggingSetting) {
                    log(player, data, data.flyVL,
                        " | speedY ${String.format("%.2f", speedY)}" +
                        " | onGround ${player.onGround}" +
                        " | airTime ${data.airTime}"
                    )
                }
                return true
            }
        }
        
        // Verifica airwalk (flutuação no ar)
        if (data.airTime > 20 && Math.abs(speedY) < 0.05 && !player.isInWater && !player.isOnLadder) {
            data.flyVL.add(2)
            if (HackerDetectorAddon.instance.debugLoggingSetting) {
                log(player, data, data.flyVL,
                    " | airTime ${data.airTime}" +
                    " | speedY ${String.format("%.2f", speedY)}" +
                    " | possible airwalk"
                )
            }
            return true
        }
        
        return false
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(3, 1, 20)
    }
}
