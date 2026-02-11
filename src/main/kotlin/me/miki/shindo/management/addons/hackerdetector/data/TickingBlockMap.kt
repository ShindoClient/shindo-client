package me.miki.shindo.management.addons.hackerdetector.data

import net.minecraft.util.BlockPos
import java.util.*

class TickingBlockMap {

    companion object {
        private const val MAX_TICK = 20
    }

    private var tickTime = 0
    private val deque: Deque<BlockPlaced> = ArrayDeque(100)
    private val map: MutableMap<BlockPos, Int> = HashMap(100)

    fun add(pos: BlockPos) {
        deque.add(BlockPlaced(tickTime, pos))
        map.merge(pos, 1) { old, _ -> old + 1 }
    }

    fun contains(pos: BlockPos): Boolean = map.containsKey(pos)

    fun size(): Int = map.size

    fun onTick() {
        tickTime++
        while (true) {
            val block = deque.peekFirst() ?: break
            if (block.tickPlaced + MAX_TICK >= tickTime) break

            deque.removeFirst()
            val count = map[block.pos] ?: continue

            if (count == 1) {
                map.remove(block.pos)
            } else {
                map[block.pos] = count - 1
            }
        }
    }

    override fun toString(): String = "${map.size}/${deque.size}"

    private data class BlockPlaced(
        val tickPlaced: Int,
        val pos: BlockPos
    )
}
