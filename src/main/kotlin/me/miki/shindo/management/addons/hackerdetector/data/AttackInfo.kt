package me.miki.shindo.management.addons.hackerdetector.data

import me.miki.shindo.management.addons.hackerdetector.AttackDetector
import net.minecraft.entity.player.EntityPlayer

/**
 * Informações sobre um ataque detectado
 */
class AttackInfo(
    target: EntityPlayer?,
    val attackType: AttackDetector.AttackType
) {
    var target: EntityPlayer? = target
        set(newTarget) {
            field = newTarget
            targetName = newTarget?.name
        }
    
    var targetName: String? = target?.name
    /**
     * Usado para filtrar eventos de dano de habilidades que atingem múltiplos jogadores
     * simultaneamente, evitando confundir com um jogador atacando múltiplas entidades
     */
    var multiTarget: Boolean = false
}
