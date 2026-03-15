package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.api.chat.ChatFriend
import me.miki.shindo.api.chat.ChatRequest
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.screen.ScreenAnimation
import me.miki.shindo.ui.comp.inputs.CompTextBox
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.PlayerHeadUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import kotlin.math.max

class GuiFriendsChat(private val parent: GuiScreen? = null) : GuiScreen(), IShindoScreen {

    private val screenAnimation = ScreenAnimation()
    private val addFriendBox = CompTextBox()
    private val messageBox = CompTextBox()
    private val friendScroll = Scroll()
    private val chatScroll = Scroll()
    private val friendEntries = ArrayList<FriendEntry>()
    private val requestEntries = ArrayList<RequestEntry>()
    private var selectedFriend: ChatFriend? = null

    private var addButtonX = 0f
    private var addButtonY = 0f
    private var addButtonW = 0f
    private var addButtonH = 0f

    private var sendButtonX = 0f
    private var sendButtonY = 0f
    private var sendButtonW = 0f
    private var sendButtonH = 0f

    private var x = 0f
    private var y = 0f
    private var menuWidth = 0f
    private var menuHeight = 0f

    override fun initGui() {
        val sr = ScaledResolution(mc)
        menuWidth = minOf(720f, sr.scaledWidth.toFloat() - 24f)
        menuHeight = minOf(420f, sr.scaledHeight.toFloat() - 24f)
        x = (sr.scaledWidth - menuWidth) / 2f
        y = (sr.scaledHeight - menuHeight) / 2f

        friendScroll.resetAll()
        chatScroll.resetAll()
        addFriendBox.setText("")
        messageBox.setText("")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        BlurUtils.drawBlurScreen(20f)
        val instance = Shindo.getInstance()
        instance.chatManager
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.colorManager.getPalette()
        val accent = instance.colorManager.getCurrentColor()

        screenAnimation.wrap(
            Runnable { drawContent(nvg, palette, accent, mouseX, mouseY, partialTicks) },
            x,
            y,
            menuWidth,
            menuHeight,
            1f,
            1f,
            false
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawContent(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        nvg.drawRect(0f, 0f, width.toFloat(), height.toFloat(), java.awt.Color(0, 0, 0, 140))
        nvg.drawShadow(x, y, menuWidth, menuHeight, 12f)
        nvg.drawRoundedRect(x, y, menuWidth, menuHeight, 10f, palette.getBackgroundColor(ColorType.NORMAL))
        nvg.drawRoundedRect(
            x + 1f,
            y + 1f,
            menuWidth - 2f,
            menuHeight - 2f,
            9f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 235)
        )

        val chatManager = Shindo.getInstance().chatManager
        friendEntries.clear()
        requestEntries.clear()

        if (!chatManager.isFeatureAvailable()) {
            drawUnavailable(nvg, palette)
            return
        }

        val padding = 16f
        val columnGap = 18f
        val leftWidth = 260f
        val leftX = x + padding
        val topY = y + padding
        val rightX = leftX + leftWidth + columnGap
        val rightWidth = menuWidth - leftWidth - columnGap - padding * 2
        val panelHeight = menuHeight - padding * 2

        drawPanel(nvg, leftX, topY, leftWidth, panelHeight, palette)
        drawPanel(nvg, rightX, topY, rightWidth, panelHeight, palette)

        val friends = chatManager.getFriends()
        if (selectedFriend != null && friends.none { it.uuid == selectedFriend?.uuid }) {
            selectedFriend = null
        }

        val leftHeaderY = topY + 14f
        nvg.drawText(
            TranslateText.CHAT_FRIENDS.getText(),
            leftX + 14f,
            leftHeaderY,
            palette.getFontColor(ColorType.DARK),
            13f,
            Fonts.SEMIBOLD
        )
        nvg.drawRect(
            leftX + 14f,
            leftHeaderY + 16f,
            leftWidth - 28f,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 80)
        )

        val addY = leftHeaderY + 24f
        addFriendBox.setDefaultText(TranslateText.CHAT_ADD_FRIEND_PLACEHOLDER.getText())
        addFriendBox.setPosition(leftX + 14f, addY, leftWidth - 64f, 20f)
        addFriendBox.draw(mouseX, mouseY, partialTicks)

        addButtonW = 32f
        addButtonH = 20f
        addButtonX = leftX + leftWidth - addButtonW - 14f
        addButtonY = addY
        drawIconButton(
            nvg,
            palette,
            accent,
            addButtonX,
            addButtonY,
            addButtonW,
            addButtonH,
            LegacyIcon.PLUS,
            mouseX,
            mouseY
        )

        val requestsTitleY = addY + 32f
        nvg.drawText(
            TranslateText.CHAT_REQUESTS.getText(),
            leftX + 14f,
            requestsTitleY,
            palette.getFontColor(ColorType.NORMAL),
            10.5f,
            Fonts.MEDIUM
        )

        var currentY = requestsTitleY + 14f
        val requestHeight = 30f
        val requests = chatManager.getRequests()
        if (requests.isEmpty()) {
            nvg.drawText(
                TranslateText.CHAT_NO_REQUESTS.getText(),
                leftX + 14f,
                currentY + 10f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                9f,
                Fonts.REGULAR
            )
            currentY += 16f
        } else {
            for (request in requests) {
                drawRequestRow(
                    nvg,
                    palette,
                    accent,
                    request,
                    leftX + 10f,
                    currentY,
                    leftWidth - 20f,
                    requestHeight,
                    mouseX,
                    mouseY
                )
                currentY += requestHeight + 8f
            }
        }

        val friendsStartY = currentY + 16f
        val friendsAreaHeight = topY + panelHeight - friendsStartY - 12f
        val friendRowHeight = 38f

        var friendY = friendsStartY
        if (friends.isEmpty()) {
            nvg.drawText(
                TranslateText.CHAT_NO_FRIENDS.getText(),
                leftX + 14f,
                friendY + 10f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                9f,
                Fonts.REGULAR
            )
        } else {
            nvg.save()
            nvg.scissor(leftX + 6f, friendsStartY - 6f, leftWidth - 12f, friendsAreaHeight + 12f)
            val friendScrollOffset = friendScroll.getValue()
            nvg.translate(0f, friendScrollOffset)

            for (friend in friends) {
                val hovered = MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    leftX + 10f,
                    friendY + friendScrollOffset,
                    leftWidth - 20f,
                    friendRowHeight
                )
                val active = selectedFriend?.uuid == friend.uuid
                drawFriendRow(
                    nvg,
                    palette,
                    accent,
                    friend,
                    leftX + 10f,
                    friendY,
                    leftWidth - 20f,
                    friendRowHeight,
                    hovered,
                    active
                )
                friendEntries.add(
                    FriendEntry(
                        friend,
                        leftX + 10f,
                        friendY + friendScrollOffset,
                        leftWidth - 20f,
                        friendRowHeight,
                        leftX + leftWidth - 38f,
                        friendY + friendScrollOffset + 9f,
                        18f,
                        18f
                    )
                )
                friendY += friendRowHeight + 8f
            }
            nvg.restore()
        }

        val friendContentHeight = max(0f, friendY - friendsStartY)
        friendScroll.maxScroll = max(0f, friendContentHeight - friendsAreaHeight)

        if (MouseUtils.isInside(mouseX, mouseY, leftX, friendsStartY, leftWidth, friendsAreaHeight)) {
            friendScroll.onScroll()
        }
        friendScroll.onAnimation()

        drawChatPanel(nvg, palette, accent, mouseX, mouseY, partialTicks, rightX, topY, rightWidth, panelHeight)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val instance = Shindo.getInstance()
        val chatManager = instance.chatManager
        if (!chatManager.isFeatureAvailable()) {
            return
        }

        addFriendBox.mouseClicked(mouseX, mouseY, mouseButton)
        messageBox.mouseClicked(mouseX, mouseY, mouseButton)

        if (mouseButton == 0) {
            for (request in requestEntries) {
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        request.acceptX,
                        request.acceptY,
                        request.acceptW,
                        request.acceptH
                    )
                ) {
                    chatManager.acceptFriendRequest(request.request.uuid) { }
                    return
                }
            }
            for (entry in friendEntries) {
                if (MouseUtils.isInside(mouseX, mouseY, entry.removeX, entry.removeY, entry.removeW, entry.removeH)) {
                    chatManager.removeFriend(entry.friend.uuid) { }
                    if (selectedFriend?.uuid == entry.friend.uuid) {
                        selectedFriend = null
                    }
                    return
                }
                if (MouseUtils.isInside(mouseX, mouseY, entry.x, entry.y, entry.width, entry.height)) {
                    selectedFriend = entry.friend
                    chatScroll.resetAll()
                    return
                }
            }

            if (MouseUtils.isInside(mouseX, mouseY, addButtonX, addButtonY, addButtonW, addButtonH)) {
                requestFriend()
                return
            }
            if (MouseUtils.isInside(mouseX, mouseY, sendButtonX, sendButtonY, sendButtonW, sendButtonH)) {
                sendMessage()
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (parent != null) {
                mc.displayGuiScreen(parent)
            } else {
                mc.displayGuiScreen(null)
                mc.setIngameFocus()
            }
            return
        }

        addFriendBox.keyTyped(typedChar, keyCode)
        messageBox.keyTyped(typedChar, keyCode)

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            if (addFriendBox.isFocused()) {
                requestFriend()
            } else if (messageBox.isFocused()) {
                sendMessage()
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun drawUnavailable(nvg: NanoVGManager, palette: ColorPalette) {
        val text = TranslateText.CHAT_FEATURE_UNAVAILABLE.getText()
        nvg.drawCenteredText(
            text,
            x + menuWidth / 2f,
            y + menuHeight / 2f,
            palette.getFontColor(ColorType.NORMAL),
            12f,
            Fonts.MEDIUM
        )
    }

    private fun drawPanel(nvg: NanoVGManager, x: Float, y: Float, width: Float, height: Float, palette: ColorPalette) {
        nvg.drawRoundedRect(x, y, width, height, 12f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawRoundedRect(
            x + 1f,
            y + 1f,
            width - 2f,
            height - 2f,
            11f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230)
        )
    }

    private fun drawIconButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        icon: String,
        mouseX: Int,
        mouseY: Int
    ) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        val bg = if (hovered) ColorUtils.applyAlpha(accent.getColor1(), 200) else ColorUtils.applyAlpha(
            accent.getColor1(),
            160
        )
        nvg.drawRoundedRect(x, y, width, height, 6f, bg)
        nvg.drawCenteredText(
            icon,
            x + width / 2f,
            y + height / 2f - 4f,
            palette.getFontColor(ColorType.DARK),
            10f,
            Fonts.LEGACYICON
        )
    }

    private fun drawRequestRow(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        request: ChatRequest,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            8f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), if (hovered) 200 else 180)
        )
        val label = nvg.getLimitText(request.name, 10f, Fonts.MEDIUM, width - 80f)
        nvg.drawText(label, x + 10f, y + 9f, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)

        val acceptW = 60f
        val acceptH = 18f
        val acceptX = x + width - acceptW - 8f
        val acceptY = y + (height - acceptH) / 2f
        val acceptHovered = MouseUtils.isInside(mouseX, mouseY, acceptX, acceptY, acceptW, acceptH)
        val acceptBg = if (acceptHovered) ColorUtils.applyAlpha(
            accent.getColor1(),
            210
        ) else ColorUtils.applyAlpha(accent.getColor1(), 170)
        nvg.drawRoundedRect(acceptX, acceptY, acceptW, acceptH, 6f, acceptBg)
        nvg.drawCenteredText(
            TranslateText.CHAT_ACCEPT.getText(),
            acceptX + acceptW / 2f,
            acceptY + acceptH / 2f - 4f,
            palette.getFontColor(ColorType.DARK),
            9f,
            Fonts.MEDIUM
        )

        requestEntries.add(RequestEntry(request, acceptX, acceptY, acceptW, acceptH))
    }

    private fun drawFriendRow(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        friend: ChatFriend,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        hovered: Boolean,
        active: Boolean
    ) {
        val base = if (active) {
            ColorUtils.applyAlpha(accent.getColor1(), 120)
        } else {
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), if (hovered) 200 else 160)
        }
        nvg.drawRoundedRect(x, y, width, height, 8f, base)

        val head = PlayerHeadUtils.getOrRequest(friend.name)
        if (head != null) {
            nvg.drawPlayerHead(head, x + 8f, y + 6f, 24f, 24f, 8f)
        } else {
            nvg.drawRoundedRect(
                x + 8f,
                y + 6f,
                24f,
                24f,
                8f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 200)
            )
            nvg.drawCenteredText(
                LegacyIcon.USER,
                x + 20f,
                y + 12f,
                palette.getFontColor(ColorType.DARK),
                10f,
                Fonts.LEGACYICON
            )
        }

        val textX = x + 40f
        val label = nvg.getLimitText(friend.name, 10f, Fonts.MEDIUM, width - 70f)
        nvg.drawText(label, textX, y + 14f, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)

        nvg.drawRoundedRect(
            x + width - 28f,
            y + 9f,
            18f,
            18f,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 200)
        )
        nvg.drawCenteredText(LegacyIcon.TRASH, x + width - 19f, y + 13f, palette.getMaterialRed(), 9f, Fonts.LEGACYICON)
    }

    private fun drawChatPanel(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val chatManager = Shindo.getInstance().chatManager
        val selfUuid = Shindo.getInstance().shindoAPI.getEffectiveUuid().toString()
        val headerY = y + 14f

        val headerText = selectedFriend?.name ?: TranslateText.CHAT_SELECT_FRIEND.getText()
        nvg.drawText(headerText, x + 16f, headerY, palette.getFontColor(ColorType.DARK), 13f, Fonts.SEMIBOLD)
        nvg.drawRect(
            x + 16f,
            headerY + 18f,
            width - 32f,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 80)
        )

        val inputHeight = 28f
        val inputY = y + height - inputHeight - 14f
        val messagesTop = headerY + 24f
        val messagesHeight = inputY - messagesTop - 10f

        if (selectedFriend == null) {
            nvg.drawCenteredText(
                TranslateText.CHAT_SELECT_FRIEND.getText(),
                x + width / 2f,
                y + height / 2f,
                palette.getFontColor(ColorType.NORMAL),
                11f,
                Fonts.REGULAR
            )
        }

        nvg.save()
        nvg.scissor(x + 8f, messagesTop - 4f, width - 16f, messagesHeight + 8f)
        val scrollOffset = chatScroll.getValue()
        nvg.translate(0f, scrollOffset)

        var msgY = messagesTop
        val messages = selectedFriend?.let { chatManager.getMessages(it.uuid) } ?: emptyList()
        val bubbleMaxWidth = width - 100f
        for (message in messages) {
            val isOwn = message.fromUuid == selfUuid
            val textWidth = bubbleMaxWidth - 24f
            val textHeight = nvg.getTextBoxHeight(message.message, 9.5f, Fonts.REGULAR, textWidth)
            val bubbleHeight = textHeight + 16f
            val bubbleX = if (isOwn) x + width - bubbleMaxWidth - 20f else x + 20f
            val bubbleColor = if (isOwn) ColorUtils.applyAlpha(
                accent.getColor1(),
                200
            ) else ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 220)
            nvg.drawRoundedRect(bubbleX, msgY, bubbleMaxWidth, bubbleHeight, 12f, bubbleColor)
            nvg.drawTextBox(
                message.message,
                bubbleX + 12f,
                msgY + 10f,
                textWidth,
                palette.getFontColor(ColorType.DARK),
                9.5f,
                Fonts.REGULAR
            )
            msgY += bubbleHeight + 10f
        }
        nvg.restore()

        val contentHeight = max(0f, msgY - messagesTop)
        chatScroll.maxScroll = max(0f, contentHeight - messagesHeight)

        if (MouseUtils.isInside(mouseX, mouseY, x, messagesTop, width, messagesHeight)) {
            chatScroll.onScroll()
        }
        chatScroll.onAnimation()

        messageBox.setDefaultText(TranslateText.CHAT_MESSAGE_PLACEHOLDER.getText())
        messageBox.setPosition(x + 16f, inputY, width - 80f, inputHeight)
        messageBox.draw(mouseX, mouseY, partialTicks)

        sendButtonW = 48f
        sendButtonH = inputHeight
        sendButtonX = x + width - sendButtonW - 16f
        sendButtonY = inputY
        drawTextButton(
            nvg,
            palette,
            accent,
            sendButtonX,
            sendButtonY,
            sendButtonW,
            sendButtonH,
            TranslateText.CHAT_SEND.getText(),
            mouseX,
            mouseY
        )
    }

    private fun drawTextButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        mouseX: Int,
        mouseY: Int
    ) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        val bg = if (hovered) ColorUtils.applyAlpha(accent.getColor1(), 210) else ColorUtils.applyAlpha(
            accent.getColor1(),
            170
        )
        nvg.drawRoundedRect(x, y, width, height, 6f, bg)
        nvg.drawCenteredText(
            label,
            x + width / 2f,
            y + height / 2f - 4f,
            palette.getFontColor(ColorType.DARK),
            9.5f,
            Fonts.MEDIUM
        )
    }

    private fun requestFriend() {
        val chatManager = Shindo.getInstance().chatManager
        val username = addFriendBox.getText().trim()
        if (username.isEmpty()) {
            return
        }
        chatManager.requestFriend(username) {
            addFriendBox.setText("")
        }
    }

    private fun sendMessage() {
        val friend = selectedFriend ?: return
        val text = messageBox.getText().trim()
        if (text.isEmpty()) {
            return
        }
        val chatManager = Shindo.getInstance().chatManager
        chatManager.sendMessage(friend.uuid, text) { result ->
            if (result is me.miki.shindo.api.chat.ChatManager.MessageSendResult.Success) {
                messageBox.setText("")
            }
        }
    }

    private data class FriendEntry(
        val friend: ChatFriend,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val removeX: Float,
        val removeY: Float,
        val removeW: Float,
        val removeH: Float
    )

    private data class RequestEntry(
        val request: ChatRequest,
        val acceptX: Float,
        val acceptY: Float,
        val acceptW: Float,
        val acceptH: Float
    )
}
