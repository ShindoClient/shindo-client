package me.miki.shindo.gui.modmenu.v1.render

import me.miki.shindo.gui.modmenu.v1.category.Category
import me.miki.shindo.gui.modmenu.v1.category.impl.CosmeticsCategory
import me.miki.shindo.gui.modmenu.v1.style.ModMenuStyle
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.components.v1.buttons.CompIconButton
import me.miki.shindo.ui.components.v1.inputs.CompSearchBox
import kotlin.math.max
import kotlin.math.min

/**
 * Centralizes ModMenu header/title/search/context-button rendering.
 */
class ModMenuHeaderController {

    data class HeaderRenderResult(
        val contentOffsetY: Int
    )

    fun draw(
        nvg: NanoVGManager,
        palette: ColorPalette,
        currentCategory: Category,
        menuX: Int,
        menuY: Int,
        menuWidth: Int,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        searchBox: CompSearchBox,
        folderButton: CompIconButton
    ): HeaderRenderResult {
        val compact = menuWidth <= 448
        val titleSize = if (compact) ModMenuStyle.CATEGORY_TITLE_SIZE_COMPACT else ModMenuStyle.CATEGORY_TITLE_SIZE

        if (currentCategory.isShowTitle()) {
            val animationProgress = currentCategory.getTextAnimation().value
            val titleAlpha = (animationProgress * 255f).toInt().coerceIn(0, 255)
            nvg.save()
            nvg.translate(animationProgress * 15f, 0f)
            nvg.drawText(
                currentCategory.getName(),
                menuX + ModMenuStyle.CATEGORY_TITLE_X,
                menuY + ModMenuStyle.CATEGORY_TITLE_Y,
                palette.getFontColor(ColorType.DARK, titleAlpha),
                titleSize,
                Fonts.SEMIBOLD
            )
            nvg.restore()
        }

        if (currentCategory.isShowSearchBox()) {
            val searchWidth = resolveSearchWidth(menuWidth.toFloat())
            searchBox.setPosition(
                menuX + menuWidth - searchWidth - 14f,
                menuY + 6.5f,
                searchWidth,
                18f
            )
            searchBox.draw(mouseX, mouseY, partialTicks)
        }

        folderButton.setVisible(false)
        if (currentCategory is CosmeticsCategory && currentCategory.shouldShowCustomCapeFolder()) {
            val buttonX = if (currentCategory.isShowSearchBox()) {
                searchBox.getX() - 22f
            } else {
                menuX + menuWidth - 36f
            }
            folderButton.setVisible(true)
            folderButton.setBounds(buttonX, menuY + 6.5f, 18f, 18f)
            folderButton.draw(mouseX, mouseY, partialTicks)
        }

        val contentY = if (currentCategory.isShowTitle()) ModMenuStyle.CONTENT_MIN_TOP_WITH_TITLE.toInt() else 0
        return HeaderRenderResult(contentOffsetY = contentY)
    }

    private fun resolveSearchWidth(menuWidth: Float): Float {
        val preferred = min(168f, menuWidth * 0.33f)
        return max(108f, preferred)
    }
}
