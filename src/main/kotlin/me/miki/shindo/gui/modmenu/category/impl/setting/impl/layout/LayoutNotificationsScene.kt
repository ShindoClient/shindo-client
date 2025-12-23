package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.layout.UILayoutManager
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils

class LayoutNotificationsScene(parent: SettingsCategory) :
    LayoutAreaScene(
        parent,
        UILayoutManager.Layouts.NOTIFICATIONS,
        TranslateText.SETTINGS_LAYOUT_SECTION_NOTIFICATION,
        TranslateText.SETTINGS_LAYOUT_NOTIFICATION_DESCRIPTION,
        LegacyIcon.BELL
    ) {

    override fun drawPreview(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170)
        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base)

        val cardWidth = 96f
        val cardHeight = 28f
        val padding = 14f
        val selected = InternalSettingsMod.getInstance().notificationCorner

        val leftX = x + padding
        val rightX = x + width - cardWidth - padding
        val topY = y + padding
        val bottomY = y + height - cardHeight - padding

        val slotColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 140)
        nvg.drawRoundedRect(leftX, topY, cardWidth, cardHeight, 8f, slotColor)
        nvg.drawRoundedRect(rightX, topY, cardWidth, cardHeight, 8f, slotColor)
        nvg.drawRoundedRect(leftX, bottomY, cardWidth, cardHeight, 8f, slotColor)
        nvg.drawRoundedRect(rightX, bottomY, cardWidth, cardHeight, 8f, slotColor)

        val cardX = when (selected) {
            InternalSettingsMod.NotificationCorner.TOP_LEFT,
            InternalSettingsMod.NotificationCorner.BOTTOM_LEFT -> leftX
            InternalSettingsMod.NotificationCorner.TOP_RIGHT,
            InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT -> rightX
        }

        val cardY = when (selected) {
            InternalSettingsMod.NotificationCorner.TOP_LEFT,
            InternalSettingsMod.NotificationCorner.TOP_RIGHT -> topY
            InternalSettingsMod.NotificationCorner.BOTTOM_LEFT,
            InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT -> bottomY
        }

        val accentStart = ColorUtils.applyAlpha(accent.color1, 200)
        val accentEnd = ColorUtils.applyAlpha(accent.color2, 200)
        val textColor = palette.getFontColor(ColorType.NORMAL)

        nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 7f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210))
        nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, cardHeight, 7f, accentStart, accentEnd)
        nvg.drawText(LegacyIcon.BELL, cardX + 7f, cardY + 6f, textColor, 11f, Fonts.LEGACYICON)
        nvg.drawRoundedRect(cardX + 22f, cardY + 7f, cardWidth - 28f, 6f, 2f, ColorUtils.applyAlpha(textColor, 200))
        nvg.drawRoundedRect(cardX + 22f, cardY + 16f, cardWidth - 38f, 5f, 2f, ColorUtils.applyAlpha(textColor, 160))
    }
}
