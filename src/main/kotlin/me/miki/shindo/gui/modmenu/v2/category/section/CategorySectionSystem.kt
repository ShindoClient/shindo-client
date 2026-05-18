package me.miki.shindo.gui.modmenu.v2.category.section

import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts

data class CategorySectionSpec(
    val title: String,
    val subtitle: String? = null,
)

data class CategorySectionStyle(
    val topMargin: Float = 18f,
    val headerHeight: Float = 30f,
    val sectionGap: Float = 18f,
    val titleSize: Float = 11f,
    val subtitleSize: Float = 9f,
    val subtitleOffsetY: Float = 14f,
)

class CategorySectionCursor(
    startY: Float,
    val style: CategorySectionStyle = CategorySectionStyle(),
) {
    var y: Float = startY + style.topMargin

    fun moveBy(offset: Float) {
        y += offset
    }

    fun nextSection() {
        y += style.sectionGap
    }
}

object CategorySectionRenderer {
    @JvmStatic
    fun drawHeader(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        y: Float,
        section: CategorySectionSpec,
        style: CategorySectionStyle,
    ): Float {
        nvg.drawText(
            section.title,
            x,
            y,
            palette.getFontColor(ColorType.DARK),
            style.titleSize,
            Fonts.SEMIBOLD,
        )

        val subtitle = section.subtitle
        if (!subtitle.isNullOrEmpty()) {
            nvg.drawText(
                subtitle,
                x,
                y + style.subtitleOffsetY,
                palette.getFontColor(ColorType.NORMAL),
                style.subtitleSize,
                Fonts.REGULAR,
            )
        }

        return y + style.headerHeight
    }
}
