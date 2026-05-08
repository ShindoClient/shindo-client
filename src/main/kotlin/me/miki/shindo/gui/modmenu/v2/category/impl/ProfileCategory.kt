package me.miki.shindo.gui.modmenu.v2.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.navigation.ModMenuSlideTransitionCoordinator
import me.miki.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import me.miki.shindo.gui.modmenu.v2.style.ModMenuMotion
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.management.profile.*
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.chips.CategoryChipRenderer
import me.miki.shindo.ui.components.v2.chips.FilterChip
import me.miki.shindo.ui.components.v2.inputs.CompTextBox
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.IOUtils
import me.miki.shindo.utils.SearchUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class ProfileCategory(parent: GuiModMenu) : Category(parent, TranslateText.PROFILE, LegacyIcon.EDIT, true, true) {

    private val nameBox = CompTextBox()
    private val serverIpBox = CompTextBox()
    private val typeChips = ArrayList<FilterChip>()
    private val detailTransition = ModMenuSlideTransitionCoordinator()

    private var currentType = ProfileType.ALL
    private var currentIcon = ProfileIcon.COMMAND
    private var openProfile = false
    private var useCustomIcon = false
    private var selectedCustomIcon: File? = null
    private var gridStartY = 0f

    private val customIconHoverAnimation = SimpleAnimation()
    private val createAnimation = SimpleAnimation()

    private val importCodeBox = CompTextBox()
    private var showImportOverlay = false
    private val importOverlayAnimation = SimpleAnimation()
    private val importButtonAnimation = SimpleAnimation()

    override fun initGui() = resetState()

    override fun initCategory() {
        scroll.resetAll()
        resetState()
    }

    private fun resetState() {
        currentType = ProfileType.ALL
        currentIcon = ProfileIcon.COMMAND
        openProfile = false
        detailTransition.reset()
        useCustomIcon = false
        selectedCustomIcon = null
        gridStartY = 0f
        customIconHoverAnimation.setValue(0f)
        createAnimation.setValue(0f)
        showImportOverlay = false
        importOverlayAnimation.setValue(0f)
        importButtonAnimation.setValue(0f)
        importCodeBox.setText("")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val profileManager = instance.getProfileManager()
        val colorManager = instance.getColorManager()
        val accentColor = colorManager.getCurrentColor()
        val palette = colorManager.getPalette()

        detailTransition.update {
            nameBox.setText("")
            serverIpBox.setText("")
            setCanClose(true)
        }

        val visibleProfiles = collectVisibleProfiles(profileManager)
        val cardWidth = (getWidth() - CARD_HORIZONTAL_PADDING * 2 - CARD_COLUMN_GAP) / 2f
        val chipBlockBottom = drawTypeChips(nvg, palette, accentColor, mouseX, mouseY)
        val contentStartY = chipBlockBottom + 24f
        val viewportHeight = getHeight() - (contentStartY - getY()) - 28f
        gridStartY = contentStartY

        nvg.save()
        nvg.translate(detailTransition.getEnterTranslateX(ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE), 0f)

        handleScroll(mouseX, mouseY, contentStartY)
        drawProfileGrid(nvg, palette, visibleProfiles, cardWidth, contentStartY, mouseX, mouseY)
        drawFadeOverlays(nvg, palette)
        drawImportButton(nvg, palette, accentColor, mouseX, mouseY)

        nvg.restore()

        drawCreatePanel(nvg, palette, accentColor, mouseX, mouseY, partialTicks)
        drawImportOverlay(nvg, palette, accentColor, mouseX, mouseY, partialTicks)

        scroll.maxScroll = computeMaxScroll(visibleProfiles.size, viewportHeight)
    }

    private fun handleScroll(mouseX: Int, mouseY: Int, contentStartY: Float) {
        if (!detailTransition.isInteractive()) return
        if (MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), contentStartY - 6f,
                getWidth().toFloat(), getHeight() - (contentStartY - getY()) + 6f)) {
            scroll.onScroll()
            scroll.onAnimation()
        }
    }

    private fun drawProfileGrid(
        nvg: NanoVGManager,
        palette: ColorPalette,
        profiles: List<Profile>,
        cardWidth: Float,
        contentStartY: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        val scrollValue = scroll.getValue()
        ModMenuClipCoordinator.withClipTranslate(
            nvg = nvg,
            x = getX().toFloat(), y = contentStartY - 6f,
            width = getWidth().toFloat(), height = getHeight() - (contentStartY - getY()) + 6f,
            translateX = 0f, translateY = scrollValue,
            intersect = true
        ) {
            profiles.forEachIndexed { i, profile ->
                val cardX = getX() + CARD_HORIZONTAL_PADDING + (i % 2) * (cardWidth + CARD_COLUMN_GAP)
                val cardY = contentStartY + (i / 2) * (CARD_HEIGHT + CARD_ROW_GAP)
                if (cardY + scrollValue > getY() + getHeight() || cardY + scrollValue + CARD_HEIGHT < getY()) return@forEachIndexed

                val hovered = detailTransition.isInteractive() &&
                        MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT)
                drawCard(nvg, palette, profile, cardX, cardY, cardWidth, hovered)
            }
        }
    }

    private fun drawCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        profile: Profile,
        cardX: Float,
        cardY: Float,
        cardWidth: Float,
        hovered: Boolean
    ) {
        val base = applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 214 else 194)
        nvg.drawShadow(cardX, cardY, cardWidth, CARD_HEIGHT, 12f, 6)
        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12f, base)
        nvg.drawOutlineRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12f, 1f,
            applyAlpha(palette.getBackgroundColor(ColorType.MID), 210))

        if (profile.id == SENTINEL_ID) {
            drawCreateCard(nvg, palette, cardX, cardY, cardWidth)
            return
        }

        drawCardIcon(nvg, palette, profile, cardX, cardY)
        drawCardText(nvg, palette, profile, cardX, cardY, cardWidth)
        drawCardActions(nvg, palette, profile, cardX, cardY, cardWidth)
    }

    private fun drawCreateCard(nvg: NanoVGManager, palette: ColorPalette, cardX: Float, cardY: Float, cardWidth: Float) {
        nvg.drawCenteredText(LegacyIcon.PLUS, cardX + cardWidth / 2f, cardY + CARD_HEIGHT / 2f - 16f,
            palette.getFontColor(ColorType.DARK), 24f, Fonts.LEGACYICON)
        nvg.drawCenteredText(TranslateText.ADD_PROFILE.getText(), cardX + cardWidth / 2f, cardY + CARD_HEIGHT / 2f + 6f,
            palette.getFontColor(ColorType.DARK), 9.5f, Fonts.MEDIUM)
    }

    private fun drawCardIcon(nvg: NanoVGManager, palette: ColorPalette, profile: Profile, cardX: Float, cardY: Float) {
        val iconX = cardX + 16f
        val iconY = cardY + (CARD_HEIGHT - ICON_SIZE) / 2f
        when {
            profile.customIcon != null -> nvg.drawRoundedImage(profile.customIcon!!, iconX, iconY, ICON_SIZE, ICON_SIZE, 9f)
            profile.icon != null       -> nvg.drawRoundedImage(profile.icon.icon, iconX, iconY, ICON_SIZE, ICON_SIZE, 9f)
            else -> {
                nvg.drawRoundedRect(iconX, iconY, ICON_SIZE, ICON_SIZE, 9f,
                    applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200))
                nvg.drawCenteredText(LegacyIcon.PLUS, iconX + ICON_SIZE / 2f, iconY + ICON_SIZE / 2f,
                    palette.getFontColor(ColorType.DARK), 14f, Fonts.LEGACYICON)
            }
        }
    }

    private fun drawCardText(nvg: NanoVGManager, palette: ColorPalette, profile: Profile, cardX: Float, cardY: Float, cardWidth: Float) {
        val isDefault = profile.id == DEFAULT_ID
        val textX = cardX + 16f + ICON_SIZE + 14f
        val textWidth = cardWidth - (textX - cardX) - 24f

        val name = nvg.getLimitText(
            profile.name.ifEmpty { if (isDefault) "Default" else "Profile" },
            12f, Fonts.MEDIUM, textWidth
        )
        nvg.drawText(name, textX, cardY + 20f, palette.getFontColor(ColorType.DARK), 12f, Fonts.MEDIUM)

        val serverText = if (profile.serverIp.isNullOrEmpty()) {
            "${TranslateText.AUTO_LOAD.getText()}: ${TranslateText.NONE.getText()}"
        } else {
            "${TranslateText.SERVER_IP.getText()}: ${profile.serverIp}"
        }
        nvg.drawText(nvg.getLimitText(serverText, 8.5f, Fonts.REGULAR, textWidth),
            textX, cardY + 36f, applyAlpha(palette.getFontColor(ColorType.NORMAL), 220), 8.5f, Fonts.REGULAR)
    }

    private fun drawCardActions(
        nvg: NanoVGManager,
        palette: ColorPalette,
        profile: Profile,
        cardX: Float,
        cardY: Float,
        cardWidth: Float
    ) {
        val isDefault = profile.id == DEFAULT_ID
        val isActive = isActiveProfile(profile)
        val btnSize = 18f
        val gap = 8f
        val btnX = cardX + cardWidth - btnSize - 18f
        val checkY  = cardY + 10f
        val starY   = checkY  + btnSize + gap
        val deleteY = starY   + btnSize + gap
        val shareY  = deleteY + btnSize + gap

        // Check / active
        nvg.drawRoundedRect(btnX, checkY - 1f, btnSize, btnSize, 5f,
            applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190))
        if (isActive) {
            nvg.drawCenteredText(LegacyIcon.CHECK, btnX + btnSize / 2f - 0.5f, checkY + 3f,
                palette.getFontColor(ColorType.DARK), 10f, Fonts.LEGACYICON)
        }

        if (isDefault) return

        // Star
        profile.starAnimation.setAnimation(if (profile.type == ProfileType.FAVORITE) 1f else 0f, 16.0)
        nvg.drawRoundedRect(btnX, starY - 1f, btnSize, btnSize, 5f,
            applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190))
        nvg.drawCenteredText(LegacyIcon.STAR, btnX + btnSize / 2f - 0.5f, starY + 3f,
            palette.getFontColor(ColorType.NORMAL), 10f, Fonts.LEGACYICON)
        nvg.drawCenteredText(LegacyIcon.STAR_FILL, btnX + btnSize / 2f, starY + 3f,
            applyAlpha(palette.getMaterialYellow(), (profile.starAnimation.getValue() * 255).toInt()), 10f, Fonts.LEGACYICON)

        // Delete
        nvg.drawRoundedRect(btnX, deleteY - 1f, btnSize, btnSize, 5f,
            applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190))
        nvg.drawCenteredText(LegacyIcon.TRASH, btnX + btnSize / 2f - 0.5f, deleteY + 3f,
            palette.getMaterialRed(), 10f, Fonts.LEGACYICON)

        // Share — filled icon if already shared (code exists), outline if not
        val alreadyShared = !profile.shareCode.isNullOrBlank()
        nvg.drawRoundedRect(btnX, shareY - 1f, btnSize, btnSize, 5f,
            applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190))
        nvg.drawCenteredText(
            if (alreadyShared) LegacyIcon.CHECK else LegacyIcon.CONTENT_COPY,
            btnX + btnSize / 2f - 0.5f, shareY + 3f,
            if (alreadyShared) applyAlpha(palette.getFontColor(ColorType.DARK), 200)
            else               applyAlpha(palette.getFontColor(ColorType.NORMAL), 170),
            10f, Fonts.LEGACYICON
        )
    }

    private fun drawFadeOverlays(nvg: NanoVGManager, palette: ColorPalette) {
        val bg = palette.getBackgroundColor(ColorType.NORMAL)
        val transparent = Color(0, 0, 0, 0)
        val paddedX = getX() + CARD_HORIZONTAL_PADDING
        val paddedW = getWidth() - CARD_HORIZONTAL_PADDING * 2
        nvg.drawVerticalGradientRect(paddedX, getY() + 48f, paddedW, 14f, bg, transparent)
        nvg.drawVerticalGradientRect(paddedX, getY() + getHeight() - 32f, paddedW, 32f, transparent, bg)
    }

    private fun drawImportButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int
    ) {
        val btnW = 72f; val btnH = 20f
        val btnX = getX() + getWidth() - CARD_HORIZONTAL_PADDING - btnW
        val btnY = getY() + getHeight() - 26f

        val hovered = detailTransition.isInteractive() && !showImportOverlay &&
                MouseUtils.isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)
        importButtonAnimation.setAnimation(if (hovered) 1f else 0f, 12.0)
        val t = importButtonAnimation.getValue()

        val bgAlpha = if (showImportOverlay) 60 else (130 + t * 80).toInt()
        val bgColor = if (hovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.DARK)

        nvg.drawRoundedRect(btnX, btnY, btnW, btnH, 6f, applyAlpha(bgColor, bgAlpha))
        nvg.drawOutlineRoundedRect(btnX, btnY, btnW, btnH, 6f, 1f,
            applyAlpha(palette.getBackgroundColor(ColorType.MID), (160 + t * 60).toInt()))
        nvg.drawCenteredText(LegacyIcon.CONTENT_PASTE,btnX + 8.5f, btnY + btnH / 2f - 4f,
            applyAlpha(palette.getFontColor(if (hovered) ColorType.DARK else ColorType.NORMAL),
                if (showImportOverlay) 80 else 220),
            8.5f, Fonts.LEGACYICON)
        nvg.drawCenteredText(
            TranslateText.PROFILE_IMPORT.getText(),
            btnX + btnW / 2f, btnY + btnH / 2f - 4f,
            applyAlpha(palette.getFontColor(if (hovered) ColorType.DARK else ColorType.NORMAL),
                if (showImportOverlay) 80 else 220),
            8.5f, Fonts.MEDIUM
        )
    }

    private fun drawCreatePanel(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        nvg.save()
        nvg.translate(detailTransition.getSlideOffset(ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE), 0f)

        val panelX = getX() + 18f
        val panelY = getY() + 15f
        val panelW = getWidth() - 36f
        val panelH = getHeight() - 30f

        nvg.drawShadow(panelX, panelY, panelW, panelH, 12f, 7)
        nvg.drawRoundedRect(panelX, panelY, panelW, panelH, 12f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawOutlineRoundedRect(panelX, panelY, panelW, panelH, 12f, 1.1f,
            applyAlpha(palette.getBackgroundColor(ColorType.MID), 220))

        nvg.drawText(TranslateText.ADD_PROFILE.getText(), panelX + 24f, panelY + 20f,
            palette.getFontColor(ColorType.DARK), 14f, Fonts.SEMIBOLD)
        nvg.drawText(TranslateText.ICON.getText(), panelX + 24f, panelY + 48f,
            palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)

        drawIconSelector(nvg, palette, panelX, panelY, panelW, mouseX, mouseY)
        drawFormFields(nvg, palette, panelX, panelY, panelW, mouseX, mouseY, partialTicks)
        drawCreateButton(nvg, palette, accentColor, mouseX, mouseY)

        nvg.restore()
    }

    private fun drawIconSelector(
        nvg: NanoVGManager,
        palette: ColorPalette,
        panelX: Float,
        panelY: Float,
        panelW: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        val iconY = panelY + 66f
        val tileSize = 24f
        val gap = 12f
        var iconX = panelX + 24f

        for (icon in ProfileIcon.values()) {
            val selected = !useCustomIcon && currentIcon == icon
            val hovered = MouseUtils.isInside(mouseX, mouseY, iconX, iconY, tileSize, tileSize)
            icon.animation.setAnimation(if (selected) 1f else 0f, 12.0)
            nvg.drawRoundedImage(icon.icon, iconX + 1f, iconY + 1f, tileSize - 2f, tileSize - 2f, 7f)
            if (selected || hovered) {
                nvg.drawOutlineRoundedRect(iconX, iconY, tileSize, tileSize, 8f, 1.6f,
                    palette.getFontColor(ColorType.NORMAL))
            }
            iconX += tileSize + gap
        }

        val customX = panelX + panelW - tileSize - 24f
        val customHovered = MouseUtils.isInside(mouseX, mouseY, customX, iconY, tileSize, tileSize)
        customIconHoverAnimation.setAnimation(if (customHovered) 1f else 0f, 14.0)
        val hoverT = customIconHoverAnimation.getValue()

        nvg.drawRoundedRect(customX, iconY, tileSize, tileSize, 8f,
            applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), (210f + hoverT * 26f).toInt().coerceIn(0, 255)))
        if (selectedCustomIcon != null) {
            nvg.drawRoundedImage(selectedCustomIcon!!, customX + 1f, iconY + 1f, tileSize - 2f, tileSize - 2f, 7f)
        } else {
            nvg.drawCenteredText(LegacyIcon.PLUS, customX + tileSize / 2f, iconY + tileSize / 2f - 6f + hoverT * 0.25f,
                palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
        }
        if (customHovered) {
            nvg.drawOutlineRoundedRect(customX, iconY, tileSize, tileSize, 8f, 1.6f,
                palette.getFontColor(ColorType.NORMAL))
        }
    }

    private fun drawFormFields(
        nvg: NanoVGManager,
        palette: ColorPalette,
        panelX: Float,
        panelY: Float,
        panelW: Float,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        val fieldY = panelY + 130f
        val fieldW = (panelW - 48f) / 2f - 15f
        val col2X = panelX + 24f + fieldW + 24f

        nvg.drawText(TranslateText.NAME.getText(), panelX + 24f, fieldY,
            palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        nameBox.setPosition(panelX + 24f, fieldY + 20f, fieldW, 20f)
        nameBox.setDefaultText(TranslateText.NAME.getText())
        nameBox.draw(mouseX, mouseY, partialTicks)

        nvg.drawText(TranslateText.SERVER_IP.getText(), col2X, fieldY,
            palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        serverIpBox.setPosition(col2X, fieldY + 20f, fieldW, 20f)
        serverIpBox.setDefaultText(TranslateText.SERVER_IP.getText())
        serverIpBox.draw(mouseX, mouseY, partialTicks)
    }

    private fun drawCreateButton(nvg: NanoVGManager, palette: ColorPalette, accentColor: AccentColor, mouseX: Int, mouseY: Int) {
        val btnX = getX() + getWidth() - 124f
        val btnY = getY() + getHeight() - 44f
        val hovered = MouseUtils.isInside(mouseX, mouseY, btnX, btnY, 100f, 21f)
        createAnimation.setAnimation(if (hovered) 1f else 0f, 12.0)
        val bgColor = if (hovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.NORMAL)
        nvg.drawRoundedRect(btnX, btnY, 100f, 21f, 6f,
            applyAlpha(bgColor, (if (hovered) 210 else 150) + (createAnimation.getValue() * 20).toInt())
        )
        nvg.drawCenteredText(TranslateText.CREATE.getText(), btnX + 50f, btnY + 6.5f,
            palette.getFontColor(ColorType.DARK), 10f, Fonts.REGULAR)
    }


    private fun drawImportOverlay(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        importOverlayAnimation.setAnimation(if (showImportOverlay) 1f else 0f, 14.0)
        val t = importOverlayAnimation.getValue()
        if (t < 0.01f) return

        val overlayW = 220f
        val overlayH = 78f
        val margin = 14f
        val overlayX = getX() + getWidth() - CARD_HORIZONTAL_PADDING - overlayW
        val anchorY = getY() + getHeight() - 32f          // aligns with import button
        val overlayY = anchorY - overlayH - 6f + (1f - t) * 8f   // slides up

        nvg.save()

        nvg.drawShadow(overlayX, overlayY, overlayW, overlayH, 10f, 7)
        nvg.drawRoundedRect(overlayX, overlayY, overlayW, overlayH, 10f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawOutlineRoundedRect(overlayX, overlayY, overlayW, overlayH, 10f, 1f,
            applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))

        // Title
        nvg.drawText(
            "${LegacyIcon.DOWNLOAD}  ${TranslateText.PROFILE_IMPORT.getText()}",
            overlayX + margin, overlayY + 14f,
            palette.getFontColor(ColorType.DARK), 10f, Fonts.SEMIBOLD
        )

        // Close (×)
        val closeX = overlayX + overlayW - margin - 8f
        val closeHovered = MouseUtils.isInside(mouseX, mouseY, closeX - 5f, overlayY + 7f, 16f, 16f)
        nvg.drawCenteredText(LegacyIcon.X, closeX, overlayY + 13f,
            applyAlpha(palette.getFontColor(ColorType.NORMAL), if (closeHovered) 230 else 130),
            9f, Fonts.LEGACYICON)

        // Code input + confirm button on the same row
        val confirmW = 36f
        val inputW = overlayW - margin * 2 - confirmW - 6f
        importCodeBox.setPosition(overlayX + margin, overlayY + 38f, inputW, 20f)
        importCodeBox.setDefaultText("XXXXXXXXXXXX")
        importCodeBox.draw(mouseX, mouseY, partialTicks)

        val confirmX = overlayX + margin + inputW + 6f
        val confirmY = overlayY + 38f
        val code = importCodeBox.getText().trim().toUpperCase(Locale.ROOT)
        val canConfirm = code.length == SHARE_CODE_LENGTH
        val confirmHovered = canConfirm && MouseUtils.isInside(mouseX, mouseY, confirmX, confirmY, confirmW, 20f)

        nvg.drawRoundedRect(confirmX, confirmY, confirmW, 20f, 6f,
            applyAlpha(
                if (confirmHovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.NORMAL),
                when { confirmHovered -> 210; canConfirm -> 170; else -> 70 }
            )
        )
        nvg.drawCenteredText(LegacyIcon.CHECK, confirmX + confirmW / 2f, confirmY + 5f,
            applyAlpha(palette.getFontColor(ColorType.DARK), if (canConfirm) 220 else 90),
            10f, Fonts.LEGACYICON)

        nvg.restore()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        // Import overlay consumes clicks first when visible
        if (importOverlayAnimation.getValue() > 0.01f) {
            if (handleImportOverlayClick(mouseX, mouseY, mouseButton)) return
        }

        if (openProfile && detailTransition.isActive()) {
            handleCreatePanelClick(mouseX, mouseY, mouseButton)
        } else if (detailTransition.isInteractive()) {
            handleListClick(mouseX, mouseY, mouseButton)
        }

        if (mouseButton == 3) closeProfilePanel(clearIconSelection = true)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (showImportOverlay) {
            importCodeBox.keyTyped(typedChar, keyCode)
            if (keyCode == Keyboard.KEY_ESCAPE) closeImportOverlay()
            return
        }

        if (openProfile && detailTransition.isActive()) {
            nameBox.keyTyped(typedChar, keyCode)
            serverIpBox.keyTyped(typedChar, keyCode)
            if (keyCode == Keyboard.KEY_ESCAPE) closeProfilePanel(clearIconSelection = true)
        } else if (detailTransition.isInteractive()) {
            val isNavKey = keyCode == 0xD0 || keyCode == 0xC8 || keyCode == Keyboard.KEY_ESCAPE
            if (!isNavKey) getSearchBox().setFocused(true)
        }
    }

    /** Returns true if the click was fully consumed by the overlay. */
    private fun handleImportOverlayClick(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton != 0) return false

        val overlayW = 220f
        val overlayH = 78f
        val margin = 14f
        val overlayX = getX() + getWidth() - CARD_HORIZONTAL_PADDING - overlayW
        val anchorY = getY() + getHeight() - 32f
        val overlayY = anchorY - overlayH - 6f

        // Click outside → close and let click fall through
        if (!MouseUtils.isInside(mouseX, mouseY, overlayX - 4f, overlayY - 4f, overlayW + 8f, overlayH + 8f)) {
            closeImportOverlay()
            return false
        }

        // Close button
        val closeX = overlayX + overlayW - margin - 8f
        if (MouseUtils.isInside(mouseX, mouseY, closeX - 5f, overlayY + 7f, 16f, 16f)) {
            closeImportOverlay()
            return true
        }

        // Confirm button
        val confirmW = 36f
        val inputW = overlayW - margin * 2 - confirmW - 6f
        val confirmX = overlayX + margin + inputW + 6f
        if (MouseUtils.isInside(mouseX, mouseY, confirmX, overlayY + 38f, confirmW, 20f)) {
            onImportConfirm()
            return true
        }

        importCodeBox.mouseClicked(mouseX, mouseY, mouseButton)
        return true
    }

    private fun onImportConfirm() {
        val instance = Shindo.getInstance()
        val code = importCodeBox.getText().trim().toUpperCase(Locale.ROOT)
        if (code.length != SHARE_CODE_LENGTH) return

        instance.getProfileShareManager().requestFetch(code) { result ->
            when (result) {
                is ProfileShareManager.FetchResult.Success -> {
                    instance.getNotificationManager().post(
                        TranslateText.PROFILE_NOTIFICATION_TITLE,
                        TranslateText.PROFILE_IMPORT_SUCCESS,
                        NotificationType.SUCCESS
                    )
                    importCodeBox.setText("")
                    closeImportOverlay()
                }
                is ProfileShareManager.FetchResult.Error -> {
                    instance.getNotificationManager().post(
                        TranslateText.PROFILE_NOTIFICATION_TITLE,
                        TranslateText.PROFILE_IMPORT_FAILED,
                        NotificationType.ERROR
                    )
                }
            }
        }
    }

    private fun handleCreatePanelClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        val panelX = getX() + 18f
        val panelY = getY() + 15f
        val panelW = getWidth() - 36f
        val panelH = getHeight() - 30f
        val tileSize = 24f
        val iconY = panelY + 66f

        var iconX = panelX + 24f
        for (icon in ProfileIcon.values()) {
            if (MouseUtils.isInside(mouseX, mouseY, iconX, iconY, tileSize, tileSize)) {
                currentIcon = icon; useCustomIcon = false
            }
            iconX += tileSize + 12f
        }

        val customX = panelX + panelW - tileSize - 24f
        if (MouseUtils.isInside(mouseX, mouseY, customX, iconY, tileSize, tileSize)) {
            if (selectedCustomIcon != null && !useCustomIcon) useCustomIcon = true
            else openCustomIconPicker()
        }

        nameBox.mouseClicked(mouseX, mouseY, mouseButton)
        serverIpBox.mouseClicked(mouseX, mouseY, mouseButton)

        if (MouseUtils.isInside(mouseX, mouseY, getX() + getWidth() - 124f, getY() + getHeight() - 44f, 100f, 21f)) {
            onCreateProfile()
        }

        if (!MouseUtils.isInside(mouseX, mouseY, panelX - 6f, panelY - 6f, panelW + 12f, panelH + 12f)) {
            closeProfilePanel(clearIconSelection = true)
        }
    }

    private fun onCreateProfile() {
        if (nameBox.getText().isEmpty()) return
        val instance = Shindo.getInstance()
        val profileFile = File(instance.getFileManager().profileDir, "${nameBox.getText()}.json")
        instance.getProfileManager().save(profileFile, serverIpBox.getText().ifEmpty { "" },
            ProfileType.ALL, currentIcon, if (useCustomIcon) selectedCustomIcon else null)
        instance.getProfileManager().loadProfiles(false)
        closeProfilePanel(clearIconSelection = true)
        currentIcon = ProfileIcon.COMMAND
    }

    private fun handleListClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            val btnW = 72f; val btnH = 20f
            val btnX = getX() + getWidth() - CARD_HORIZONTAL_PADDING - btnW
            val btnY = getY() + getHeight() - 26f
            if (MouseUtils.isInside(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                openImportOverlay(); return
            }
            for (chip in typeChips) {
                if (chip.contains(mouseX, mouseY)) { chip.click(); return }
            }
        }

        val instance = Shindo.getInstance()
        val profileManager = instance.getProfileManager()
        val scrollValue = scroll.getValue()
        val contentStartY = if (gridStartY > 0f) gridStartY else getY() + 56f
        val cardWidth = (getWidth() - CARD_HORIZONTAL_PADDING * 2 - CARD_COLUMN_GAP) / 2f
        val visibleProfiles = collectVisibleProfiles(profileManager)

        for (i in visibleProfiles.indices) {
            val profile = visibleProfiles[i]
            val cardX = getX() + CARD_HORIZONTAL_PADDING + (i % 2) * (cardWidth + CARD_COLUMN_GAP)
            val cardY = contentStartY + (i / 2) * (CARD_HEIGHT + CARD_ROW_GAP) + scrollValue

            if (!MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, CARD_HEIGHT)) continue
            if (mouseButton != 0) continue
            if (profile.id == SENTINEL_ID) { openProfilePanel(); return }

            handleCardClick(instance, profileManager, profile, mouseX, mouseY, cardX, cardY, cardWidth)
            return
        }
    }

    private fun handleCardClick(
        instance: Shindo,
        profileManager: ProfileManager,
        profile: Profile,
        mouseX: Int,
        mouseY: Int,
        cardX: Float,
        cardY: Float,
        cardWidth: Float
    ) {
        val isDefault = profile.id == DEFAULT_ID
        val btnSize = 18f
        val gap = 8f
        val btnX = cardX + cardWidth - btnSize - 18f
        val checkY  = cardY + 10f
        val starY   = checkY  + btnSize + gap
        val deleteY = starY   + btnSize + gap
        val shareY  = deleteY + btnSize + gap

        if (!isDefault && MouseUtils.isInside(mouseX, mouseY, btnX - 0.5f, starY + 3f, btnSize, btnSize)) {
            profile.type = if (profile.type == ProfileType.FAVORITE) ProfileType.ALL else ProfileType.FAVORITE
            profileManager.save(profile.jsonFile!!, profile.serverIp, profile.type, profile.icon, profile.customIcon)
            return
        }

        if (!isDefault && MouseUtils.isInside(mouseX, mouseY, btnX - 0.5f, deleteY + 3f, btnSize, btnSize)) {
            profile.shareCode?.takeIf { it.isNotBlank() }?.let { instance.getProfileShareManager().requestUnshare(it) }
            profileManager.delete(profile)
            profileManager.loadProfiles(false)
            return
        }

        if (!isDefault && MouseUtils.isInside(mouseX, mouseY, btnX - 0.5f, shareY + 3f, btnSize, btnSize)) {
            onShareButtonClicked(instance, profileManager, profile)
            return
        }

        val success = profileManager.load(profile.jsonFile)
        instance.getNotificationManager().post(
            TranslateText.PROFILE_NOTIFICATION_TITLE,
            if (success) TranslateText.PROFILE_LOADED else TranslateText.PROFILE_FAILED,
            if (success) NotificationType.SUCCESS else NotificationType.ERROR
        )
    }

    private fun onShareButtonClicked(instance: Shindo, profileManager: ProfileManager, profile: Profile) {
        val existingCode = profile.shareCode
        if (!existingCode.isNullOrBlank()) {
            IOUtils.copyStringToClipboard(existingCode)
            instance.getNotificationManager().post(
                TranslateText.PROFILE_NOTIFICATION_TITLE,
                "${TranslateText.PROFILE_SHARE_SUCCESS.getText()}: $existingCode",
                NotificationType.SUCCESS
            )
            return
        }

        instance.getProfileShareManager().requestShare(profile) { result ->
            when (result) {
                is ProfileShareManager.ShareResult.Success -> {
                    profileManager.updateShareCode(profile, result.code)
                    IOUtils.copyStringToClipboard(result.code)
                    instance.getNotificationManager().post(
                        TranslateText.PROFILE_NOTIFICATION_TITLE,
                        "${TranslateText.PROFILE_SHARE_SUCCESS.getText()}: ${result.code}",
                        NotificationType.SUCCESS
                    )
                }
                is ProfileShareManager.ShareResult.Error -> {
                    instance.getNotificationManager().post(
                        TranslateText.PROFILE_NOTIFICATION_TITLE,
                        TranslateText.PROFILE_SHARE_FAILED,
                        NotificationType.ERROR
                    )
                }
            }
        }
    }

    private fun openProfilePanel() {
        openProfile = true
        detailTransition.open()
        setCanClose(false)
        closeImportOverlay()
    }

    private fun closeProfilePanel(clearIconSelection: Boolean) {
        openProfile = false
        detailTransition.close()
        if (clearIconSelection) { selectedCustomIcon = null; useCustomIcon = false }
    }

    private fun openImportOverlay() {
        showImportOverlay = true
        importCodeBox.setText("")
    }

    private fun closeImportOverlay() {
        showImportOverlay = false
    }

    private fun drawTypeChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        mouseX: Int,
        mouseY: Int
    ): Float {
        typeChips.clear()
        val startX = getX() + CARD_HORIZONTAL_PADDING
        val maxX = getX() + getWidth() - CARD_HORIZONTAL_PADDING
        var curX = startX; var curY = getY() + 16f
        var blockBottom = curY + CategoryChipRenderer.CHIP_HEIGHT

        for (type in ProfileType.values()) {
            val label = type.name
            val chipW = CategoryChipRenderer.computeWidth(nvg, label, null)
            if (curX + chipW > maxX) {
                curX = startX; curY += CategoryChipRenderer.CHIP_HEIGHT + CHIP_GAP
                blockBottom = curY + CategoryChipRenderer.CHIP_HEIGHT
            }

            val active = type == currentType
            val hovered = detailTransition.isInteractive() &&
                    MouseUtils.isInside(mouseX, mouseY, curX, curY, chipW, CategoryChipRenderer.CHIP_HEIGHT)
            CategoryChipRenderer.drawChip(nvg, palette, accentColor, curX, curY, chipW, label, null, active, hovered)

            val chip = FilterChip(Runnable { if (currentType != type) { currentType = type; scroll.resetAll() } })
            chip.setBounds(curX, curY, chipW, CategoryChipRenderer.CHIP_HEIGHT)
            typeChips.add(chip)
            curX += chipW + CHIP_GAP
        }

        return blockBottom
    }

    private fun isActiveProfile(profile: Profile): Boolean {
        val active = Shindo.getInstance().getProfileManager().activeProfile ?: return false
        if (active == profile) return true
        val af = active.jsonFile ?: return false
        val pf = profile.jsonFile ?: return false
        return try { af.canonicalPath == pf.canonicalPath } catch (e: Exception) { af.absolutePath == pf.absolutePath }
    }

    private fun collectVisibleProfiles(profileManager: ProfileManager): List<Profile> =
        profileManager.profiles.filter { it.id == SENTINEL_ID || !filter(it) }

    private fun filter(profile: Profile): Boolean {
        if (currentType == ProfileType.FAVORITE && profile.type != ProfileType.FAVORITE) return true
        val query = getSearchBox().getText()
        return query.isNotEmpty() && !SearchUtils.isSimilar(profile.name, query)
    }

    private fun computeMaxScroll(profileCount: Int, viewportHeight: Float): Float {
        val rows = ceil(profileCount / 2.0).toFloat()
        val contentH = rows * CARD_HEIGHT + max(0f, rows - 1) * CARD_ROW_GAP
        return max(0f, contentH - viewportHeight)
    }

    private fun openCustomIconPicker() {
        TaskExecutor.runAsync(ThreadPoolType.IO) {
            val fileManager = Shindo.getInstance().getFileManager()
            val file = FileUtils.selectImageFile() ?: return@runAsync
            val iconDir = fileManager.profileIconDir
            if (!iconDir.exists() || !file.exists() || FileUtils.getExtension(file) != "png") return@runAsync
            try {
                val dest = File(iconDir, file.name)
                FileUtils.copyFile(file, dest)
                val prev = selectedCustomIcon
                selectedCustomIcon = dest; useCustomIcon = true
                prev?.takeIf { it.exists() }?.delete()
            } catch (e: IOException) {
                ShindoLogger.error("Failed to copy custom profile icon", e)
            }
        }
    }

    // =========================================================================
    // Constants
    // =========================================================================

    private companion object {
        const val SENTINEL_ID = 999
        const val DEFAULT_ID = -1
        const val SHARE_CODE_LENGTH = 12
        const val CARD_HORIZONTAL_PADDING = 18f
        const val CARD_COLUMN_GAP = 18f
        const val CARD_ROW_GAP = 14f
        const val CARD_HEIGHT = 112f   // bumped from 94 to fit 4 action buttons (check/star/delete/share)
        const val ICON_SIZE = 44f
        const val CHIP_GAP = 8f
    }
}