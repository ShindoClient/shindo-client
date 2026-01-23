package me.miki.shindo.management.addons.hackerdetector.checks

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.HackerDetectorAddon
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import me.miki.shindo.management.addons.hackerdetector.data.TickingBlockMap
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

/**
 * Detecta se o jogador ataca através de blocos/entidades (versão A)
 */
class KillAuraACheck(private val recentPlacedBlocks: TickingBlockMap) : Check() {
    
    override fun getCheatName(): String = "KillAura"
    
    override fun getCheatDescription(): String = "O jogador pode atacar através de blocos/entidades"
    
    override fun getFlagType(): String = "A"
    
    override fun canSendReport(): Boolean = true
    
    override fun performCheck(player: EntityPlayer, data: PlayerDataSamples) {
        checkViolationLevel(player, check(player, data), data.killAuraAVL)
    }
    
    override fun check(player: EntityPlayer, data: PlayerDataSamples): Boolean {
        if (!isCheckEnabled()) return false
        if (!data.hasAttackedTarget()) return false
        if (player.isRiding) return false
        if (data.attackInfo?.target == mc.thePlayer) return false
        
        val maxReach = 3.15
        val attackerEyePos = data.getPositionEyesServer(player)
        val lookVect = data.getLookServer()
        val lookEndPos = attackerEyePos.addVector(lookVect.xCoord * maxReach, lookVect.yCoord * maxReach, lookVect.zCoord * maxReach)
        
        val target = data.attackInfo?.target ?: return false
        if (target !is IMixinEntityPlayer) return false
        
        val targetData = target.getPlayerDataSamples()
        val MAX_TICK_DELAY = 10
        if (targetData.posXList.size() < MAX_TICK_DELAY) return false
        
        val hits = BooleanArray(MAX_TICK_DELAY - 1)
        val hitDistances = DoubleArray(MAX_TICK_DELAY - 1)
        var maxDistance = -1.0
        
        for (i in 1 until MAX_TICK_DELAY) {
            if (isInsideHitbox(targetData.posXList.get(i), targetData.posYList.get(i), targetData.posZList.get(i), attackerEyePos)) {
                return false
            }
            val hitOnPlayerPos = getHitVectOnPlayer(
                targetData.posXList.get(i),
                targetData.posYList.get(i),
                targetData.posZList.get(i),
                attackerEyePos,
                lookEndPos
            )
            if (hitOnPlayerPos == null) {
                hits[i - 1] = false
                hitDistances[i - 1] = -1.0
            } else {
                hits[i - 1] = true
                hitDistances[i - 1] = attackerEyePos.distanceTo(hitOnPlayerPos)
                maxDistance = Math.max(maxDistance, hitDistances[i - 1])
            }
        }
        
        if (maxDistance == -1.0) return false
        
        val STEP_SIZE = 0.1
        val maxSteps = (maxDistance / STEP_SIZE).toInt()
        val insideBlockArray = IntArray(maxSteps + 1)
        var blockXpos = -1
        var blockYpos = -1
        var blockZpos = -1
        var canHitThroughBlock = false
        var timesInsideBlock = 0
        
        for (i in 0..maxSteps) {
            val dx = attackerEyePos.xCoord + i * STEP_SIZE * lookVect.xCoord
            val dy = attackerEyePos.yCoord + i * STEP_SIZE * lookVect.yCoord
            val dz = attackerEyePos.zCoord + i * STEP_SIZE * lookVect.zCoord
            val xpos = MathHelper.floor_double(dx)
            val ypos = MathHelper.floor_double(dy)
            val zpos = MathHelper.floor_double(dz)
            
            if (xpos != blockXpos || ypos != blockYpos || zpos != blockZpos) {
                val pos = BlockPos(xpos, ypos, zpos)
                val iblockstate = mc.theWorld.getBlockState(pos)
                val block = iblockstate.block
                canHitThroughBlock = !block.isFullBlock || !block.canCollideCheck(iblockstate, false) || recentPlacedBlocks.contains(pos)
                blockXpos = xpos
                blockYpos = ypos
                blockZpos = zpos
            }
            if (!canHitThroughBlock) {
                timesInsideBlock++
            }
            insideBlockArray[i] = timesInsideBlock
        }
        
        val f = 1.0F
        maxDistance = maxDistance + 2.0
        val nearbyPlayers = getPlayersDataInAABBexcluding(
            player,
            player.entityBoundingBox.addCoord(
                lookVect.xCoord * maxDistance,
                lookVect.yCoord * maxDistance,
                lookVect.zCoord * maxDistance
            ).expand(f.toDouble(), f.toDouble(), f.toDouble())
        ) { p ->
            p != data.attackInfo?.target && 
            p != mc.thePlayer && 
            p.canBeCollidedWith() && 
            HackerDetectorAddon.isValidPlayer(p.uniqueID) && 
            !p.isInvisible() && 
            p is IMixinEntityPlayer && 
            p.getPlayerDataSamples().posXList.size() >= MAX_TICK_DELAY
        }
        
        var b = 1000
        var p = 1000
        var reach = 0.0
        
        for (i in 1 until MAX_TICK_DELAY) {
            if (!hits[i - 1]) continue
            val iterMax = (hitDistances[i - 1] / STEP_SIZE).toInt()
            var timesInsidePlayer = 0
            if (nearbyPlayers.isNotEmpty() && nearbyPlayers.size < 15) {
                for (j in 0..iterMax) {
                    val dx = attackerEyePos.xCoord + j * STEP_SIZE * lookVect.xCoord
                    val dy = attackerEyePos.yCoord + j * STEP_SIZE * lookVect.yCoord
                    val dz = attackerEyePos.zCoord + j * STEP_SIZE * lookVect.zCoord
                    for (eData in nearbyPlayers) {
                        if (isInsideHitbox(eData.posXList.get(i), eData.posYList.get(i), eData.posZList.get(i), dx, dy, dz)) {
                            timesInsidePlayer++
                            break
                        }
                    }
                }
            }
            if (b + p > timesInsidePlayer + insideBlockArray[iterMax]) {
                b = insideBlockArray[iterMax]
                p = timesInsidePlayer
                reach = hitDistances[i - 1]
            }
            if (b + p == 0) {
                return false
            }
        }
        
        if (b + p > 0) {
            val vlb = Math.min(15, b)
            val vlp = Math.min(8, p)
            data.killAuraAVL.add(Math.min(15, vlb + (if (nearbyPlayers.size > 8) vlp / 2 else vlp)) * 25)
            if (HackerDetectorAddon.instance.debugLoggingSetting) {
                val msg = " | ${data.attackInfo?.attackType?.name}" +
                        " | target : ${data.attackInfo?.targetName}" +
                        " | b $b | p $p" +
                        " | reach ${String.format("%.2f", reach)}" +
                        " | players ${nearbyPlayers.size}"
                log(player, data, data.killAuraAVL, msg)
            }
            return true
        }
        
        return false
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
        fun newVL(): ViolationLevelTracker = ViolationLevelTracker(0, 1, 500)
    }
}
