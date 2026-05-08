package me.miki.shindo.gui.modmenu.v2.render

import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.category.impl.CosmeticsCategory
import me.miki.shindo.gui.modmenu.v2.style.ModMenuStyle
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.components.v2.buttons.CompIconButton
import me.miki.shindo.ui.components.v2.inputs.CompSearchBox
import kotlin.math.max
import kotlin.math.min

class ModMenuHeaderController {

    data class HeaderRenderResult(val contentOffsetY: Int)

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

        if (currentCategory.isShowTitle()) {
            drawTitle(nvg, palette, currentCategory, menuX, menuY, compact)
        }

        if (currentCategory.isShowSearchBox()) {
            drawSearchBox(searchBox, menuX, menuY, menuWidth, mouseX, mouseY, partialTicks)
        }

        drawFolderButton(folderButton, currentCategory, searchBox, menuX, menuY, mouseX, mouseY, partialTicks)

        val contentY = if (currentCategory.isShowTitle()) ModMenuStyle.CONTENT_MIN_TOP_WITH_TITLE.toInt() else 0
        return HeaderRenderResult(contentOffsetY = contentY)
    }

    private fun drawTitle(
        nvg: NanoVGManager,
        palette: ColorPalette,
        category: Category,
        menuX: Int,
        menuY: Int,
        compact: Boolean
    ) {
        val progress = category.getTextAnimation().getValue()
        val titleSize = if (compact) ModMenuStyle.CATEGORY_TITLE_SIZE_COMPACT else ModMenuStyle.CATEGORY_TITLE_SIZE

        nvg.save()
        nvg.translate(progress * 15f, 0f)
        nvg.drawText(
            category.getName(),
            menuX + ModMenuStyle.CATEGORY_TITLE_X,
            menuY + ModMenuStyle.CATEGORY_TITLE_Y,
            palette.getFontColor(ColorType.DARK, (progress * 255f).toInt().coerceIn(0, 255)),
            titleSize,
            Fonts.SEMIBOLD
        )
        nvg.restore()
    }

    private fun drawSearchBox(
        searchBox: CompSearchBox,
        menuX: Int,
        menuY: Int,
        menuWidth: Int,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        val searchWidth = resolveSearchWidth(menuWidth.toFloat())
        searchBox.setPosition(menuX + menuWidth - searchWidth - 14f, menuY + 6.5f, searchWidth, 18f)
        searchBox.draw(mouseX, mouseY, partialTicks)
    }

    private fun drawFolderButton(
        folderButton: CompIconButton,
        category: Category,
        searchBox: CompSearchBox,
        menuX: Int,
        menuY: Int,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        if (category !is CosmeticsCategory || !category.shouldShowCustomCapeFolder()) {
            folderButton.setVisible(false)
            return
        }

        val buttonX = if (category.isShowSearchBox()) searchBox.getX() - 22f
        else menuX + category.getWidth() - 36f

        folderButton.setVisible(true)
        folderButton.setBounds(buttonX, menuY + 6.5f, 18f, 18f)
        folderButton.draw(mouseX, mouseY, partialTicks)
    }

    private fun resolveSearchWidth(menuWidth: Float): Float =
        max(108f, min(168f, menuWidth * 0.33f))
}