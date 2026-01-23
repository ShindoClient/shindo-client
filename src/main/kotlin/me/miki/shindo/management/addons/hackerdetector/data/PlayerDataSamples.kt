package me.miki.shindo.management.addons.hackerdetector.data

import me.miki.shindo.management.addons.hackerdetector.checks.*
import me.miki.shindo.management.addons.hackerdetector.utils.Vector2D
import me.miki.shindo.management.addons.hackerdetector.utils.ViolationLevelTracker
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3

/**
 * Amostras de dados do jogador para detecção de cheats
 * Armazena histórico de posições, rotações, ataques, etc.
 */
class PlayerDataSamples {
    
    /** Usado para garantir que verificamos cada jogador apenas uma vez por tick */
    var checkedThisTick: Boolean = false
    
    /** Quantidade de ticks que o jogador passou no chão */
    var onGroundTime: Int = 0
    
    /** Quantidade de ticks que o jogador passou no ar */
    var airTime: Int = 0
    
    /** Quantidade de ticks desde que o jogador começou a correr */
    var sprintTime: Int = 0
    
    /** Quantidade de ticks desde que o jogador está usando um item */
    var useItemTime: Int = 0
    
    /** Quantidade de ticks desde que o jogador começou a comer */
    var timeEating: Int = 0
    
    /** Quantidade de ticks desde que o jogador terminou de comer algo */
    var lastEatTime: Int = 50
    
    /** True se o item em uso é comida, poção ou balde de leite */
    var usedItemIsConsumable: Boolean = false
    
    /** True quando recebemos um pacote de swing desta entidade durante o último tick */
    var hasSwung: Boolean = false
    
    val swingList = SampleListZ(20)
    
    /** Info sobre ataque que aconteceu neste tick se houver */
    var attackInfo: AttackInfo? = null
    val attackList = SampleListZ(20)
    
    /** Última vez que o jogador quebrou um bloco */
    var lastBreakBlockTime: Long = System.currentTimeMillis()
    val breakTimeRatio = SampleListF(8)
    
    /** Último bloco tocado por este jogador */
    var blockTouched: BlockPos? = null
    
    /* ----- Amostras de rotações/posições interpoladas pelo cliente ----- */
    val posXList = SampleListD(10)
    val posYList = SampleListD(10)
    val posZList = SampleListD(10)
    val speedXList = SampleListD(5)
    val speedYList = SampleListD(5)
    val speedZList = SampleListD(5)
    /* ----- Amostras do cliente terminam ----- */
    
    /* ----- Amostras de rotações/posições recebidas do servidor ----- */
    private var serverUpdates: Int = 0
    val serverUpdatesList = SampleListI(20)
    val serverPosXList = SampleListD(5)
    val serverPosYList = SampleListD(5)
    val serverPosZList = SampleListD(5)
    /** Yaw do corpo do jogador [-180, 180] */
    val serverYawList = SampleListF(5)
    /** Pitch da cabeça do jogador [-90, 90] */
    val serverPitchList = SampleListF(5)
    /** Yaw da cabeça do jogador [-180, 180], igual a player.rotationYawHead */
    val serverYawHeadList = SampleListF(5)
    /* ----- Amostras do servidor terminam ----- */
    
    /* Violation Level Trackers para cada check */
    val autoblockAVL = AutoblockCheck.newVL()
    val fastbreakVL = FastbreakCheck.newVL()
    val ghosthandVL = GhosthandCheck.newVl()
    val keepsprintAVL = KeepSprintACheck.newVL()
    val keepSprintBVL = KeepSprintBCheck.newVL()
    val killAuraAVL = KillAuraACheck.newVL()
    val killAuraBVL = KillAuraBCheck.newVL()
    val noSlowdownVL = NoSlowdownCheck.newVL()
    val scaffoldVL = ScaffoldCheck.newVL()
    val reachVL = ReachCheck.newVL()
    val flyVL = FlyCheck.newVL()
    val speedVL = SpeedCheck.newVL()
    val jesusVL = JesusCheck.newVL()
    val noFallVL = NoFallCheck.newVL()
    
    fun onTickStart() {
        checkedThisTick = false
        hasSwung = false
        attackInfo = null
        serverUpdates = 0
    }
    
    fun onTick(player: EntityPlayer) {
        checkedThisTick = true
        onGroundTime = if (player.onGround) onGroundTime + 1 else 0
        airTime = if (player.onGround) 0 else airTime + 1
        sprintTime = if (player.isSprinting) sprintTime + 1 else 0
        
        val isUsingItem = player.isEating && player.heldItem != null && player.heldItem!!.maxItemUseDuration > 0
        if (!isUsingItem && usedItemIsConsumable && useItemTime > 25) {
            lastEatTime = 0
        }
        lastEatTime++
        
        if (isUsingItem) {
            usedItemIsConsumable = player.heldItem!!.maxItemUseDuration == 32
            useItemTime++
            timeEating = if (usedItemIsConsumable) timeEating + 1 else 0
        } else {
            useItemTime = 0
            timeEating = 0
        }
        
        swingList.add(hasSwung)
        attackList.add(hasAttacked())
        posXList.add(player.posX)
        posYList.add(player.posY)
        posZList.add(player.posZ)
        speedXList.add((player.posX - player.lastTickPosX) * 20.0)
        speedYList.add((player.posY - player.lastTickPosY) * 20.0)
        speedZList.add((player.posZ - player.lastTickPosZ) * 20.0)
        serverUpdatesList.add(serverUpdates)
    }
    
    fun onPostChecks() {
        attackInfo?.target = null
        blockTouched = null
    }
    
    fun setPositionAndRotation(x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
        serverUpdates++
        serverPosXList.add(x)
        serverPosYList.add(y)
        serverPosZList.add(z)
        serverYawList.add(yaw)
        serverPitchList.add(pitch)
    }
    
    fun setRotationYawHead(yawHead: Float) {
        serverYawHeadList.add(yawHead)
    }
    
    /** True se o jogador atacou outro jogador durante este tick */
    fun hasAttacked(): Boolean = attackInfo != null && !attackInfo!!.multiTarget
    
    /** True se o jogador atacou outro jogador durante este tick e encontramos o alvo */
    fun hasAttackedTarget(): Boolean = attackInfo != null && !attackInfo!!.multiTarget && attackInfo!!.target != null
    
    fun isOnFlatGround(): Boolean {
        return Math.abs(serverPosYList.get(0) - serverPosYList.get(1)) < 0.001 &&
                Math.abs(serverPosYList.get(1) - serverPosYList.get(2)) < 0.001 &&
                Math.abs(serverPosYList.get(2) - serverPosYList.get(3)) < 0.001
    }
    
    /** True se a posição do jogador no plano XZ é idêntica ao último tick */
    fun isNotMovingXZ(): Boolean = speedXList.get(0) == 0.0 && speedZList.get(0) == 0.0
    
    fun getSpeedXZ(): Double {
        val vx = speedXList.get(0)
        val vz = speedZList.get(0)
        return Math.sqrt(vx * vx + vz * vz)
    }
    
    fun getSpeedXZSq(): Double {
        val vx = speedXList.get(0)
        val vz = speedZList.get(0)
        return vx * vx + vz * vz
    }
    
    fun getPositionEyesServer(player: EntityPlayer): Vec3 {
        return Vec3(
            serverPosXList.get(0),
            serverPosYList.get(0) + player.eyeHeight,
            serverPosZList.get(0)
        )
    }
    
    fun getLookServer(): Vec3 {
        return getVectorForRotation(serverPitchList.get(0), serverYawHeadList.get(0))
    }
    
    fun getMoveLookAngleDiff(): Double {
        return MathHelper.wrapAngleTo180_double(
            Vector2D(speedZList.get(0), -speedXList.get(0)).getOrientedAngle() - serverYawHeadList.get(0)
        )
    }
    
    /**
     * Cria um Vec3 usando pitch e yaw da rotação da entidade
     */
    private fun getVectorForRotation(pitch: Float, yaw: Float): Vec3 {
        val f = MathHelper.cos(-yaw * 0.017453292F - Math.PI.toFloat())
        val f1 = MathHelper.sin(-yaw * 0.017453292F - Math.PI.toFloat())
        val f2 = -MathHelper.cos(-pitch * 0.017453292F)
        val f3 = MathHelper.sin(-pitch * 0.017453292F)
        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }
}
