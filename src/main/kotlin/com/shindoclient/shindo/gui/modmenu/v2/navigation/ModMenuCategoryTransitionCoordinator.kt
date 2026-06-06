package com.shindoclient.shindo.gui.modmenu.v2.navigation

import com.shindoclient.shindo.gui.modmenu.v2.category.Category
import com.shindoclient.shindo.gui.modmenu.v2.style.ModMenuMotion
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import kotlin.math.max
import kotlin.math.min

/**
 * Coordinates category transition state for ModMenu content area.
 *
 * A single coordinator keeps transition behavior consistent across:
 * - sidebar/category switching;
 * - keyboard navigation switching.
 */
class ModMenuCategoryTransitionCoordinator {
    enum class State {
        IDLE,
        SWITCHING,
    }

    data class RenderLayer(
        val category: Category,
        val offsetX: Float,
        val alpha: Float,
    )

    private val progressAnimation = SimpleAnimation(1f)
    private var fromCategory: Category? = null
    private var toCategory: Category? = null
    private var directionSign = 1
    private var state: State = State.IDLE

    fun reset(activeCategory: Category) {
        fromCategory = null
        toCategory = activeCategory
        directionSign = 1
        progressAnimation.setValue(1f)
        state = State.IDLE
    }

    fun getActiveCategory(fallback: Category): Category {
        val active = toCategory ?: fallback
        if (toCategory == null) {
            toCategory = active
        }
        return active
    }

    fun requestSwitch(
        currentCategory: Category,
        targetCategory: Category,
        directionHint: Int,
    ) {
        if (currentCategory == targetCategory && !isTransitioning()) {
            toCategory = targetCategory
            fromCategory = null
            progressAnimation.setValue(1f)
            state = State.IDLE
            return
        }

        val visibleCurrent = toCategory ?: currentCategory
        fromCategory = visibleCurrent
        toCategory = targetCategory
        directionSign = if (directionHint < 0) -1 else 1
        progressAnimation.setValue(0f)
        state = State.SWITCHING
    }

    fun update() {
        if (fromCategory == null || toCategory == null) {
            progressAnimation.setValue(1f)
            state = State.IDLE
            return
        }

        progressAnimation.setAnimation(1f, ModMenuMotion.CATEGORY_CONTENT_SWITCH_SPEED)
        if (progressAnimation.getValue() >= 0.999f) {
            fromCategory = null
            progressAnimation.setValue(1f)
            state = State.IDLE
        }
    }

    fun isTransitioning(): Boolean = state == State.SWITCHING && fromCategory != null && progressAnimation.getValue() < 0.999f

    fun getState(): State = state

    fun collectVisibleCategories(): Set<Category> {
        val visible = LinkedHashSet<Category>(2)
        fromCategory?.let { visible.add(it) }
        toCategory?.let { visible.add(it) }
        return visible
    }

    fun buildRenderLayers(contentWidth: Float): List<RenderLayer> {
        val active = toCategory ?: return emptyList()
        val leaving = fromCategory ?: return listOf(RenderLayer(category = active, offsetX = 0f, alpha = 1f))

        val progress = progressAnimation.getValue().coerceIn(0f, 1f)
        val distance = resolveSlideDistance(contentWidth)

        val enterOffset = directionSign * (1f - progress) * distance
        val leaveOffset = -directionSign * progress * (distance * 0.7f)

        val enterAlpha = (0.25f + progress * 0.75f).coerceIn(0f, 1f)
        val leaveAlpha = (1f - progress).coerceIn(0f, 1f)

        val layers = ArrayList<RenderLayer>(2)
        if (leaveAlpha > 0.01f) {
            layers.add(RenderLayer(category = leaving, offsetX = leaveOffset, alpha = leaveAlpha))
        }
        layers.add(RenderLayer(category = active, offsetX = enterOffset, alpha = enterAlpha))
        return layers
    }

    private fun resolveSlideDistance(contentWidth: Float): Float {
        val scaled = contentWidth * ModMenuMotion.CATEGORY_CONTENT_SLIDE_FACTOR
        return max(
            ModMenuMotion.CATEGORY_CONTENT_SLIDE_MIN,
            min(ModMenuMotion.CATEGORY_CONTENT_SLIDE_MAX, scaled),
        )
    }
}
