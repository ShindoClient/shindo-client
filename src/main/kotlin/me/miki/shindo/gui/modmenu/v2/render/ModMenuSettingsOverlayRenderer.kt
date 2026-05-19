package me.miki.shindo.gui.modmenu.v2.render

import me.miki.shindo.gui.modmenu.v2.style.ModMenuSettingsOverlayStyle
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.ui.components.v2.layout.SettingsPanel
import me.miki.shindo.ui.components.v2.layout.settingspanel.SettingsPanelStyle
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.Scroll

/**
 * Shared renderer/layout helper for ModMenu settings overlays.
 *
 * This keeps panel shell rendering and sizing logic in one place for
 * categories that open a SettingsPanel drawer (Module and Addon).
 */
object ModMenuSettingsOverlayRenderer {
    data class Layout(
        val panelX: Float,
        val panelY: Float,
        val panelWidth: Float,
        val panelHeight: Float,
        val headerIconY: Float,
        val titleY: Float,
        val resetIconX: Float,
        val resetIconY: Float,
        val contentX: Float,
        val contentY: Float,
        val contentWidth: Float,
        val contentHeight: Float,
        val scissorX: Float,
        val scissorY: Float,
        val scissorWidth: Float,
        val scissorHeight: Float,
    )

    fun computeLayout(
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ): Layout {
        val panelX = viewportX + ModMenuSettingsOverlayStyle.PANEL_MARGIN
        val panelY = viewportY + ModMenuSettingsOverlayStyle.PANEL_MARGIN
        val panelWidth = viewportWidth - (ModMenuSettingsOverlayStyle.PANEL_MARGIN * 2f)
        val panelHeight = viewportHeight - (ModMenuSettingsOverlayStyle.PANEL_MARGIN * 2f)
        val headerIconY =
            panelY + (ModMenuSettingsOverlayStyle.HEADER_HEIGHT - ModMenuSettingsOverlayStyle.HEADER_ICON_SIZE) * 0.5f
        val titleY = headerIconY + 1f
        val contentX = panelX + ModMenuSettingsOverlayStyle.CONTENT_INSET_X
        val contentY = panelY + ModMenuSettingsOverlayStyle.HEADER_HEIGHT + ModMenuSettingsOverlayStyle.CONTENT_TOP_GAP
        val contentWidth = panelWidth - (ModMenuSettingsOverlayStyle.CONTENT_INSET_X * 2f)
        val contentHeight =
            panelHeight -
                ModMenuSettingsOverlayStyle.HEADER_HEIGHT -
                ModMenuSettingsOverlayStyle.CONTENT_TOP_GAP -
                ModMenuSettingsOverlayStyle.CONTENT_BOTTOM_GAP

        return Layout(
            panelX = panelX,
            panelY = panelY,
            panelWidth = panelWidth,
            panelHeight = panelHeight,
            headerIconY = headerIconY,
            titleY = titleY,
            resetIconX = panelX + panelWidth - ModMenuSettingsOverlayStyle.RESET_ICON_INSET_X,
            resetIconY = headerIconY - 0.5f,
            contentX = contentX,
            contentY = contentY,
            contentWidth = contentWidth,
            contentHeight = contentHeight,
            scissorX = panelX + ModMenuSettingsOverlayStyle.SCISSOR_INSET_X,
            scissorY = contentY,
            scissorWidth = panelWidth - (ModMenuSettingsOverlayStyle.SCISSOR_INSET_X * 2f),
            scissorHeight = contentHeight,
        )
    }

    fun drawChrome(
        nvg: NanoVGManager,
        palette: ColorPalette,
        layout: Layout,
        title: String,
        resetRotation: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val backHovered =
            isHeaderActionHovered(
                mouseX,
                mouseY,
                layout.panelX + ModMenuSettingsOverlayStyle.HEADER_BACK_X,
                layout.headerIconY,
            )
        val resetHovered = isHeaderActionHovered(mouseX, mouseY, layout.resetIconX, layout.resetIconY)

        nvg.drawShadow(layout.panelX, layout.panelY, layout.panelWidth, layout.panelHeight, 12f, 7)
        nvg.drawRoundedRect(
            layout.panelX,
            layout.panelY,
            layout.panelWidth,
            layout.panelHeight,
            ModMenuSettingsOverlayStyle.PANEL_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210),
        )
        nvg.drawRoundedRect(
            layout.panelX + 1f,
            layout.panelY + 1f,
            layout.panelWidth - 2f,
            layout.panelHeight - 2f,
            ModMenuSettingsOverlayStyle.PANEL_INNER_RADIUS,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230),
        )
        nvg.drawDivider(
            layout.panelX + 10f,
            layout.panelY + ModMenuSettingsOverlayStyle.HEADER_HEIGHT + 1f,
            layout.panelWidth - 20f,
            1f,
            1f,
            48f,
        )

        nvg.drawText(
            Lucide.CHEVRON_LEFT,
            layout.panelX + ModMenuSettingsOverlayStyle.HEADER_BACK_X,
            layout.headerIconY,
            if (backHovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
            13f,
            Fonts.LUCIDE,
        )
        nvg.drawText(
            title,
            layout.panelX + ModMenuSettingsOverlayStyle.HEADER_TITLE_X,
            layout.titleY,
            palette.getFontColor(ColorType.DARK),
            13f,
            Fonts.MEDIUM,
        )
        nvg.withState {
            nvg.rotateDegreesAt(layout.resetIconX + 6.5f, layout.resetIconY + 6.5f, resetRotation % 360f)
            nvg.drawText(
                Lucide.REFRESH_CW,
                layout.resetIconX,
                layout.resetIconY,
                if (resetHovered) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL),
                13f,
                Fonts.LUCIDE,
            )
        }
    }

    fun drawBackdrop(
        nvg: NanoVGManager,
        palette: ColorPalette,
        viewportX: Float,
        viewportY: Float,
        viewportWidth: Float,
        viewportHeight: Float,
    ) {
        nvg.drawRoundedRect(
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight,
            0f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 118),
        )
        nvg.drawVerticalGradientRect(
            viewportX,
            viewportY,
            viewportWidth,
            viewportHeight,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 22),
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 4),
        )
    }

    fun drawSettingsPanel(
        nvg: NanoVGManager,
        palette: ColorPalette,
        panel: SettingsPanel,
        layout: Layout,
        scroll: Scroll,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = layout.scissorX,
            y = layout.scissorY,
            width = layout.scissorWidth,
            height = layout.scissorHeight,
            layer = ModMenuClipCoordinator.ClipLayer.OVERLAY,
            tag = "settings_overlay_panel",
        ) {
            panel.draw(
                mouseX,
                mouseY,
                partialTicks,
                layout.contentX,
                layout.contentY,
                layout.contentWidth,
                layout.contentHeight,
                nvg,
                palette,
                scroll,
            )
        }
    }

    private fun isHeaderActionHovered(
        mouseX: Int,
        mouseY: Int,
        actionX: Float,
        actionY: Float,
    ): Boolean =
        mouseX >= actionX - 4f &&
            mouseX <= actionX + ModMenuSettingsOverlayStyle.HEADER_ACTION_HITBOX &&
            mouseY >= actionY - 3f &&
            mouseY <= actionY + ModMenuSettingsOverlayStyle.HEADER_ACTION_HITBOX

    fun configureSettingsPanel(
        panel: SettingsPanel,
        panelStyle: SettingsPanelStyle,
        layoutMode: SettingsPanel.LayoutMode?,
    ) {
        panel.setStyle(panelStyle)
        panel.setLayoutMode(layoutMode)
    }
}
