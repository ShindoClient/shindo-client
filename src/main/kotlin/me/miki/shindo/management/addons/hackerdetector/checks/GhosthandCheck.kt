package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3

/**
 * Detecta se o jogador pode minerar blocos através de outros jogadores
 */
class GhosthandCheck : Check() {
    
    override fun getCheatName(): String = "Ghosthand"
    
    override fun getCheatDescription(): String = "O jogador pode minerar blocos através de outros jogadores"
    
    override fun canSendReport(): Boolean = false
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.ghosthandVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        val blockTouched = data.blockTouched ?: return false
        if (player.isRiding) return false
        if (getEyesToBlockVect(player, data, blockTouched).normSquared() > 28.79422863) return false
        
        val maxReach = 5.0
        val eyePos = data.getPositionEyesServer(player)
        val lookVect = data.getLookServer()
        val lookEndPos = eyePos.addVector(lookVect.xCoord * maxReach, lookVect.yCoord * maxReach, lookVect.zCoord * maxReach)
        val hitVect = getHitVectOnBlock(blockTouched, eyePos, lookEndPos) ?: return false
        
        val distance = eyePos.distanceTo(hitVect)
        val f = 1.0F
        val MAX_TICK_DELAY = 10
        
        val nearbyPlayers = getPlayersDataInAABBexcluding(
            player,
            player.entityBoundingBox.addCoord(lookVect.xCoord * distance, lookVect.yCoord * distance, lookVect.zCoord * distance).expand(f.toDouble(), f.toDouble(), f.toDouble())
        ) { p ->
            p != mc.thePlayer && p.canBeCollidedWith() && 
            HackerDetectorAddon.isValidPlayer(p.uniqueID) && 
            !p.isInvisible() && 
            p is IMixinEntityPlayer && 
            p.getPlayerDataSamples().posXList.size() >= MAX_TICK_DELAY
        }
        
        if (nearbyPlayers.isEmpty()) {
            data.ghosthandVL.subtract(1)
            return false
        }
        
        val STEP_SIZE = 0.2
        val maxSteps = (distance / STEP_SIZE).toInt()
        for (i in 1 until MAX_TICK_DELAY) {
            var isInsidePlayer = false
            stepLoop@ for (j in 0..maxSteps) {
                val dx = eyePos.xCoord + j * STEP_SIZE * lookVect.xCoord
                val dy = eyePos.yCoord + j * STEP_SIZE * lookVect.yCoord
                val dz = eyePos.zCoord + j * STEP_SIZE * lookVect.zCoord
                for (eData in nearbyPlayers) {
                    if (isInsideHitbox(eData.posXList.get(i), eData.posYList.get(i), eData.posZList.get(i), dx, dy, dz)) {
                        isInsidePlayer = true
                        break@stepLoop
                    }
                }
            }
            if (!isInsidePlayer) {
                data.ghosthandVL.subtract(1)
                return false
            }
        }
        
        data.ghosthandVL.add(1)
        if (HackerDetectorAddon.instance.debugLoggingSetting && data.ghosthandVL.getViolationLevel() > 2) {
            log(player, data, data.ghosthandVL, null)
        }
        return true
    }
    
    private fun getPlayersDataInAABBexcluding(
        entity: EntityPlayer,
        aabb: net.minecraft.util.AxisAlignedBB,
        predicate: (EntityPlayer) -> Boolean
    ): List<PlayerDataSamples> {
        val list = mutableListOf<PlayerDataSamples>()
        for (player in mc.theWorld.playerEntities) {
            if (player != entity && player.entityBoundingBox.intersectsWith(aabb) && predicate(player)) {
                if (player is IMixinEntityPlayer) {
                    list.add(player.getPlayerDataSamples())
                }
            }
        }
        return list
    }
    
    companion object {
        fun newVl(): ViolationLevelTracker = ViolationLevelTracker(8)
    }
}
