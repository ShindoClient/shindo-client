package me.miki.shindo.management.addons.hackerdetector

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.data.AttackInfo
import me.miki.shindo.management.addons.hackerdetector.data.PlayerDataSamples
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.Vec3

/**
 * Detecta ataques de jogadores analisando pacotes de rede
 */
object AttackDetector {
    
    private val mc: Minecraft = Minecraft.getMinecraft()
    
    private var lastPacketWasSwing = false
    private var lastSwingTime = 0L
    private var attackerID = 0
    private var lastPacketWasHurt = false
    private var lastHurtTime = 0L
    private var lastHurtID = 0
    private var consecutiveSwingHurt = false

    /**
     * Analisa pacotes para detectar ataques
     * CUIDADO: Este código não é chamado da thread principal
     */
    @JvmStatic
    fun lookForAttacks(packet: Packet<*>) {
        when (packet) {
            is S0BPacketAnimation -> {
                val animationType = packet.animationType
                when (animationType) {
                    0 -> { // Swing packet
                        lastPacketWasHurt = false
                        consecutiveSwingHurt = false
                        lastPacketWasSwing = true
                        lastSwingTime = System.currentTimeMillis()
                        attackerID = packet.entityID
                        onEntitySwing(attackerID)
                    }

                    4, 5 -> { // critical (4) / enchant particle (5)
                        if (System.currentTimeMillis() - lastSwingTime < 2) {
                            val attackType = if (animationType == 4) {
                                if (lastPacketWasSwing) AttackType.DIRECT_CRITICAL else AttackType.CRITICAL
                            } else {
                                if (lastPacketWasSwing) AttackType.DIRECT_SHARPNESS else AttackType.SHARPNESS
                            }
                            checkPlayerAttack(attackerID, packet.entityID, attackType, null)
                        }
                    }
                }
            }

            is S12PacketEntityVelocity -> {
                if (System.currentTimeMillis() - lastSwingTime < 2) {
                    val packetVelo = packet
                    if (packetVelo.motionX != 0 || packetVelo.motionY != 0 || packetVelo.motionZ != 0) {
                        val attackType = if (lastPacketWasSwing) AttackType.DIRECT_VELOCITY else AttackType.VELOCITY
                        checkPlayerAttack(attackerID, packetVelo.entityID, attackType, null)
                    }
                }
            }

            is S19PacketEntityStatus -> {
                if (packet.opCode.toInt() == 2) { // Entity gets hurt (2)
                    if (lastPacketWasSwing) consecutiveSwingHurt = true
                    lastPacketWasSwing = false
                    lastPacketWasHurt = true
                    lastHurtTime = System.currentTimeMillis()
                    // Obtém entityId através do método getEntity
                    val entity = packet.getEntity(mc.theWorld)
                    if (entity != null) {
                        lastHurtID = entity.entityId
                    }
                }
            }

            is S29PacketSoundEffect -> {
                val soundPacket = packet
                if (lastPacketWasSwing && System.currentTimeMillis() - lastSwingTime < 2) {
                    when {
                        soundPacket.soundName == "mob.guardian.elder.hit" && soundPacket.pitch > 1f -> {
                            checkPlayerHit(
                                attackerID,
                                AttackType.DREADLORDHIT,
                                Vec3(soundPacket.x, soundPacket.y, soundPacket.z)
                            )
                        }

                        soundPacket.soundName == "note.harp" -> {
                            checkPlayerHit(
                                attackerID,
                                AttackType.SHAMANHIT,
                                Vec3(soundPacket.x, soundPacket.y, soundPacket.z)
                            )
                        }
                    }
                } else if (lastPacketWasHurt && System.currentTimeMillis() - lastSwingTime < 2 && System.currentTimeMillis() - lastHurtTime < 2) {
                    when {
                        soundPacket.soundName == "game.player.hurt" -> {
                            val attackType =
                                if (consecutiveSwingHurt) AttackType.DIRECTHURTSOUND else AttackType.HURTSOUND
                            checkPlayerAttack(
                                attackerID,
                                lastHurtID,
                                attackType,
                                Vec3(soundPacket.x, soundPacket.y, soundPacket.z)
                            )
                        }

                        soundPacket.soundName == "game.player.die" -> {
                            val attackType =
                                if (consecutiveSwingHurt) AttackType.DIRECTDEATHSOUND else AttackType.DEATHSOUND
                            checkPlayerAttack(
                                attackerID,
                                lastHurtID,
                                attackType,
                                Vec3(soundPacket.x, soundPacket.y, soundPacket.z)
                            )
                        }
                    }
                }
            }
        }

        lastPacketWasHurt = false
        lastPacketWasSwing = false
        consecutiveSwingHurt = false
    }
    
    private fun onEntitySwing(entityID: Int) {
        HackerDetectorAddon.instance.addScheduledTask {
            val attacker = mc.theWorld?.getEntityByID(entityID)
            if (attacker is IMixinEntityPlayer) {
                attacker.getPlayerDataSamples().hasSwung = true
            }
        }
    }
    
    private fun checkPlayerHit(playerId: Int, attackType: AttackType, soundPos: Vec3) {
        HackerDetectorAddon.instance.addScheduledTask {
            val player = mc.theWorld?.getEntityByID(playerId) as? EntityPlayer ?: return@addScheduledTask
            if (player is IMixinEntityPlayer) {
                val data = player.getPlayerDataSamples()
                if (data.attackInfo == null) {
                    data.attackInfo = AttackInfo(null, attackType)
                }
            }
        }
    }
    
    private fun checkPlayerAttack(attackerEntityId: Int, targetEntityId: Int, attackType: AttackType, soundPos: Vec3?) {
        HackerDetectorAddon.instance.addScheduledTask {
            val attacker = mc.theWorld?.getEntityByID(attackerEntityId) as? EntityPlayer ?: return@addScheduledTask
            val target = mc.theWorld?.getEntityByID(targetEntityId) as? EntityPlayer ?: return@addScheduledTask
            
            if (attacker == target) return@addScheduledTask
            
            val xDiff = Math.abs(mc.thePlayer.posX - target.posX)
            val zDiff = Math.abs(mc.thePlayer.posZ - target.posZ)
            if (xDiff > 56.0 || zDiff > 56.0) return@addScheduledTask
            
            if (attacker.getDistanceSqToEntity(target) > 64.0) return@addScheduledTask
            
            if (attacker !is IMixinEntityPlayer) return@addScheduledTask
            
            val data = attacker.getPlayerDataSamples()
            val attackInfo = data.attackInfo
            if (attackInfo == null) {
                data.attackInfo = AttackInfo(target, attackType)
            } else if (attackInfo.target == null) {
                attackInfo.target = target
            } else if (attackInfo.target != target) {
                attackInfo.multiTarget = true
            }
        }
    }
    
    enum class AttackType {
        CRITICAL,
        DEATHSOUND,
        DIRECTDEATHSOUND,
        DIRECTHURTSOUND,
        DIRECT_CRITICAL,
        DIRECT_SHARPNESS,
        DIRECT_VELOCITY,
        DREADLORDHIT,
        HURTSOUND,
        SHAMANHIT,
        SHARPNESS,
        VELOCITY
    }
}
