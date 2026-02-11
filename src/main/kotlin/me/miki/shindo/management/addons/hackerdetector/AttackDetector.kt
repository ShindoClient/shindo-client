package me.miki.shindo.management.addons.hackerdetector

import me.miki.shindo.injection.mixin.interfaces.entity.player.IMixinEntityPlayer
import me.miki.shindo.management.addons.hackerdetector.data.AttackInfo
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.Vec3
import kotlin.math.abs

object AttackDetector {

    private val mc: Minecraft = Minecraft.getMinecraft()

    private var lastPacketWasSwing = false
    private var lastSwingTime = 0L
    private var attackerID = 0
    private var lastPacketWasHurt = false
    private var lastHurtTime = 0L
    private var lastHurtID = 0
    private var consecutiveSwingHurt = false

    @JvmStatic
    fun lookForAttacks(packet: Packet<*>) {
        when (packet) {
            is S0BPacketAnimation -> {
                when (val animationType = packet.animationType) {
                    0 -> {
                        lastPacketWasHurt = false
                        consecutiveSwingHurt = false
                        lastPacketWasSwing = true
                        lastSwingTime = System.currentTimeMillis()
                        attackerID = packet.entityID
                        onEntitySwing(attackerID)
                    }

                    4, 5 -> {
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
                    if (packet.motionX != 0 || packet.motionY != 0 || packet.motionZ != 0) {
                        val attackType = if (lastPacketWasSwing) AttackType.DIRECT_VELOCITY else AttackType.VELOCITY
                        checkPlayerAttack(attackerID, packet.entityID, attackType, null)
                    }
                }
            }

            is S19PacketEntityStatus -> {
                if (packet.opCode.toInt() == 2) {
                    if (lastPacketWasSwing) consecutiveSwingHurt = true
                    lastPacketWasSwing = false
                    lastPacketWasHurt = true
                    lastHurtTime = System.currentTimeMillis()
                    val entity = packet.getEntity(mc.theWorld)
                    if (entity != null) {
                        lastHurtID = entity.entityId
                    }
                }
            }

            is S29PacketSoundEffect -> {
                if (lastPacketWasSwing && System.currentTimeMillis() - lastSwingTime < 2) {
                    when {
                        packet.soundName == "mob.guardian.elder.hit" && packet.pitch > 1f -> {
                            checkPlayerHit(
                                attackerID,
                                AttackType.DREADLORDHIT,
                                Vec3(packet.x, packet.y, packet.z)
                            )
                        }

                        packet.soundName == "note.harp" -> {
                            checkPlayerHit(
                                attackerID,
                                AttackType.SHAMANHIT,
                                Vec3(packet.x, packet.y, packet.z)
                            )
                        }
                    }
                } else if (lastPacketWasHurt && System.currentTimeMillis() - lastSwingTime < 2 && System.currentTimeMillis() - lastHurtTime < 2) {
                    when (packet.soundName) {
                        "game.player.hurt" -> {
                            val attackType =
                                if (consecutiveSwingHurt) AttackType.DIRECTHURTSOUND else AttackType.HURTSOUND
                            checkPlayerAttack(
                                attackerID,
                                lastHurtID,
                                attackType,
                                Vec3(packet.x, packet.y, packet.z)
                            )
                        }
                        "game.player.die" -> {
                            val attackType =
                                if (consecutiveSwingHurt) AttackType.DIRECTDEATHSOUND else AttackType.DEATHSOUND
                            checkPlayerAttack(
                                attackerID,
                                lastHurtID,
                                attackType,
                                Vec3(packet.x, packet.y, packet.z)
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
        HackerDetectorAddon.instance.addScheduledTask(Runnable {
            val attacker = mc.theWorld?.getEntityByID(entityID)
            if (attacker is IMixinEntityPlayer) {
                attacker.getPlayerDataSamples().hasSwung = true
            }
        })
    }

    private fun checkPlayerHit(playerId: Int, attackType: AttackType, soundPos: Vec3) {
        HackerDetectorAddon.instance.addScheduledTask(Runnable {
            val player = mc.theWorld?.getEntityByID(playerId) as? EntityPlayer ?: return@Runnable
            if (player is IMixinEntityPlayer) {
                val data = player.getPlayerDataSamples()
                if (data.attackInfo == null) {
                    data.attackInfo = AttackInfo(null, attackType)
                }
            }
        })
    }

    private fun checkPlayerAttack(attackerEntityId: Int, targetEntityId: Int, attackType: AttackType, soundPos: Vec3?) {
        HackerDetectorAddon.instance.addScheduledTask(Runnable {
            val attacker = mc.theWorld?.getEntityByID(attackerEntityId) as? EntityPlayer ?: return@Runnable
            val target = mc.theWorld?.getEntityByID(targetEntityId) as? EntityPlayer ?: return@Runnable

            if (attacker == target) return@Runnable

            val xDiff = abs(mc.thePlayer.posX - target.posX)
            val zDiff = abs(mc.thePlayer.posZ - target.posZ)
            if (xDiff > 56.0 || zDiff > 56.0) return@Runnable

            if (attacker.getDistanceSqToEntity(target) > 64.0) return@Runnable

            if (attacker !is IMixinEntityPlayer) return@Runnable

            val data = attacker.getPlayerDataSamples()
            val attackInfo = data.attackInfo
            if (attackInfo == null) {
                data.attackInfo = AttackInfo(target, attackType)
            } else if (attackInfo.target == null) {
                attackInfo.target = target
            } else if (attackInfo.target != target) {
                attackInfo.multiTarget = true
            }
        })
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
