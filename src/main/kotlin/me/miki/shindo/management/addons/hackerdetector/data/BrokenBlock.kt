package me.miki.shindo.management.addons.hackerdetector.data

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos

/**
 * Representa um bloco quebrado durante este tick
 */
class BrokenBlock(
    val block: Block,
    val blockPos: BlockPos,
    val tool: String
) {
    val breakTime: Long = System.currentTimeMillis()
    var playerList: MutableList<EntityPlayer>? = null
    
    fun addPlayer(player: EntityPlayer) {
        if (playerList == null) {
            playerList = mutableListOf()
        }
        playerList?.add(player)
    }
}
