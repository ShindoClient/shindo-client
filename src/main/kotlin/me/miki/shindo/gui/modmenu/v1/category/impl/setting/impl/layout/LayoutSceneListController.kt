package me.miki.shindo.gui.modmenu.v1.category.impl.setting.impl.layout

import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import kotlin.math.max

/**
 * Handles index-list drawing, scrolling and hit testing for [LayoutScene].
 */
class LayoutSceneListController {

    private val scroll = Scroll()
    private val cardSlots = ArrayList<CardSlot>()

    data class CardSlot(
        val controller: LayoutAreaController,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    ) {
        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }
    }

    fun reset() {
        scroll.resetAll()
        cardSlots.clear()
    }

    fun drawList(
        controllers: List<LayoutAreaController>,
        activeController: LayoutAreaController?,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        baseX: Float,
        baseY: Float,
        baseWidth: Float,
        baseHeight: Float
    ) {
        val listPadding = 15f
        val entryHeight = 52f
        val entrySpacing = 10f

        val contentHeight = listPadding * 2f +
                controllers.size * entryHeight +
                max(0, controllers.size - 1) * entrySpacing
        scroll.maxScroll = max(0f, contentHeight - baseHeight)

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            scroll.onScroll()
        }
        scroll.onAnimation()

        cardSlots.clear()
        var offsetY = listPadding
        var i = 0
        while (i < controllers.size) {
            val controller = controllers[i]
            val cardX = baseX + 18f
            val cardY = baseY + offsetY + scroll.getValue()
            val cardWidth = max(0f, baseWidth - 36f)
            cardSlots.add(CardSlot(controller, cardX, cardY, cardWidth, entryHeight))
            controller.drawCard(
                mouseX,
                mouseY,
                partialTicks,
                cardX,
                cardY,
                cardWidth,
                entryHeight,
                activeController == controller,
                activeController == null
            )
            offsetY += entryHeight + entrySpacing
            i++
        }
    }

    fun findClickedController(mouseX: Int, mouseY: Int): LayoutAreaController? {
        var i = 0
        while (i < cardSlots.size) {
            val slot = cardSlots[i]
            if (slot.contains(mouseX, mouseY)) {
                return slot.controller
            }
            i++
        }
        return null
    }
}
