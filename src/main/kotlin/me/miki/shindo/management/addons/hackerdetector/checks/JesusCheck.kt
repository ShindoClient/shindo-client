package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos

/**
 * Detecta se o jogador está caminhando sobre água/lava sem afundar
 */
class JesusCheck : Check() {
    
    override fun getCheatName(): String = "Jesus"
    
    override fun getCheatDescription(): String = "O jogador pode caminhar sobre água/lava sem afundar"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.jesusVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (player.isRiding) return false
        if (!data.serverPosYList.hasCollected()) return false
        
        val pos = BlockPos(player.posX, player.posY, player.posZ)
        val blockBelow = mc.theWorld?.getBlockState(pos.down())?.block ?: return false
        
        // Verifica se o bloco abaixo é água ou lava
        val isLiquid = blockBelow.material == Material.water || blockBelow.material == Material.lava
        
        if (!isLiquid) {
            data.jesusVL.subtract(1)
            return false
        }
        
        // Verifica se o jogador está realmente sobre o líquido (não dentro)
        val playerY = player.posY
        val blockY = pos.y.toDouble()
        
        // Se o jogador está muito abaixo do bloco, está dentro do líquido (normal)
        if (playerY < blockY + 0.1) {
            data.jesusVL.subtract(1)
            return false
        }
        
        // Verifica se está se movendo horizontalmente sobre o líquido
        if (data.isNotMovingXZ()) {
            data.jesusVL.subtract(1)
            return false
        }
        
        val speedXZ = data.getSpeedXZ()
        
        // Se está se movendo muito rápido sobre líquido, pode ser jesus hack
        if (speedXZ > 0.2 && playerY > blockY + 0.5) {
            data.jesusVL.add(3)
            if (HackerDetectorAddon.instance.debugLoggingSetting) {
                log(player, data, data.jesusVL,
                    " | speedXZ ${String.format("%.2f", speedXZ)}" +
                    " | playerY ${String.format("%.2f", playerY)}" +
                    " | blockY ${String.format("%.2f", blockY)}" +
                    " | material ${blockBelow.material}"
                )
            }
            return true
        }
        
        return false
    }
    
    companion object {
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(2, 1, 10)
    }
}
