package me.miki.shindo.gui.mainmenu.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.management.skin.Skin
import me.miki.shindo.management.skin.SkinManager
import me.miki.shindo.management.skin.SkinPreviewRenderer
import me.miki.shindo.management.skin.SkinType
import me.miki.shindo.ui.comp.inputs.CompMainMenuTextBox
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseInOutCirc
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.buffer.ScreenAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.ArrayList
import java.util.EnumMap
import java.util.Locale
import kotlin.math.roundToInt

class SkinScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAnimation = ScreenAnimation()
    private val scroll = Scroll()
    private lateinit var introAnimation: Animation
    private val formTransition = SimpleAnimation()

    private val previewRenderer = SkinPreviewRenderer()
    private val cardSlots: MutableList<CardSlot> = ArrayList()
    private val filterChipBounds = EnumMap<FilterType, Hitbox>(FilterType::class.java)
    private val sourceChipBounds = EnumMap<SkinSource, Hitbox>(SkinSource::class.java)
    private val typeChipBounds = EnumMap<SkinType, Hitbox>(SkinType::class.java)

    private val formState = SkinFormState()

    private var currentFilter = FilterType.ALL
    private var formMode = FormMode.HIDDEN
    private var editingSkin: Skin? = null

    private var resetSelectionButton: Hitbox? = null
    private var saveButton: Hitbox? = null
    private var cancelButton: Hitbox? = null
    private var formBounds: Hitbox? = null

    override fun initScene() {
        introAnimation = EaseInOutCirc(250, 1.0)
        introAnimation.setDirection(Direction.FORWARDS)
        formTransition.value = 0f
        formMode = FormMode.HIDDEN
        editingSkin = null
        formState.resetForAdd()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        screenAnimation.wrap( Runnable { drawNanoVG(mouseX, mouseY, partialTicks, sr, instance, nvg) }, 0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), 2 - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f), false)
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(MainScene::class.java))
        }
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, partialTicks: Float, sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager?) {
        val palette: ColorPalette = getMenuPalette()
        val accent: AccentColor = getMenuAccent()
        val skinManager: SkinManager = instance.skinManager

        val acWidth = 640
        val acHeight = 370
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)

        val slideDistance = acWidth + CONTENT_SLIDE_EXTRA
        formTransition.setAnimation(if (formMode == FormMode.HIDDEN) 0f else 1f, 20.0)
        val transition = 0f.coerceAtLeast(1f.coerceAtMost(formTransition.value))
        val contentTranslate = -transition * slideDistance
        val formTranslate = (1f - transition) * slideDistance
        val logicalMouseX = (mouseX - acX - contentTranslate).roundToInt()
        val logicalMouseY = mouseY - acY
        val formMouseX = (mouseX - acX - formTranslate).roundToInt()
        val formMouseY = mouseY - acY
        val formVisible = isFormTransitionActive(transition)

        if (!formVisible) {
            scroll.onScroll()
        }
        scroll.onAnimation()

        nvg!!.save()
        nvg.translate(acX.toFloat(), acY.toFloat())
        nvg.drawRoundedRect(0f, 0f, acWidth.toFloat(), acHeight.toFloat(), 10f, getPanelColor())

        nvg.save()
        nvg.scissor(0f, 0f, acWidth.toFloat(), acHeight.toFloat())
        nvg.translate(contentTranslate, 0f)
        nvg.drawCenteredText(tx(TranslateText.SKIN_LIBRARY_TITLE), (acWidth / 2f), 10f, Color.WHITE, 16f, Fonts.SEMIBOLD)
        nvg.drawCenteredText(tx(TranslateText.SKIN_LIBRARY_SUBTITLE), (acWidth / 2f), 26f, palette.getFontColor(ColorType.DARK), 9.5f, Fonts.REGULAR)
        drawFilterChips(logicalMouseX, logicalMouseY, nvg, palette, accent, 18f, 50f)
        drawResetButton(logicalMouseX, logicalMouseY, nvg, palette, accent, acWidth - 132f, 50f)

        val gridX = 18f
        val gridY = 90f
        val gridWidth = acWidth - 36f
        val gridHeight = acHeight - 110f
        drawSkinGrid(logicalMouseX, logicalMouseY, partialTicks, nvg, palette, accent, skinManager, gridX, gridY, gridWidth, gridHeight)
        nvg.restore()

        if (formVisible) {
            nvg.save()
            nvg.intersectScissor(0f, 0f, acWidth.toFloat(), acHeight.toFloat())
            nvg.translate(formTranslate, 0f)
            val formX = (acWidth - FORM_PANEL_WIDTH) / 2f
            val formY = (acHeight - FORM_PANEL_HEIGHT) / 2f
            drawFormPanel(formMouseX, formMouseY, nvg, palette, accent, formX, formY)
            nvg.restore()
        } else {
            formBounds = null
        }

        nvg.restore()
    }

    private fun drawFilterChips(mouseX: Int, mouseY: Int, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, startX: Float, y: Float) {
        filterChipBounds.clear()
        var x = startX
        for (filterType in FilterType.entries) {
            val label = if (filterType == FilterType.ALL) tx(TranslateText.SKIN_FILTER_ALL) else tx(TranslateText.SKIN_FILTER_FAVORITES)
            val icon = if (filterType == FilterType.ALL) LegacyIcon.LIST else LegacyIcon.STAR
            val width = CategoryChipRenderer.computeWidth(nvg, label, icon)
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, CategoryChipRenderer.CHIP_HEIGHT)
            val active = currentFilter == filterType
            CategoryChipRenderer.drawChip(nvg, palette, accent, x, y, width, label, icon, active, hovered)
            filterChipBounds[filterType] = Hitbox(x, y, width, CategoryChipRenderer.CHIP_HEIGHT)
            x += width + 8
        }
    }

    private fun drawResetButton(mouseX: Int, mouseY: Int, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, x: Float, y: Float) {
        val width = 120f
        val height = 22f
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        var background = palette.getBackgroundColor(ColorType.DARK)
        if (hovered) {
            background = ColorUtils.applyAlpha(background, 220)
        }
        nvg.drawRoundedRect(x, y, width, height, 6f, background)
        nvg.drawCenteredText(tx(TranslateText.SKIN_RESET_BUTTON), x + (width / 2f), y + 7f, Color.WHITE, 9.5f, Fonts.MEDIUM)
        nvg.drawText(LegacyIcon.REFRESH, x + 7f, y + 6f, Color.WHITE, 10f, Fonts.LEGACYICON)
        resetSelectionButton = Hitbox(x, y, width, height)
    }
    private fun drawSkinGrid(mouseX: Int, mouseY: Int, partialTicks: Float, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, skinManager: SkinManager, gridX: Float, gridY: Float, gridWidth: Float, gridHeight: Float) {
        cardSlots.clear()
        val entries: MutableList<Skin> = ArrayList(skinManager.skins)
        if (currentFilter == FilterType.FAVORITES) {
            entries.removeIf { skin -> !skin.isFavorite }
        }

        val cards: MutableList<Any?> = ArrayList()
        cards.add(null)
        cards.addAll(entries)

        val cardWidth = (gridWidth - (CARD_GAP * (CARDS_PER_ROW - 1))) / CARDS_PER_ROW
        val scrollValue = scroll.getValue()

        nvg.save()
        nvg.intersectScissor(gridX, gridY - 8f, gridWidth, gridHeight + 16f)
        nvg.translate(0f, scrollValue)

        var column = 0
        var row = 0

        for (entry in cards) {
            val cardX = gridX + column * (cardWidth + CARD_GAP)
            val cardY = gridY + row * (CARD_HEIGHT + CARD_GAP)
            val renderY = cardY + scrollValue
            if (entry != null) {
                val skin = entry as Skin
                val slot = drawSkinCard(mouseX, mouseY, partialTicks, scrollValue, nvg, palette, accent, skinManager, skin, cardX, cardY, cardWidth)
                slot.area = Hitbox(cardX, renderY, cardWidth, CARD_HEIGHT)
                cardSlots.add(slot)
            } else {
                drawAddCard(mouseX, mouseY, scrollValue, nvg, palette, accent, cardX, cardY, cardWidth)
                cardSlots.add(CardSlot.createAdd(Hitbox(cardX, renderY, cardWidth, CARD_HEIGHT)))
            }

            column++
            if (column >= CARDS_PER_ROW) {
                column = 0
                row++
            }
        }

        nvg.restore()

        val totalRows = Math.ceil(cards.size / CARDS_PER_ROW.toDouble()).toInt()
        val contentHeight = Math.max(0f, totalRows * (CARD_HEIGHT + CARD_GAP) - CARD_GAP)
        val maxScroll = Math.max(0f, contentHeight - gridHeight)
        scroll.maxScroll = maxScroll

        if (entries.isEmpty()) {
            nvg.drawCenteredText(tx(TranslateText.SKIN_EMPTY_PRIMARY), gridX + (gridWidth / 2f), gridY + (gridHeight / 2f) - 18f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
            nvg.drawCenteredText(tx(TranslateText.SKIN_EMPTY_SECONDARY), gridX + (gridWidth / 2f), gridY + (gridHeight / 2f) - 2f, palette.getFontColor(ColorType.DARK), 9f, Fonts.REGULAR)
        }
    }

    private fun drawAddCard(mouseX: Int, mouseY: Int, scrollValue: Float, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, cardX: Float, cardY: Float, cardWidth: Float) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT)
        val base = palette.getBackgroundColor(ColorType.DARK)
        if (hovered) {
            ColorUtils.applyAlpha(base, 230)
        }
        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 8f, getControlColor())
        nvg.drawRoundedRect(cardX + 8f, cardY + 8f, cardWidth - 16f, CARD_HEIGHT - 16f, 8f, getPanelColor())
        nvg.drawCenteredText(LegacyIcon.PLUS, cardX + (cardWidth / 2f), cardY + 36f, Color.WHITE, 26f, Fonts.LEGACYICON)
        nvg.drawCenteredText(tx(TranslateText.SKIN_ADD_CARD_TITLE), cardX + (cardWidth / 2f), cardY + CARD_HEIGHT - 45f, Color.WHITE, 11f, Fonts.MEDIUM)
        nvg.drawCenteredText(tx(TranslateText.SKIN_ADD_CARD_SUBTITLE), cardX + (cardWidth / 2f), cardY + CARD_HEIGHT - 30f, palette.getFontColor(ColorType.DARK), 8.5f, Fonts.REGULAR)
    }

    private fun drawSkinCard(mouseX: Int, mouseY: Int, partialTicks: Float, scrollValue: Float, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, skinManager: SkinManager, skin: Skin, cardX: Float, cardY: Float, cardWidth: Float): CardSlot {
        val hovered = MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT)
        val current = skinManager.currentSkin
        val selected = current != null && current == skin

        val base = palette.getBackgroundColor(ColorType.DARK)
        val background = if (hovered) ColorUtils.applyAlpha(base, 225) else base
        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 8f, background)

        if (selected) {
            nvg.drawGradientRoundedRect(cardX - 1, cardY - 1, cardWidth + 2, CARD_HEIGHT + 2, 9f, ColorUtils.applyAlpha(accent.color1, 120), ColorUtils.applyAlpha(accent.color2, 120))
        }

        val limitedName = nvg.getLimitText(skin.name, 12f, Fonts.MEDIUM, cardWidth - 70)
        val nameWidth = nvg.getTextWidth(limitedName, 12f, Fonts.MEDIUM) + 12f
        nvg.drawRoundedRect(cardX + 8f, cardY + 8f, nameWidth, 18f, 6f, base)
        nvg.drawText(limitedName, cardX + 12f, cardY + 12f, Color.WHITE, 12f, Fonts.MEDIUM)

        if (selected) {
            val badge = tx(TranslateText.SKIN_BADGE_IN_USE)
            val badgeWidth = nvg.getTextWidth(badge, 8f, Fonts.REGULAR) + 12
            nvg.drawRoundedRect(cardX + cardWidth - badgeWidth - 12, cardY + 28f, badgeWidth, 14f, 6f, ColorUtils.applyAlpha(accent.color1, 200))
            nvg.drawText(badge, cardX + cardWidth - badgeWidth - 6, cardY + 32f, Color.WHITE, 8f, Fonts.REGULAR)
        }

        val iconSize = 16f
        val iconY = cardY + 8f
        val starX = cardX + cardWidth - iconSize - 6
        val editX = starX - iconSize - 4
        val deleteX = editX - iconSize - 4

        val slot = CardSlot(skin, false)
        slot.favoriteButton = drawIconButton(nvg, palette, accent, starX, iconY, iconSize, if (skin.isFavorite) LegacyIcon.STAR_FILL else LegacyIcon.STAR, skin.isFavorite, scrollValue)
        slot.editButton = drawIconButton(nvg, palette, accent, editX, iconY, iconSize, LegacyIcon.EDIT, false, scrollValue)
        slot.deleteButton = drawIconButton(nvg, palette, accent, deleteX, iconY, iconSize, LegacyIcon.TRASH, false, scrollValue)

        val previewBottom = cardY + CARD_HEIGHT - 40
        val previewMaxWidth = Math.max(20f, cardWidth - 32f)
        val previewMaxHeight = Math.max(20f, CARD_HEIGHT - 86f)
        val scaleByWidth = previewMaxWidth / previewRenderer.baseWidth
        val scaleByHeight = previewMaxHeight / previewRenderer.baseHeight
        val previewScale = Math.min(Math.min(scaleByWidth, scaleByHeight), 4.0f)
        val rendered = renderPreview(skin, cardX + (cardWidth / 2f), previewBottom, previewScale, nvg)
        if (!rendered) {
            nvg.drawCenteredText(tx(TranslateText.SKIN_PREVIEW_UNAVAILABLE), cardX + (cardWidth / 2f), previewBottom - 10, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.REGULAR)
        }

        val typeLabel = if (skin.type == SkinType.SLIM) tx(TranslateText.SKIN_TYPE_SLIM) else tx(TranslateText.SKIN_TYPE_DEFAULT)
        val typeWidth = nvg.getTextWidth(typeLabel, 9f, Fonts.REGULAR) + 10f
        nvg.drawRoundedRect(cardX + 12f, cardY + CARD_HEIGHT - 43f, typeWidth, 14f, 4f, palette.getBackgroundColor(ColorType.NORMAL))
        nvg.drawText(typeLabel, cardX + 16f, cardY + CARD_HEIGHT - 40f, Color.WHITE, 9f, Fonts.REGULAR)

        val buttonWidth = cardWidth - 24
        val buttonHeight = 20f
        val buttonX = cardX + 12
        val buttonY = cardY + CARD_HEIGHT - buttonHeight - 8
        val buttonColor = if (selected) ColorUtils.applyAlpha(accent.color1, 220) else palette.getBackgroundColor(ColorType.NORMAL)
        nvg.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, 6f, buttonColor)
        nvg.drawCenteredText(if (selected) tx(TranslateText.SKIN_BUTTON_SELECTED) else tx(TranslateText.SKIN_BUTTON_USE), buttonX + (buttonWidth / 2f), buttonY + 6f, Color.WHITE, 9.5f, Fonts.MEDIUM)

        slot.selectButton = Hitbox(buttonX, buttonY + scrollValue, buttonWidth, buttonHeight)
        return slot
    }

    private fun drawIconButton(nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, x: Float, y: Float, size: Float, icon: String, active: Boolean, scrollValue: Float): Hitbox {
        var background = palette.getBackgroundColor(ColorType.NORMAL)
        if (active) {
            background = ColorUtils.applyAlpha(accent.color1, 200)
        }
        nvg.drawRoundedRect(x, y, size, size, 4f, background)
        nvg.drawCenteredText(icon, x + (size / 2f), y + 3f, Color.WHITE, 11f, Fonts.LEGACYICON)
        return Hitbox(x, y + scrollValue, size, size)
    }
    private fun renderPreview(skin: Skin?, centerX: Float, bottomY: Float, pixelScale: Float, nvg: NanoVGManager): Boolean {
        if (skin == null) {
            return false
        }

        val uuid = skin.profileUuid
        if (uuid == null || uuid.trim().isEmpty()) {
            return false
        }

        val width = previewRenderer.baseWidth * pixelScale
        val height = previewRenderer.baseHeight * pixelScale
        val drawX = centerX - (width / 2f)
        val drawY = bottomY - height

        previewRenderer.renderRemoteSkinPreview(
            nvg.getContext(),
            uuid,
            drawX,
            drawY,
            pixelScale,
            Color(0, 0, 0, 35),
            null
        )
        return previewRenderer.isPreviewCached(uuid)
    }

    private fun drawFormPanel(mouseX: Int, mouseY: Int, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, formX: Float, formY: Float) {
        val formWidth = FORM_PANEL_WIDTH
        val formHeight = FORM_PANEL_HEIGHT
        val drawX = formX
        formBounds = Hitbox(formX, formY, formWidth, formHeight)

        nvg.drawRoundedRect(drawX, formY, formWidth, formHeight, 10f, getPanelColor())
        nvg.drawCenteredText(if (formMode == FormMode.ADD) tx(TranslateText.SKIN_FORM_ADD_TITLE) else tx(TranslateText.SKIN_FORM_EDIT_TITLE), drawX + (formWidth / 2f), formY + 12f, Color.WHITE, 14f, Fonts.SEMIBOLD)

        val inset = 16f
        var currentY = formY + 38f

        formState.nameField.setPosition(drawX + inset, currentY, formWidth - (inset * 2f), 22F)
        formState.nameField.setBackgroundColor(palette.getBackgroundColor(ColorType.DARK))
        formState.nameField.setFontColor(Color.WHITE)
        formState.nameField.setEmptyText(LegacyIcon.PENCIL, TranslateText.SKIN_FIELD_NAME_PLACEHOLDER.text)
        formState.nameField.draw(mouseX, mouseY, 0f)
        currentY += 40f

        nvg.drawText(tx(TranslateText.SKIN_FORM_SOURCE_LABEL), drawX + inset, currentY - 6f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.REGULAR)
        drawSourceChips(mouseX, mouseY, nvg, palette, accent, drawX + inset, currentY + 4f)
        currentY += CategoryChipRenderer.CHIP_HEIGHT + 18f

        if (formState.source == SkinSource.USERNAME) {
            formState.usernameField.setPosition(drawX + inset, currentY, formWidth - (inset * 2f), 22F)
            formState.usernameField.setBackgroundColor(palette.getBackgroundColor(ColorType.DARK))
            formState.usernameField.setFontColor(Color.WHITE)
            formState.usernameField.setEmptyText(LegacyIcon.USER, TranslateText.SKIN_FIELD_USERNAME_PLACEHOLDER.text)
            formState.usernameField.draw(mouseX, mouseY, 0f)
        } else {
            formState.uuidField.setPosition(drawX + inset, currentY, formWidth - (inset * 2f), 22F)
            formState.uuidField.setBackgroundColor(palette.getBackgroundColor(ColorType.DARK))
            formState.uuidField.setFontColor(Color.WHITE)
            formState.uuidField.setEmptyText(LegacyIcon.KEY, TranslateText.SKIN_FIELD_UUID_PLACEHOLDER.text)
            formState.uuidField.draw(mouseX, mouseY, 0f)
        }
        currentY += 40f

        nvg.drawText(tx(TranslateText.SKIN_FORM_MODEL_LABEL), drawX + inset, currentY - 6f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.REGULAR)
        drawTypeChips(mouseX, mouseY, nvg, palette, accent, drawX + inset, currentY + 4f)

        if (formState.statusMessage != null) {
            val statusColor = if (formState.statusError) palette.getMaterialRed(220) else palette.getFontColor(ColorType.NORMAL)
            nvg.drawText(formState.statusMessage!!, drawX + inset, formY + formHeight - 74, statusColor, 8.5f, Fonts.REGULAR)
        }

        val buttonWidth = (formWidth - (inset * 2f) - 8f) / 2f
        val buttonHeight = 22f
        val buttonY = formY + formHeight - 46f

        cancelButton = Hitbox(formX + inset, buttonY, buttonWidth, buttonHeight)
        saveButton = Hitbox(formX + inset + buttonWidth + 8f, buttonY, buttonWidth, buttonHeight)

        nvg.drawRoundedRect(drawX + inset, buttonY, buttonWidth, buttonHeight, 6f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawCenteredText(tx(TranslateText.SKIN_FORM_CANCEL), drawX + inset + (buttonWidth / 2f), buttonY + 6f, Color.WHITE, 9.5f, Fonts.MEDIUM)

        val saveColor = if (formState.processing) palette.getFontColor(ColorType.DARK) else ColorUtils.applyAlpha(accent.color1, 220)
        nvg.drawRoundedRect(drawX + inset + buttonWidth + 8f, buttonY, buttonWidth, buttonHeight, 6f, saveColor)
        nvg.drawCenteredText(if (formMode == FormMode.ADD) tx(TranslateText.SKIN_FORM_ADD_ACTION) else tx(TranslateText.SKIN_FORM_SAVE_ACTION), drawX + inset + buttonWidth + 8f + (buttonWidth / 2f), buttonY + 6f, Color.WHITE, 9.5f, Fonts.MEDIUM)
    }

    private fun drawSourceChips(mouseX: Int, mouseY: Int, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, startX: Float, y: Float) {
        sourceChipBounds.clear()
        var x = startX
        for (source in SkinSource.values()) {
            val label: String
            val icon: String
            if (source == SkinSource.USERNAME) {
                label = tx(TranslateText.SKIN_SOURCE_USERNAME)
                icon = LegacyIcon.USER
            } else {
                label = tx(TranslateText.SKIN_SOURCE_UUID)
                icon = LegacyIcon.KEY
            }
            val width = CategoryChipRenderer.computeWidth(nvg, label, icon)
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, CategoryChipRenderer.CHIP_HEIGHT)
            val active = formState.source == source
            CategoryChipRenderer.drawChip(nvg, palette, accent, x, y, width, label, icon, active, hovered)
            sourceChipBounds[source] = Hitbox(x, y, width, CategoryChipRenderer.CHIP_HEIGHT)
            x += width + 8
        }
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
        disposePreviews()
    }

    override fun onSceneClosed() {
        super.onSceneClosed()
        disposePreviews()
    }

    private fun disposePreviews() {
        val nvg = Shindo.getInstance().nanoVGManager
        val vg = nvg?.getContext() ?: 0L
        previewRenderer.clearCache(vg)
    }

    private fun drawTypeChips(mouseX: Int, mouseY: Int, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, startX: Float, y: Float) {
        typeChipBounds.clear()
        var x = startX
        for (type in SkinType.entries) {
            val label = if (type == SkinType.SLIM) tx(TranslateText.SKIN_TYPE_SLIM) else tx(TranslateText.SKIN_TYPE_DEFAULT)
            val width = CategoryChipRenderer.computeWidth(nvg, label, LegacyIcon.USER)
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, CategoryChipRenderer.CHIP_HEIGHT)
            val active = formState.selectedType == type
            CategoryChipRenderer.drawChip(nvg, palette, accent, x, y, width, label, LegacyIcon.USER, active, hovered)
            typeChipBounds[type] = Hitbox(x, y, width, CategoryChipRenderer.CHIP_HEIGHT)
            x += width + 8
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)
        val acWidth = 640
        val acHeight = 370
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)
        val slideDistance = acWidth + CONTENT_SLIDE_EXTRA
        val transition = Math.max(0f, Math.min(1f, formTransition.value))
        val contentTranslate = -transition * slideDistance
        val formTranslate = (1f - transition) * slideDistance
        val logicalMouseX = Math.round(mouseX - acX - contentTranslate)
        val logicalMouseY = mouseY - acY
        val formMouseX = Math.round(mouseX - acX - formTranslate)
        val formMouseY = mouseY - acY
        val formVisible = isFormTransitionActive(transition)

        if (formVisible) {
            if (formMode != FormMode.HIDDEN) {
                if (mouseButton == 0 && formBounds != null && !formBounds!!.contains(formMouseX, formMouseY)) {
                    closeForm()
                    return
                }
                if (handleFormClick(formMouseX, formMouseY, mouseButton)) {
                    return
                }
            }
            return
        }

        if (!MouseUtils.isInside(mouseX, mouseY, acX.toFloat(), acY.toFloat(), acWidth.toFloat(), acHeight.toFloat())
            && !MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 112f, 6f, 22f, 22f)) {
            introAnimation.setDirection(Direction.BACKWARDS)
            return
        }

        if (mouseButton == 0) {
            for ((key, value) in filterChipBounds) {
                if (value.contains(logicalMouseX, logicalMouseY)) {
                    if (currentFilter != key) {
                        currentFilter = key
                        scroll.resetAll()
                    }
                    return
                }
            }
            if (resetSelectionButton != null && resetSelectionButton!!.contains(logicalMouseX, logicalMouseY)) {
                resetSelection()
                return
            }
            for (slot in cardSlots) {
                if (slot.area != null && slot.area!!.contains(logicalMouseX, logicalMouseY)) {
                    if (slot.addCard) {
                        openAddForm()
                        return
                    }
                    if (slot.favoriteButton != null && slot.favoriteButton!!.contains(logicalMouseX, logicalMouseY)) {
                        toggleFavorite(slot.skin)
                        return
                    }
                    if (slot.editButton != null && slot.editButton!!.contains(logicalMouseX, logicalMouseY)) {
                        openEditForm(slot.skin)
                        return
                    }
                    if (slot.deleteButton != null && slot.deleteButton!!.contains(logicalMouseX, logicalMouseY)) {
                        deleteSkin(slot.skin)
                        return
                    }
                    if (slot.selectButton != null && slot.selectButton!!.contains(logicalMouseX, logicalMouseY)) {
                        selectSkin(slot.skin)
                        return
                    }
                }
            }
        }
    }

    private fun handleFormClick(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        formState.nameField.mouseClicked(mouseX, mouseY, mouseButton)
        formState.usernameField.mouseClicked(mouseX, mouseY, mouseButton)
        formState.uuidField.mouseClicked(mouseX, mouseY, mouseButton)

        if (mouseButton == 0) {
            for ((key, value) in sourceChipBounds) {
                if (value.contains(mouseX, mouseY)) {
                    formState.source = key
                    return true
                }
            }
            for ((key, value) in typeChipBounds) {
                if (value.contains(mouseX, mouseY)) {
                    formState.selectedType = key
                    return true
                }
            }
            if (cancelButton != null && cancelButton!!.contains(mouseX, mouseY)) {
                closeForm()
                return true
            }
            if (!formState.processing && saveButton != null && saveButton!!.contains(mouseX, mouseY)) {
                handleSubmitForm()
                return true
            }
        }
        return false
    }
    private fun handleSubmitForm() {
        if (formState.processing) {
            return
        }
        if (formMode == FormMode.EDIT && formState.displayName.isEmpty()) {
            updateFormStatus(tx(TranslateText.SKIN_STATUS_NAME_REQUIRED), true)
            return
        }

        val manager = Shindo.getInstance().skinManager
        if (manager == null) {
            updateFormStatus(tx(TranslateText.SKIN_STATUS_MANAGER_UNAVAILABLE), true)
            return
        }

        formState.processing = true
        updateFormStatus(tx(TranslateText.SKIN_STATUS_PROCESSING), false)

        val providedName = formState.displayName
        val source = formState.source
        val selectedType = formState.selectedType
        val username = formState.username
        val uuid = formState.uuid
        val editing = editingSkin
        val currentMode = formMode

        Multithreading.runAsync {
            try {
                if (currentMode == FormMode.ADD) {
                    processAdd(manager, providedName, source, selectedType, username, uuid)
                } else if (editing != null) {
                    processEdit(manager, editing, providedName, source, selectedType, username, uuid)
                }
                mc.addScheduledTask {
                    formState.processing = false
                    updateFormStatus(tx(TranslateText.SKIN_STATUS_SAVED), false)
                    closeForm()
                }
            } catch (e: Exception) {
                ShindoLogger.error("Skin form error", e)
                mc.addScheduledTask {
                    formState.processing = false
                    updateFormStatus(e.message ?: tx(TranslateText.SKIN_STATUS_GENERIC_ERROR), true)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun processAdd(manager: SkinManager, providedName: String, source: SkinSource, selectedType: SkinType, username: String, uuid: String) {
        val type = selectedType
        when (source) {
            SkinSource.UUID -> {
                if (uuid.isEmpty()) {
                    throw IOException(tx(TranslateText.SKIN_STATUS_UUID_INVALID))
                }
                val remote = manager.downloadSkinByUuid(uuid)
                val nameFromUuid = if (providedName.isEmpty()) uuid.substring(0, Math.min(12, uuid.length)) else providedName
                manager.addSkin(nameFromUuid, type, false, remote.image, remote.uuid)
            }
            SkinSource.USERNAME -> {
                if (username.isEmpty()) {
                    throw IOException(tx(TranslateText.SKIN_STATUS_USERNAME_INVALID))
                }
                val downloaded = manager.downloadSkinByUsername(username)
                val nameFromUser = if (providedName.isEmpty()) username else providedName
                manager.addSkin(nameFromUser, type, false, downloaded.image, downloaded.uuid)
            }
        }
    }

    @Throws(IOException::class)
    private fun processEdit(manager: SkinManager, skin: Skin, newName: String, source: SkinSource, selectedType: SkinType, username: String, uuid: String) {
        var replacement: BufferedImage? = null
        var profileUuid: String? = null
        when (source) {
            SkinSource.UUID -> {
                if (uuid.isNotEmpty()) {
                    val remote = manager.downloadSkinByUuid(uuid)
                    replacement = remote.image
                    profileUuid = remote.uuid
                }
            }
            SkinSource.USERNAME -> {
                if (username.isNotEmpty()) {
                    val downloaded = manager.downloadSkinByUsername(username)
                    replacement = downloaded.image
                    profileUuid = downloaded.uuid
                }
            }
        }
        val finalName = if (newName.isEmpty()) skin.name else newName
        val type = selectedType
        manager.updateSkin(skin, finalName, type, replacement, profileUuid)
    }

    private fun updateFormStatus(message: String, error: Boolean) {
        formState.statusMessage = message
        formState.statusError = error
    }

    private fun openAddForm() {
        formState.resetForAdd()
        editingSkin = null
        formMode = FormMode.ADD
    }

    private fun openEditForm(skin: Skin?) {
        if (skin == null) {
            return
        }
        formState.resetForEdit(skin)
        editingSkin = skin
        formMode = FormMode.EDIT
    }

    private fun closeForm() {
        formMode = FormMode.HIDDEN
        editingSkin = null
        formState.resetForAdd()
    }

    private fun selectSkin(skin: Skin?) {
        if (skin == null) {
            return
        }
        Shindo.getInstance().skinManager.setCurrentSkin(skin)
        Shindo.getInstance().notificationManager.post(tx(TranslateText.SKIN_NOTIFICATION_TITLE), String.format(Locale.ROOT, tx(TranslateText.SKIN_NOTIFICATION_SELECTED), skin.name), NotificationType.SUCCESS)
    }

    private fun toggleFavorite(skin: Skin?) {
        if (skin == null) {
            return
        }
        Shindo.getInstance().skinManager.setFavorite(skin, !skin.isFavorite)
    }

    private fun deleteSkin(skin: Skin?) {
        if (skin == null) {
            return
        }
        Shindo.getInstance().skinManager.deleteSkin(skin)
        Shindo.getInstance().notificationManager.post(tx(TranslateText.SKIN_NOTIFICATION_TITLE), tx(TranslateText.SKIN_NOTIFICATION_REMOVED), NotificationType.WARNING)
    }

    private fun resetSelection() {
        Shindo.getInstance().skinManager.clearCurrentSkin()
        Shindo.getInstance().notificationManager.post(tx(TranslateText.SKIN_NOTIFICATION_TITLE), tx(TranslateText.SKIN_NOTIFICATION_RESET), NotificationType.INFO)
    }

    private fun isFormTransitionActive(transition: Float): Boolean {
        return formMode != FormMode.HIDDEN || transition > 0.01f
    }

    private fun isFormTransitionActive(): Boolean {
        return formMode != FormMode.HIDDEN || formTransition.value > 0.01f
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (isFormTransitionActive()) {
            if (formMode != FormMode.HIDDEN) {
                formState.nameField.keyTyped(typedChar, keyCode)
                formState.usernameField.keyTyped(typedChar, keyCode)
                formState.uuidField.keyTyped(typedChar, keyCode)
                if (keyCode == Keyboard.KEY_ESCAPE) {
                    closeForm()
                }
            }
            return
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
        scroll.onKey(keyCode)
    }

    private fun tx(text: TranslateText): String {
        return text.text
    }

    private enum class FilterType {
        ALL,
        FAVORITES
    }

    private enum class FormMode {
        HIDDEN,
        ADD,
        EDIT
    }

    private enum class SkinSource {
        USERNAME,
        UUID
    }

    private class Hitbox(val x: Float, val y: Float, val width: Float, val height: Float) {
        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }
    }

    private class CardSlot(val skin: Skin?, val addCard: Boolean) {
        var area: Hitbox? = null
        var selectButton: Hitbox? = null
        var favoriteButton: Hitbox? = null
        var editButton: Hitbox? = null
        var deleteButton: Hitbox? = null

        companion object {
            fun createAdd(hitbox: Hitbox): CardSlot {
                val slot = CardSlot(null, true)
                slot.area = hitbox
                return slot
            }
        }
    }

    private class SkinFormState {
        val nameField = CompMainMenuTextBox()
        val usernameField = CompMainMenuTextBox()
        val uuidField = CompMainMenuTextBox()
        var source = SkinSource.USERNAME
        var selectedType = SkinType.DEFAULT
        var processing = false
        var statusMessage: String? = null
        var statusError = false

        init {
            applyPlaceholders()
        }

        fun resetForAdd() {
            applyPlaceholders()
            nameField.setText("");
            usernameField.setText("");
            uuidField.setText("");
            source = SkinSource.USERNAME
            selectedType = SkinType.DEFAULT
            processing = false
            statusMessage = null
            statusError = false
        }

        fun resetForEdit(skin: Skin) {
            applyPlaceholders()
            nameField.setText(skin.name);
            usernameField.setText("");
            uuidField.setText(skin.profileUuid ?: "");
            selectedType = skin.type
            source = if (skin.profileUuid == null) SkinSource.USERNAME else SkinSource.UUID
            processing = false
            statusMessage = null
            statusError = false
        }

        val displayName: String
            get() = nameField.getText().trim()

        val username: String
            get() = usernameField.getText().trim()

        val uuid: String
            get() = uuidField.getText().trim()

        private fun applyPlaceholders() {
            nameField.setEmptyText(LegacyIcon.PENCIL, TranslateText.SKIN_FIELD_NAME_PLACEHOLDER.text)
            usernameField.setEmptyText(LegacyIcon.USER, TranslateText.SKIN_FIELD_USERNAME_PLACEHOLDER.text)
            uuidField.setEmptyText(LegacyIcon.KEY, TranslateText.SKIN_FIELD_UUID_PLACEHOLDER.text)
        }
    }

    companion object {
        private const val CARDS_PER_ROW = 4
        private const val CARD_GAP = 12f
        private const val CARD_HEIGHT = 152f
        private const val FORM_PANEL_WIDTH = 360f
        private const val FORM_PANEL_HEIGHT = 340f
        private const val CONTENT_SLIDE_EXTRA = 48f
    }
}
