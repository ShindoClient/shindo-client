package me.miki.shindo.gui.modmenu.v2.navigation

import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.style.ModMenuMotion
import me.miki.shindo.gui.modmenu.v2.style.ModMenuStyle
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.MathUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * Keeps sidebar slot geometry and interaction state out of [me.miki.shindo.gui.modmenu.v2.GuiModMenu].
 */
class ModMenuSidebarController {
    private val slots = ArrayList<ModMenuSidebarSlot>()
    private val activeSlotAnimation = SimpleAnimation()
    private val hoverAnimations = HashMap<Category, SimpleAnimation>()
    private var slotGap = 22f

    fun rebuildSlots(
        categories: List<Category>,
        currentCategory: Category,
        startX: Float,
        startY: Float,
        slotSize: Float,
        gap: Float,
        maxBottomY: Float,
    ) {
        slotGap =
            resolveGap(
                categoryCount = categories.size,
                startY = startY,
                slotSize = slotSize,
                preferredGap = gap,
                maxBottomY = maxBottomY,
            )
        slots.clear()

        var y = startY
        for (category in categories) {
            slots.add(ModMenuSidebarSlot(category, startX, y, slotSize))
            hoverAnimations.putIfAbsent(category, SimpleAnimation())
            y += slotGap
        }

        val activeIndex = categories.indexOf(currentCategory).coerceAtLeast(0)
        activeSlotAnimation.setAnimation(activeIndex * slotGap, ModMenuMotion.CATEGORY_SWITCH_SPEED)
    }

    fun draw(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        currentCategory: Category,
        mouseX: Int,
        mouseY: Int,
    ) {
        if (slots.isEmpty()) {
            return
        }

        val first = slots[0]
        nvg.drawGradientRoundedRect(
            first.x,
            first.y + activeSlotAnimation.getValue(),
            first.size,
            first.size,
            5f,
            accent.getColor1(),
            accent.getColor2(),
        )

        for (i in slots.indices) {
            val slot = slots[i]
            val category = slot.category
            val hovered = slot.contains(mouseX, mouseY)
            val hoverAnimation = hoverAnimations[category] ?: SimpleAnimation()
            hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 16.0)
            hoverAnimations[category] = hoverAnimation

            val inActiveRange =
                MathUtils.isInRange(
                    activeSlotAnimation.getValue(),
                    (i * slotGap) - 8f,
                    (i * slotGap) + 8f,
                )
            val normalColor = palette.getFontColor(ColorType.NORMAL)
            val hoverColor = ColorUtils.applyAlpha(Color.WHITE, 235)
            val targetColor =
                when {
                    inActiveRange -> {
                        Color.WHITE
                    }

                    hoverAnimation.getValue() > 0.01f -> {
                        ColorUtils.interpolateColor(
                            normalColor,
                            hoverColor,
                            hoverAnimation.getValue().toDouble(),
                        )
                    }

                    else -> {
                        normalColor
                    }
                }
            val iconColor = category.getTextColorAnimation().getColor(targetColor, 18)

            category.getTextAnimation().setAnimation(
                if (category == currentCategory) 1.0f else 0.0f,
                ModMenuMotion.CATEGORY_ICON_FADE_SPEED,
            )

            nvg.drawText(
                category.getIcon(),
                slot.x + 3.5f,
                slot.y + 3.5f,
                iconColor,
                14f,
                Fonts.LEGACYICON,
            )
        }
    }

    fun resolveClickedCategory(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ): Category? {
        if (mouseButton != 0) {
            return null
        }
        for (slot in slots) {
            if (slot.contains(mouseX, mouseY)) {
                return slot.category
            }
        }
        return null
    }

    private fun resolveGap(
        categoryCount: Int,
        startY: Float,
        slotSize: Float,
        preferredGap: Float,
        maxBottomY: Float,
    ): Float {
        if (categoryCount <= 1) {
            return preferredGap
        }

        val availableHeight = max(0f, maxBottomY - startY - slotSize)
        val targetGap = availableHeight / (categoryCount - 1).toFloat()
        return min(preferredGap, max(ModMenuStyle.SIDEBAR_MIN_ITEM_GAP, targetGap))
    }
}
