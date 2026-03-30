package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.navigation.ModMenuDetailLayerTransitionCoordinator
import me.miki.shindo.gui.modmenu.render.ModMenuClipCoordinator
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.management.profile.Profile
import me.miki.shindo.management.profile.ProfileIcon
import me.miki.shindo.management.profile.ProfileManager
import me.miki.shindo.management.profile.ProfileShareManager
import me.miki.shindo.management.profile.ProfileType
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.chips.CategoryChipRenderer
import me.miki.shindo.ui.comp.chips.FilterChip
import me.miki.shindo.ui.comp.inputs.CompTextBox
import me.miki.shindo.utils.ColorUtils
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


class ProfileCategory(parent: GuiModMenu) : Category(parent, TranslateText.PROFILE, LegacyIcon.EDIT, true, true) {

    private val nameBox = CompTextBox()
    private val serverIpBox = CompTextBox()
    private val typeChips = ArrayList<FilterChip>()
    private val detailTransition = ModMenuDetailLayerTransitionCoordinator()
    private var currentType = ProfileType.ALL
    private var openProfile = false
    private var currentIcon = ProfileIcon.COMMAND
    private var useCustomIcon = false
    private var selectedCustomIcon: File? = null
    private var gridStartY = 0f
    //private var importButtonX = 0f
    //private var importButtonY = 0f
    //private var importButtonW = 0f
    //private var importButtonH = 0f
    private val customIconHoverAnimation = SimpleAnimation()
    private val createAnimation = SimpleAnimation()

    override fun initGui() {
        currentType = ProfileType.ALL
        currentIcon = ProfileIcon.COMMAND
        openProfile = false
        detailTransition.reset()
        useCustomIcon = false
        selectedCustomIcon = null
        gridStartY = 0f
        customIconHoverAnimation.value = 0f
        createAnimation.value = 0f
    }

    override fun initCategory() {
        scroll.resetAll()
        openProfile = false
        detailTransition.reset()
        useCustomIcon = false
        selectedCustomIcon = null
        gridStartY = 0f
        customIconHoverAnimation.value = 0f
        createAnimation.value = 0f
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val profileManager = instance.profileManager
        val activeProfile = profileManager.activeProfile
        val colorManager = instance.colorManager
        val accentColor = colorManager.getCurrentColor()
        val palette = colorManager.getPalette()

        detailTransition.update {
            nameBox.setText("")
            serverIpBox.setText("")
            setCanClose(true)
        }

        val visibleProfiles = collectVisibleProfiles(profileManager)
        val scrollValue = scroll.getValue()

        nvg.save()
        nvg.translate(detailTransition.getListTranslateX(), 0f)

        val chipBlockBottom = drawTypeChips(nvg, palette, accentColor, mouseX, mouseY)
        val contentStartY = chipBlockBottom + 24f
        gridStartY = contentStartY
        val cardWidth = ((getWidth() - (CARD_HORIZONTAL_PADDING * 2) - CARD_COLUMN_GAP) / 2f)
        val viewportHeight = getHeight() - (contentStartY - getY()) - 28f

        if (detailTransition.isListInteractive() && MouseUtils.isInside(
                mouseX,
                mouseY,
                getX().toFloat(),
                contentStartY - 6f,
                getWidth().toFloat(),
                getHeight() - (contentStartY - getY()) + 6f
            )
        ) {
            scroll.onScroll()
            scroll.onAnimation()
        }

        ModMenuClipCoordinator.withClipTranslate(
            nvg = nvg,
            x = getX().toFloat(),
            y = contentStartY - 6f,
            width = getWidth().toFloat(),
            height = getHeight() - (contentStartY - getY()) + 6f,
            translateX = 0f,
            translateY = scrollValue,
            intersect = true
        ) {
            for (i in visibleProfiles.indices) {
                val profile = visibleProfiles[i]
                val isCreateCard = profile.id == 999
                val isDefault = profile.id == -1
                var isActive = activeProfile != null && activeProfile == profile
                if (!isActive && activeProfile != null && activeProfile.jsonFile != null && profile.jsonFile != null) {
                    isActive = try {
                        activeProfile.jsonFile.canonicalPath == profile.jsonFile.canonicalPath
                    } catch (e: Exception) {
                        activeProfile.jsonFile.absolutePath == profile.jsonFile.absolutePath
                    }
                }

                val column = i % 2
                val row = i / 2

                val cardX = getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP)
                val cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP)

                if (cardY + scrollValue > getY() + getHeight() || cardY + scrollValue + CARD_HEIGHT < getY()) {
                    continue
                }

                val hovered =
                    detailTransition.isListInteractive() && MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        cardX,
                        cardY + scrollValue,
                        cardWidth,
                        CARD_HEIGHT
                    )

                val base = applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 214 else 194)

                nvg.drawShadow(cardX, cardY, cardWidth, CARD_HEIGHT, 12f, 6)
                nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 12f, base)
                nvg.drawOutlineRoundedRect(
                    cardX,
                    cardY,
                    cardWidth,
                    CARD_HEIGHT,
                    12f,
                    1f,
                    applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
                )


                if (isCreateCard) {
                    nvg.drawCenteredText(
                        LegacyIcon.PLUS,
                        cardX + cardWidth / 2f,
                        cardY + CARD_HEIGHT / 2f - 16f,
                        palette.getFontColor(ColorType.DARK),
                        24f,
                        Fonts.LEGACYICON
                    )
                    nvg.drawCenteredText(
                        TranslateText.ADD_PROFILE.getText(),
                        cardX + cardWidth / 2f,
                        cardY + CARD_HEIGHT / 2f + 6f,
                        palette.getFontColor(ColorType.DARK),
                        9.5f,
                        Fonts.MEDIUM
                    )
                    continue
                }

                val iconX = cardX + 16f
                val iconY = cardY + (CARD_HEIGHT - ICON_SIZE) / 2f

                if (profile.customIcon != null) {
                    nvg.drawRoundedImage(profile.customIcon!!, iconX, iconY, ICON_SIZE, ICON_SIZE, 9f)
                } else if (profile.icon != null) {
                    nvg.drawRoundedImage(profile.icon.icon, iconX, iconY, ICON_SIZE, ICON_SIZE, 9f)
                } else {
                    nvg.drawRoundedRect(
                        iconX,
                        iconY,
                        ICON_SIZE,
                        ICON_SIZE,
                        9f,
                        applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200)
                    )
                    nvg.drawCenteredText(
                        LegacyIcon.PLUS,
                        iconX + ICON_SIZE / 2f,
                        iconY + ICON_SIZE / 2f,
                        palette.getFontColor(ColorType.DARK),
                        14f,
                        Fonts.LEGACYICON
                    )
                }

                val textX = iconX + ICON_SIZE + 14f
                val textWidth = cardWidth - (textX - cardX) - 24f
                var profileName = profile.name.ifEmpty { if (isDefault) "Default" else "Profile" }
                profileName = nvg.getLimitText(profileName, 12f, Fonts.MEDIUM, textWidth)
                nvg.drawText(profileName, textX, cardY + 20f, palette.getFontColor(ColorType.DARK), 12f, Fonts.MEDIUM)

                var serverInfo = if (profile.serverIp == null || profile.serverIp!!.isEmpty()) {
                    TranslateText.AUTO_LOAD.getText() + ": " + TranslateText.NONE.getText()
                } else {
                    TranslateText.SERVER_IP.getText() + ": " + profile.serverIp
                }

                serverInfo = nvg.getLimitText(serverInfo, 8.5f, Fonts.REGULAR, textWidth)
                nvg.drawText(
                    serverInfo,
                    textX,
                    cardY + 36f,
                    applyAlpha(palette.getFontColor(ColorType.NORMAL), 220),
                    8.5f,
                    Fonts.REGULAR
                )

                if (!isDefault) {
                    val starSize = 18f
                    val startX = cardX + cardWidth - starSize - 18f
                    val startY = cardY + 10f

                    profile.starAnimation.setAnimation(if (profile.type == ProfileType.FAVORITE) 1.0f else 0.0f, 16.0)

                    nvg.drawRoundedRect(
                        startX,
                        startY - 1f,
                        starSize,
                        starSize,
                        5f,
                        applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190)
                    )

                    val starY = startY + starSize + 10f
                    nvg.drawRoundedRect(
                        startX,
                        starY - 1f,
                        starSize,
                        starSize,
                        5f,
                        applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190)
                    )
                    nvg.drawCenteredText(
                        LegacyIcon.STAR,
                        startX + starSize / 2f - 0.5f,
                        starY + 3f,
                        palette.getFontColor(ColorType.NORMAL),
                        10f,
                        Fonts.LEGACYICON
                    )
                    nvg.drawCenteredText(
                        LegacyIcon.STAR_FILL,
                        startX + starSize / 2f,
                        starY + 3f,
                        applyAlpha(palette.getMaterialYellow(), (profile.starAnimation.value * 255).toInt()),
                        10f,
                        Fonts.LEGACYICON
                    )

                    val deleteY = starY + starSize + 10f
                    nvg.drawRoundedRect(
                        startX,
                        deleteY - 1f,
                        starSize,
                        starSize,
                        5f,
                        applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190)
                    )
                    nvg.drawCenteredText(
                        LegacyIcon.TRASH,
                        startX + starSize / 2f - 0.5f,
                        deleteY + 3f,
                        palette.getMaterialRed(),
                        10f,
                        Fonts.LEGACYICON
                    )
                } else {
                    val checkSize = 18f
                    val checkX = cardX + cardWidth - checkSize - 18f
                    val checkY = cardY + 10f

                    nvg.drawRoundedRect(
                        checkX,
                        checkY - 1f,
                        checkSize,
                        checkSize,
                        5f,
                        applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190)
                    )
                }

                if (isActive) {
                    val checkSize = 18f
                    val checkX = cardX + cardWidth - checkSize - 18f
                    val checkY = cardY + 10f

                    nvg.drawCenteredText(
                        LegacyIcon.CHECK,
                        checkX + checkSize / 2f - 0.5f,
                        checkY + 3f,
                        palette.getFontColor(ColorType.DARK),
                        10f,
                        Fonts.LEGACYICON
                    )
                }
            }
        }

        nvg.restore()
        nvg.drawVerticalGradientRect(
            getX() + CARD_HORIZONTAL_PADDING,
            getY() + 48f,
            getWidth() - (CARD_HORIZONTAL_PADDING * 2),
            14f,
            palette.getBackgroundColor(ColorType.NORMAL),
            Color(0, 0, 0, 0)
        )
        nvg.drawVerticalGradientRect(
            getX() + CARD_HORIZONTAL_PADDING,
            getY() + getHeight() - 16f,
            getWidth() - (CARD_HORIZONTAL_PADDING * 2),
            16f,
            Color(0, 0, 0, 0),
            palette.getBackgroundColor(ColorType.NORMAL)
        )
        nvg.save()
        nvg.translate(detailTransition.getDetailsTranslateX(), 0f)

        val panelX = getX() + 18f
        val panelY = getY() + 15f
        val panelWidth = getWidth() - 36f
        val panelHeight = getHeight() - 30f

        nvg.drawShadow(panelX, panelY, panelWidth, panelHeight, 12f, 7)
        nvg.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 12f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawOutlineRoundedRect(
            panelX,
            panelY,
            panelWidth,
            panelHeight,
            12f,
            1.1f,
            applyAlpha(palette.getBackgroundColor(ColorType.MID), 220)
        )

        nvg.drawText(
            TranslateText.ADD_PROFILE.getText(),
            panelX + 24f,
            panelY + 20f,
            palette.getFontColor(ColorType.DARK),
            14f,
            Fonts.SEMIBOLD
        )
        nvg.drawText(
            TranslateText.ICON.getText(),
            panelX + 24f,
            panelY + 48f,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.MEDIUM
        )

        var iconSelectorX = panelX + 24f
        val iconSelectorY = panelY + 66f
        val iconSelectorGap = 12f
        val iconTileSize = 24f

        for (icon in ProfileIcon.values()) {
            val selected = !useCustomIcon && currentIcon == icon
            val hovered = MouseUtils.isInside(mouseX, mouseY, iconSelectorX, iconSelectorY, iconTileSize, iconTileSize)
            icon.animation.setAnimation(if (selected) 1.0f else 0.0f, 12.0)
            nvg.drawRoundedImage(
                icon.icon,
                iconSelectorX + 1f,
                iconSelectorY + 1f,
                iconTileSize - 2f,
                iconTileSize - 2f,
                7f
            )

            if (selected || hovered) {
                nvg.drawOutlineRoundedRect(
                    iconSelectorX,
                    iconSelectorY,
                    iconTileSize,
                    iconTileSize,
                    8f,
                    1.6f,
                    palette.getFontColor(ColorType.NORMAL)
                    )
            }

            iconSelectorX += iconTileSize + iconSelectorGap
        }

        val customTileX = panelX + panelWidth - iconTileSize - 24f
        val customTileY = panelY + 66f
        val customHovered = MouseUtils.isInside(mouseX, mouseY, customTileX, customTileY, iconTileSize, iconTileSize)
        customIconHoverAnimation.setAnimation(if (customHovered) 1.0f else 0.0f, 14.0)

        val customHover = customIconHoverAnimation.value

        val customBase = applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), (210f + customHover * 26f).toInt().coerceIn(0, 255))
        nvg.drawRoundedRect(customTileX, customTileY, 24f, 24f, 8f, customBase)
        if (selectedCustomIcon != null) {
            nvg.drawRoundedImage(
                selectedCustomIcon!!,
                customTileX + 1f,
                customTileY + 1f,
                iconTileSize - 2f,
                iconTileSize - 2f,
                7f
            )
        } else {
            nvg.drawCenteredText(
                LegacyIcon.PLUS,
                customTileX + iconTileSize / 2f,
                customTileY + iconTileSize / 2f - 6f + customHover * 0.25f,
                palette.getFontColor(ColorType.DARK),
                12f,
                Fonts.LEGACYICON
            )
        }

        if (customHovered) {
            nvg.drawOutlineRoundedRect(
                customTileX,
                customTileY,
                iconTileSize,
                iconTileSize,
                8f,
                1.6f,
                palette.getFontColor(
                    ColorType.NORMAL)
            )
        }



        val fieldStartY = panelY + 130f
        val fieldWidth = (panelWidth - 48f) / 2f - 15f

        nvg.drawText(
            TranslateText.NAME.getText(),
            panelX + 24f,
            fieldStartY,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.MEDIUM
        )
        nameBox.setPosition(panelX + 24f, fieldStartY + 20f, fieldWidth, 20f)
        nameBox.setDefaultText(TranslateText.NAME.getText())
        nameBox.draw(mouseX, mouseY, partialTicks)

        nvg.drawText(
            TranslateText.SERVER_IP.getText(),
            panelX + 24f + fieldWidth + 24f,
            fieldStartY,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.MEDIUM
        )
        serverIpBox.setPosition(panelX + 24f + fieldWidth + 24f, fieldStartY + 20f, fieldWidth, 20f)
        serverIpBox.setDefaultText(TranslateText.SERVER_IP.getText())
        serverIpBox.draw(mouseX, mouseY, partialTicks)



        val createHovered = MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 124f, this.getY() + this.getHeight() - 44f, 100f, 21f)
        createAnimation.setAnimation( if (createHovered) 1.0f else 0.0f, 12.0)


        nvg.drawRoundedRect(this.getX() + this.getWidth() - 124f, this.getY() + this.getHeight() - 44f, 100f, 21f, 6f,
            ColorUtils.applyAlpha(if (createHovered) accentColor.getInterpolateColor() else palette.getBackgroundColor(ColorType.NORMAL), (if (createHovered) 210 else 150) + createAnimation.value.toInt() * 20)
        )
        nvg.drawCenteredText(TranslateText.CREATE.getText(), this.getX() + this.getWidth() - 124f + 50f, this.getY() + this.getHeight() - 37.5F, palette.getFontColor(ColorType.DARK), 10f, Fonts.REGULAR)

        nvg.restore()

        val totalRows = kotlin.math.ceil(visibleProfiles.size / 2.0).toFloat()
        val contentHeight = totalRows * CARD_HEIGHT + kotlin.math.max(0f, totalRows - 1) * CARD_ROW_GAP
        scroll.maxScroll = kotlin.math.max(0f, contentHeight - viewportHeight)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val profileManager = instance.profileManager
        val modManager = instance.modManager
        val fileManager = instance.fileManager

        val scrollValue = scroll.getValue()
        val contentStartY = if (gridStartY > 0f) gridStartY else getY() + 56f
        val cardWidth = ((getWidth() - (CARD_HORIZONTAL_PADDING * 2) - CARD_COLUMN_GAP) / 2f)




        if (openProfile && detailTransition.isDetailsInteractive()) {
            val panelX = getX() + 18f
            val panelY = getY() + 15f
            val panelWidth = getWidth() - 36f
            val panelHeight = getHeight() - 30f

            var iconSelectorX = panelX + 24f
            val iconSelectorY = panelY + 66f
            val iconSelectorGap = 12f
            val iconTileSize = 24f

            for (icon in ProfileIcon.values()) {
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        iconSelectorX,
                        iconSelectorY,
                        iconTileSize,
                        iconTileSize
                    ) && mouseButton == 0
                ) {
                    currentIcon = icon
                    useCustomIcon = false
                }
                iconSelectorX += iconTileSize + iconSelectorGap
            }

            val customTileX = panelX + panelWidth - iconTileSize - 24f
            val customTileY = panelY + 66f

            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    customTileX,
                    customTileY,
                    iconTileSize,
                    iconTileSize
                ) && mouseButton == 0
            ) {
                if (selectedCustomIcon != null && !useCustomIcon) {
                    useCustomIcon = true
                } else {
                    openCustomIconPicker()
                }
            }

            nameBox.mouseClicked(mouseX, mouseY, mouseButton)
            serverIpBox.mouseClicked(mouseX, mouseY, mouseButton)

            val createButtonWidth = 100f
            val createButtonHeight = 21f
            val createButtonX = this.getX() + this.getWidth() - 124f
            val createButtonY = this.getY() + this.getHeight() - 44f

            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    createButtonX,
                    createButtonY,
                    createButtonWidth,
                    createButtonHeight
                ) && mouseButton == 0
            ) {
                if (nameBox.getText().isNotEmpty()) {
                    val serverIp = serverIpBox.getText().ifEmpty { "" }
                    val profileFile = File(fileManager.profileDir, nameBox.getText() + ".json")

                    profileManager.save(
                        profileFile,
                        serverIp,
                        ProfileType.ALL,
                        currentIcon,
                        if (useCustomIcon) selectedCustomIcon else null
                    )
                    profileManager.loadProfiles(false)

                    closeProfilePanel(clearIconSelection = true)
                    currentIcon = ProfileIcon.COMMAND
                }
            }

            /*
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    importButtonX,
                    importButtonY,
                    importButtonW,
                    importButtonH
                ) && mouseButton == 0
            ) {
                val code = importCodeBox.getText().trim().toUpperCase(Locale.ROOT)
                if (code.isNotEmpty()) {
                    if (code.length != 12) {
                        instance.notificationManager.post(
                            TranslateText.PROFILE_NOTIFICATION_TITLE,
                            TranslateText.PROFILE_IMPORT_FAILED,
                            NotificationType.ERROR
                        )
                        return
                    }
                    val shareManager = instance.profileShareManager
                    shareManager.requestFetch(code) { result ->
                        when (result) {
                            is ProfileShareManager.FetchResult.Success -> {
                                instance.notificationManager.post(
                                    TranslateText.PROFILE_NOTIFICATION_TITLE,
                                    TranslateText.PROFILE_IMPORT_SUCCESS,
                                    NotificationType.SUCCESS
                                )
                                importCodeBox.setText("")
                            }

                            is ProfileShareManager.FetchResult.Error -> {
                                instance.notificationManager.post(
                                    TranslateText.PROFILE_NOTIFICATION_TITLE,
                                    TranslateText.PROFILE_IMPORT_FAILED,
                                    NotificationType.ERROR
                                )
                            }
                        }
                    }
                }
            }

            */

            if (!MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    panelX - 6f,
                    panelY - 6f,
                    panelWidth + 12f,
                    panelHeight + 12f
                ) && mouseButton == 0
            ) {
                closeProfilePanel(clearIconSelection = true)
            }
        } else if (detailTransition.isListInteractive()) {
            if (mouseButton == 0) {
                for (chip in typeChips) {
                    if (chip.contains(mouseX, mouseY)) {
                        chip.click()
                        return
                    }
                }
            }

            val visibleProfiles = collectVisibleProfiles(profileManager)
            for (i in visibleProfiles.indices) {
                val profile = visibleProfiles[i]
                val column = i % 2
                val row = i / 2

                val cardX = getX() + CARD_HORIZONTAL_PADDING + column * (cardWidth + CARD_COLUMN_GAP)
                val cardY = contentStartY + row * (CARD_HEIGHT + CARD_ROW_GAP) + scrollValue

                if (!MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, CARD_HEIGHT)) {
                    continue
                }

                if (mouseButton == 0) {
                    if (profile.id == 999) {
                        openProfilePanel()
                        return
                    }

                    val isDefault = profile.id == -1
                    val iconSize = 18f
                    val iconX = cardX + cardWidth - iconSize - 18f
                    val startY = cardY + 10f
                    val starY = startY + iconSize + 10f
                    val deleteY = starY + iconSize + 10f

                    if (!isDefault && MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            iconX - 0.5f,
                            starY + 3f,
                            iconSize,
                            iconSize
                        )
                    ) {
                        if (profile.type == ProfileType.FAVORITE) {
                            profile.type = ProfileType.ALL
                        } else {
                            profile.type = ProfileType.FAVORITE
                        }
                        profileManager.save(
                            profile.jsonFile!!,
                            profile.serverIp,
                            profile.type,
                            profile.icon,
                            profile.customIcon
                        )
                        return
                    }

                    if (!isDefault && MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            iconX - 0.5f,
                            deleteY + 3f,
                            iconSize,
                            iconSize
                        )
                    ) {
                        val shareCode = profile.shareCode
                        if (!shareCode.isNullOrBlank()) {
                            instance.profileShareManager.requestUnshare(shareCode)
                        }
                        profileManager.delete(profile)
                        profileManager.loadProfiles(false)
                        return
                    }

                    if (profile.id != 999) {
                        modManager.disableAll()
                        val success = profileManager.load(profile.jsonFile)

                        if (success) {
                            instance.notificationManager.post(
                                TranslateText.PROFILE_NOTIFICATION_TITLE,
                                TranslateText.PROFILE_LOADED,
                                NotificationType.SUCCESS
                            )
                        } else {
                            instance.notificationManager.post(
                                TranslateText.PROFILE_NOTIFICATION_TITLE,
                                TranslateText.PROFILE_FAILED,
                                NotificationType.ERROR
                            )
                        }
                    }
                }


                /*
                if (mouseButton == 1 && profile.id != 999) {
                    val shareManager = instance.profileShareManager
                    shareManager.requestShare(profile) { result ->
                        when (result) {
                            is ProfileShareManager.ShareResult.Success -> {
                                profileManager.updateShareCode(profile, result.code)
                                IOUtils.copyStringToClipboard(result.code)
                                instance.notificationManager.post(
                                    TranslateText.PROFILE_NOTIFICATION_TITLE,
                                    TranslateText.PROFILE_SHARE_SUCCESS.getText() + ": " + result.code,
                                    NotificationType.SUCCESS
                                )
                            }

                            is ProfileShareManager.ShareResult.Error -> {
                                instance.notificationManager.post(
                                    TranslateText.PROFILE_NOTIFICATION_TITLE,
                                    TranslateText.PROFILE_SHARE_FAILED,
                                    NotificationType.ERROR
                                )
                            }
                        }
                    }
                    return
                }

                 */
            }
        }

        if (mouseButton == 3) {
            closeProfilePanel(clearIconSelection = true)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (openProfile && detailTransition.isDetailsInteractive()) {
            nameBox.keyTyped(typedChar, keyCode)
            serverIpBox.keyTyped(typedChar, keyCode)

            if (keyCode == Keyboard.KEY_ESCAPE) {
                closeProfilePanel(clearIconSelection = true)
            }
        } else if (detailTransition.isListInteractive()) {
            if (keyCode != 0xD0 && keyCode != 0xC8 && keyCode != Keyboard.KEY_ESCAPE) {
                getSearchBox().setFocused(true)
            }
        }
    }

    private fun openProfilePanel() {
        openProfile = true
        detailTransition.open()
        setCanClose(false)
    }

    private fun closeProfilePanel(clearIconSelection: Boolean) {
        openProfile = false
        detailTransition.close()
        if (clearIconSelection) {
            selectedCustomIcon = null
            useCustomIcon = false
        }
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
        var currentX = startX
        var currentY = getY() + 16f
        var blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT

        for (type in ProfileType.values()) {
            val label = type.name
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null)

            if (currentX + chipWidth > maxX) {
                currentX = startX
                currentY += CategoryChipRenderer.CHIP_HEIGHT + CHIP_GAP
                blockBottom = currentY + CategoryChipRenderer.CHIP_HEIGHT
            }

            val active = type == currentType
            val hovered = detailTransition.isListInteractive() && MouseUtils.isInside(
                mouseX,
                mouseY,
                currentX,
                currentY,
                chipWidth,
                CategoryChipRenderer.CHIP_HEIGHT
            )

            CategoryChipRenderer.drawChip(
                nvg,
                palette,
                accentColor,
                currentX,
                currentY,
                chipWidth,
                label,
                null,
                active,
                hovered
            )

            val chip = FilterChip(Runnable {
                if (currentType != type) {
                    currentType = type
                    scroll.resetAll()
                }
            })
            chip.setBounds(currentX, currentY, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            typeChips.add(chip)

            currentX += chipWidth + CHIP_GAP
        }

        return blockBottom
    }

    private fun openCustomIconPicker() {
        TaskExecutor.runAsync(ThreadPoolType.IO) {
            val fileManager = Shindo.getInstance().fileManager

            val file = FileUtils.selectImageFile()
            val iconDir = fileManager.profileIconDir

            if (file != null && iconDir.exists() && file.exists() && FileUtils.getExtension(file) == "png") {
                val destFile = File(iconDir, file.name)

                try {
                    FileUtils.copyFile(file, destFile)
                    val previousIcon = selectedCustomIcon
                    selectedCustomIcon = destFile
                    useCustomIcon = true
                    if (previousIcon != null && previousIcon.exists()) {
                        previousIcon.delete()
                    }
                } catch (e: IOException) {
                    ShindoLogger.error("Failed to copy custom profile icon", e)
                }
            }
        }
    }

    private fun collectVisibleProfiles(profileManager: ProfileManager): ArrayList<Profile> {
        val visible = ArrayList<Profile>()
        for (profile in profileManager.profiles) {
            if (profile.id == 999) {
                visible.add(profile)
                continue
            }
            if (!filter(profile)) {
                visible.add(profile)
            }
        }
        return visible
    }

    private fun filter(profile: Profile): Boolean {
        if (currentType == ProfileType.FAVORITE && profile.type != ProfileType.FAVORITE) {
            return true
        }
        return getSearchBox().getText().isNotEmpty() && !SearchUtils.isSimilar(profile.name, getSearchBox().getText())
    }

    private companion object {
        private const val CARD_HORIZONTAL_PADDING = 18f
        private const val CARD_COLUMN_GAP = 18f
        private const val CARD_ROW_GAP = 14f
        private const val CARD_HEIGHT = 94f
        private const val ICON_SIZE = 44f
        private const val CHIP_GAP = 8f
    }
}


