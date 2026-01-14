package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.api.chat.ChatFriend
import me.miki.shindo.api.chat.ChatRequest
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.impl.field.CompTextBox
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.PlayerHeadUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class ChatCategory(parent: GuiModMenu) : Category(parent, TranslateText.CHAT_FRIENDS, LegacyIcon.USERS, false, true) {

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

    override fun initGui() {
        friendScroll.resetAll()
        chatScroll.resetAll()
        addFriendBox.setText("")
        messageBox.setText("")
    }

    override fun initCategory() {
        friendScroll.resetAll()
        chatScroll.resetAll()
        addFriendBox.setText("")
        messageBox.setText("")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val chatManager = instance.chatManager
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.colorManager.palette
        val accent = instance.colorManager.currentColor

        if (!chatManager.isFeatureAvailable()) {
            drawUnavailable(nvg, palette)
            return
        }

        val padding = 16f
        val columnGap = 18f
        val leftWidth = 230f
        val leftX = getX() + padding
        val topY = getY() + padding
        val rightX = leftX + leftWidth + columnGap
        val rightWidth = getWidth() - leftWidth - columnGap - padding * 2
        val panelHeight = getHeight() - padding * 2

        friendEntries.clear()
        requestEntries.clear()

        drawPanel(nvg, leftX, topY, leftWidth, panelHeight, palette)
        drawPanel(nvg, rightX, topY, rightWidth, panelHeight, palette)

        val friends = chatManager.getFriends()
        if (selectedFriend != null && friends.none { it.uuid == selectedFriend?.uuid }) {
            selectedFriend = null
        }

        val leftHeaderY = topY + 16f
        nvg.drawText(TranslateText.CHAT_FRIENDS.text, leftX + 14f, leftHeaderY, palette.getFontColor(ColorType.DARK), 13f, Fonts.SEMIBOLD)

        val addY = leftHeaderY + 16f
        addFriendBox.setDefaultText(TranslateText.CHAT_ADD_FRIEND_PLACEHOLDER.text)
        addFriendBox.setPosition(leftX + 14f, addY, leftWidth - 64f, 20f)
        addFriendBox.draw(mouseX, mouseY, partialTicks)

        addButtonW = 32f
        addButtonH = 20f
        addButtonX = leftX + leftWidth - addButtonW - 14f
        addButtonY = addY
        drawIconButton(nvg, palette, accent, addButtonX, addButtonY, addButtonW, addButtonH, LegacyIcon.PLUS, mouseX, mouseY)

        val requestsTitleY = addY + 30f
        nvg.drawText(TranslateText.CHAT_REQUESTS.text, leftX + 14f, requestsTitleY, palette.getFontColor(ColorType.NORMAL), 10.5f, Fonts.MEDIUM)

        var currentY = requestsTitleY + 12f
        val requestHeight = 30f
        val requests = chatManager.getRequests()
        if (requests.isEmpty()) {
            nvg.drawText(TranslateText.CHAT_NO_REQUESTS.text, leftX + 14f, currentY + 10f, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 9f, Fonts.REGULAR)
            currentY += 16f
        } else {
            for (request in requests) {
                drawRequestRow(nvg, palette, accent, request, leftX + 10f, currentY, leftWidth - 20f, requestHeight, mouseX, mouseY)
                currentY += requestHeight + 8f
            }
        }

        val friendsTitleY = currentY + 12f
        nvg.drawText(TranslateText.CHAT_FRIENDS.text, leftX + 14f, friendsTitleY, palette.getFontColor(ColorType.NORMAL), 10.5f, Fonts.MEDIUM)

        val friendsStartY = friendsTitleY + 12f
        val friendsAreaHeight = topY + panelHeight - friendsStartY - 12f
        val friendRowHeight = 38f

        var friendY = friendsStartY
        if (friends.isEmpty()) {
            nvg.drawText(
                TranslateText.CHAT_NO_FRIENDS.text,
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
                val hovered = MouseUtils.isInside(mouseX, mouseY, leftX + 10f, friendY + friendScrollOffset, leftWidth - 20f, friendRowHeight)
                val active = selectedFriend?.uuid == friend.uuid
                drawFriendRow(nvg, palette, accent, friend, leftX + 10f, friendY, leftWidth - 20f, friendRowHeight, hovered, active)
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
                if (MouseUtils.isInside(mouseX, mouseY, request.acceptX, request.acceptY, request.acceptW, request.acceptH)) {
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

    private fun drawUnavailable(nvg: NanoVGManager, palette: ColorPalette) {
        val text = TranslateText.CHAT_FEATURE_UNAVAILABLE.text
        val centerX = getX() + getWidth() / 2f
        val centerY = getY() + getHeight() / 2f
        nvg.drawCenteredText(text, centerX, centerY, palette.getFontColor(ColorType.NORMAL), 12f, Fonts.MEDIUM)
    }

    private fun drawPanel(nvg: NanoVGManager, x: Float, y: Float, width: Float, height: Float, palette: ColorPalette) {
        nvg.drawRoundedRect(x, y, width, height, 12f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawRoundedRect(x + 1f, y + 1f, width - 2f, height - 2f, 11f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))
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
        val bg = if (hovered) ColorUtils.applyAlpha(accent.color1, 200) else ColorUtils.applyAlpha(accent.color1, 160)
        nvg.drawRoundedRect(x, y, width, height, 6f, bg)
        nvg.drawCenteredText(icon, x + width / 2f, y + height / 2f - 4f, palette.getFontColor(ColorType.DARK), 10f, Fonts.LEGACYICON)
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
        nvg.drawRoundedRect(x, y, width, height, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), if (hovered) 200 else 180))
        val label = nvg.getLimitText(request.name, 10f, Fonts.MEDIUM, width - 80f)
        nvg.drawText(label, x + 10f, y + 9f, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)

        val acceptW = 60f
        val acceptH = 18f
        val acceptX = x + width - acceptW - 8f
        val acceptY = y + (height - acceptH) / 2f
        val acceptHovered = MouseUtils.isInside(mouseX, mouseY, acceptX, acceptY, acceptW, acceptH)
        val acceptBg = if (acceptHovered) ColorUtils.applyAlpha(accent.color1, 210) else ColorUtils.applyAlpha(accent.color1, 170)
        nvg.drawRoundedRect(acceptX, acceptY, acceptW, acceptH, 6f, acceptBg)
        nvg.drawCenteredText(TranslateText.CHAT_ACCEPT.text, acceptX + acceptW / 2f, acceptY + acceptH / 2f - 4f, palette.getFontColor(ColorType.DARK), 9f, Fonts.MEDIUM)

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
        val base = if (active) ColorUtils.applyAlpha(accent.color1, 120) else ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), if (hovered) 200 else 160)
        nvg.drawRoundedRect(x, y, width, height, 8f, base)

        val head = PlayerHeadUtils.getOrRequest(friend.name)
        if (head != null) {
            nvg.drawPlayerHead(head, x + 8f, y + 6f, 24f, 24f, 8f)
        } else {
            nvg.drawRoundedRect(x + 8f, y + 6f, 24f, 24f, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 200))
            nvg.drawCenteredText(LegacyIcon.USER, x + 20f, y + 12f, palette.getFontColor(ColorType.DARK), 10f, Fonts.LEGACYICON)
        }

        val textX = x + 40f
        val label = nvg.getLimitText(friend.name, 10f, Fonts.MEDIUM, width - 70f)
        nvg.drawText(label, textX, y + 14f, palette.getFontColor(ColorType.DARK), 10f, Fonts.MEDIUM)

        nvg.drawRoundedRect(x + width - 28f, y + 9f, 18f, 18f, 6f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 200))
        nvg.drawCenteredText(LegacyIcon.TRASH, x + width - 19f, y + 13f, palette.materialRed, 9f, Fonts.LEGACYICON)
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
        val headerY = y + 16f

        val headerText = selectedFriend?.name ?: TranslateText.CHAT_SELECT_FRIEND.text
        nvg.drawText(headerText, x + 16f, headerY, palette.getFontColor(ColorType.DARK), 13f, Fonts.SEMIBOLD)

        val inputHeight = 24f
        val inputY = y + height - inputHeight - 16f
        val messagesTop = headerY + 18f
        val messagesHeight = inputY - messagesTop - 10f

        if (selectedFriend == null) {
            nvg.drawCenteredText(TranslateText.CHAT_SELECT_FRIEND.text, x + width / 2f, y + height / 2f, palette.getFontColor(ColorType.NORMAL), 11f, Fonts.REGULAR)
        }

        nvg.save()
        nvg.scissor(x + 8f, messagesTop - 4f, width - 16f, messagesHeight + 8f)
        val scrollOffset = chatScroll.getValue()
        nvg.translate(0f, scrollOffset)

        var msgY = messagesTop
        val messages = selectedFriend?.let { chatManager.getMessages(it.uuid) } ?: emptyList()
        val bubbleMaxWidth = width - 90f
        for (message in messages) {
            val isOwn = message.fromUuid == selfUuid
            val bubbleWidth = bubbleMaxWidth
            val textWidth = bubbleWidth - 20f
            val textHeight = nvg.getTextBoxHeight(message.message, 9.5f, Fonts.REGULAR, textWidth)
            val bubbleHeight = textHeight + 14f
            val bubbleX = if (isOwn) x + width - bubbleWidth - 16f else x + 16f
            val bubbleColor = if (isOwn) ColorUtils.applyAlpha(accent.color1, 180) else ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200)
            nvg.drawRoundedRect(bubbleX, msgY, bubbleWidth, bubbleHeight, 10f, bubbleColor)
            nvg.drawTextBox(message.message, bubbleX + 10f, msgY + 8f, textWidth, palette.getFontColor(ColorType.DARK), 9.5f, Fonts.REGULAR)
            msgY += bubbleHeight + 8f
        }
        nvg.restore()

        val contentHeight = max(0f, msgY - messagesTop)
        chatScroll.maxScroll = max(0f, contentHeight - messagesHeight)

        if (MouseUtils.isInside(mouseX, mouseY, x, messagesTop, width, messagesHeight)) {
            chatScroll.onScroll()
        }
        chatScroll.onAnimation()

        messageBox.setDefaultText(TranslateText.CHAT_MESSAGE_PLACEHOLDER.text)
        messageBox.setPosition(x + 16f, inputY, width - 80f, inputHeight)
        messageBox.draw(mouseX, mouseY, partialTicks)

        sendButtonW = 48f
        sendButtonH = inputHeight
        sendButtonX = x + width - sendButtonW - 16f
        sendButtonY = inputY
        drawTextButton(nvg, palette, accent, sendButtonX, sendButtonY, sendButtonW, sendButtonH, TranslateText.CHAT_SEND.text, mouseX, mouseY)
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
        val bg = if (hovered) ColorUtils.applyAlpha(accent.color1, 210) else ColorUtils.applyAlpha(accent.color1, 170)
        nvg.drawRoundedRect(x, y, width, height, 6f, bg)
        nvg.drawCenteredText(label, x + width / 2f, y + height / 2f - 4f, palette.getFontColor(ColorType.DARK), 9.5f, Fonts.MEDIUM)
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
