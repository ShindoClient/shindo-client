package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.autotext.AutoTextEntry
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.AutoTextMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.easing.EaseBackIn
import me.miki.shindo.ui.animation.v2.screen.ScreenAnimation
import me.miki.shindo.ui.components.v2.inputs.CompAutoTextKeybind
import me.miki.shindo.ui.components.v2.inputs.CompTextBox
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiAutoTextManager(private val parent: GuiScreen?) : GuiScreen() {

    private data class AutoTextCard(
        val entry: AutoTextEntry,
        val nameBox: CompTextBox = CompTextBox(),
        val textBox: CompTextBox = CompTextBox(),
        val keybindComp: CompAutoTextKeybind,
        var editing: Boolean = false
    )

    private val manager = AutoTextMod.instance.autoTextManager
    private val cards = ArrayList<AutoTextCard>()
    private val scroll = Scroll()
    private val screenAnimation = ScreenAnimation()
    private lateinit var introAnimation: Animation

    private var x = 0
    private var y = 0
    private var menuWidth = 460
    private var menuHeight = 280

    override fun initGui() {
        val sr = ScaledResolution(mc)
        x = (sr.scaledWidth / 2) - (menuWidth / 2)
        y = (sr.scaledHeight / 2) - (menuHeight / 2)
        introAnimation = EaseBackIn(320, 1.0, 1.8f)
        introAnimation.setDirection(Direction.FORWARDS)
        scroll.resetAll()
        rebuildCards()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        BlurUtils.drawBlurScreen(20f)
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        val progress = introAnimation.getValueFloat().coerceIn(0f, 1f)
        screenAnimation.wrap(
            Runnable { drawContent(nvg, mouseX, mouseY, partialTicks) },
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            2f - progress,
            progress,
            false
        )
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            closeGui()
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawContent(nvg: NanoVGManager, mouseX: Int, mouseY: Int, partialTicks: Float) {
        nvg.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Color(0, 0, 0, 120))
        nvg.drawShadow(x.toFloat(), y.toFloat(), menuWidth.toFloat(), menuHeight.toFloat(), 12f)
        nvg.drawRoundedRect(
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            10f,
            Color(28, 28, 28, 225)
        )
        nvg.drawRoundedRect(x + 1f, y + 1f, menuWidth - 2f, menuHeight - 2f, 9f, Color(40, 40, 40, 220))

        nvg.drawText(TranslateText.AUTO_TEXT.getText(), x + 10f, y + 10f, Color.WHITE, 13f, Fonts.MEDIUM)

        val addX = x + menuWidth - 30f
        val addY = y + 7f
        val addHovered = MouseUtils.isInside(mouseX, mouseY, addX, addY, 20f, 20f)
        nvg.drawRoundedRect(
            addX,
            addY,
            20f,
            20f,
            5f,
            if (addHovered) Color(255, 255, 255, 70) else Color(255, 255, 255, 45)
        )
        nvg.drawCenteredText(LegacyIcon.PLUS, addX + 10f, addY + 6.5f, Color.WHITE, 9.5f, Fonts.LEGACYICON)

        val listX = x + 8f
        val listY = y + 34f
        val listWidth = menuWidth - 16f
        val listHeight = menuHeight - 42f
        val cardHeight = 86f
        val cardGap = 8f

        nvg.save()
        nvg.scissor(listX, listY, listWidth, listHeight)
        nvg.translate(0f, scroll.getValue())

        var offsetY = 0f
        for (card in cards) {
            drawCard(nvg, card, listX, listY + offsetY, listWidth, cardHeight, mouseX, mouseY, partialTicks)
            offsetY += cardHeight + cardGap
        }

        nvg.restore()
        scroll.maxScroll = kotlin.math.max(0f, offsetY - listHeight)
        if (MouseUtils.isInside(mouseX, mouseY, listX, listY, listWidth, listHeight)) {
            scroll.onScroll()
        }
        scroll.onAnimation()
    }

    private fun drawCard(
        nvg: NanoVGManager,
        card: AutoTextCard,
        cardX: Float,
        cardY: Float,
        cardWidth: Float,
        cardHeight: Float,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 8f, Color(255, 255, 255, 20))

        val deleteSize = 18f
        val actionSize = 18f
        val actionGap = 5f
        val deleteX = cardX + cardWidth - 10f - deleteSize
        val deleteY = cardY + (cardHeight - deleteSize) / 2f
        val actionX = deleteX - actionGap - actionSize
        val actionY = deleteY
        val contentWidth = cardWidth - 16f - actionSize - actionGap - deleteSize - 12f

        if (card.editing) {
            card.nameBox.setDefaultText(TranslateText.NAME.getText())
            card.nameBox.setPosition(cardX + 8f, cardY + 8f, contentWidth, 20f)
            card.nameBox.draw(mouseX, mouseY, partialTicks)

            card.textBox.setDefaultText(TranslateText.TEXT.getText())
            card.textBox.setPosition(cardX + 8f, cardY + 34f, contentWidth, 20f)
            card.textBox.draw(mouseX, mouseY, partialTicks)

            card.keybindComp.setPosition(cardX + 8f, cardY + 60f)
            card.keybindComp.setWidth(72f)
            card.keybindComp.draw(mouseX, mouseY, partialTicks)
        } else {
            nvg.drawText(
                if (card.entry.name.isBlank()) TranslateText.NAME.getText() else card.entry.name,
                cardX + 8f,
                cardY + 10f,
                Color.WHITE,
                9.5f,
                Fonts.MEDIUM
            )
            nvg.drawText(
                if (card.entry.textOrCommand.isBlank()) TranslateText.TEXT.getText() else card.entry.textOrCommand,
                cardX + 8f,
                cardY + 35f,
                Color(235, 235, 235, 235),
                9f,
                Fonts.REGULAR
            )
            nvg.drawRoundedRect(cardX + 8f, cardY + 58f, 72f, 16f, 4f, Color(255, 255, 255, 26))
            nvg.drawCenteredText(
                Keyboard.getKeyName(card.entry.keyCode),
                cardX + 44f,
                cardY + 63.5f,
                Color.WHITE,
                8f,
                Fonts.REGULAR
            )
        }

        val actionHovered = MouseUtils.isInside(mouseX, mouseY, actionX, actionY, actionSize, actionSize)
        val deleteHovered = MouseUtils.isInside(mouseX, mouseY, deleteX, deleteY, deleteSize, deleteSize)

        nvg.drawRoundedRect(
            actionX,
            actionY,
            actionSize,
            actionSize,
            4f,
            if (actionHovered) Color(255, 255, 255, 70) else Color(255, 255, 255, 45)
        )
        nvg.drawRoundedRect(
            deleteX,
            deleteY,
            deleteSize,
            deleteSize,
            4f,
            if (deleteHovered) Color(255, 70, 70, 95) else Color(255, 70, 70, 70)
        )
        nvg.drawCenteredText(
            if (card.editing) LegacyIcon.CHECK else LegacyIcon.PENCIL,
            actionX + actionSize / 2f,
            actionY + 6.5f,
            Color.WHITE,
            9f,
            Fonts.LEGACYICON
        )
        nvg.drawCenteredText(
            LegacyIcon.TRASH,
            deleteX + deleteSize / 2f,
            deleteY + 6.5f,
            Color.WHITE,
            9f,
            Fonts.LEGACYICON
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            for (card in cards) {
                if (card.editing) {
                    card.nameBox.mouseClicked(mouseX, mouseY, mouseButton)
                    card.textBox.mouseClicked(mouseX, mouseY, mouseButton)
                    card.keybindComp.mouseClicked(mouseX, mouseY, mouseButton)
                }
            }
            return
        }

        val addX = x + menuWidth - 30f
        val addY = y + 7f
        if (MouseUtils.isInside(mouseX, mouseY, addX, addY, 20f, 20f)) {
            val entry = manager.createEntry()
            rebuildCards()
            setEditingById(entry.id, true)
            return
        }

        val listX = x + 8f
        val listY = y + 34f
        val listWidth = menuWidth - 16f
        val cardHeight = 86f
        val cardGap = 8f

        var offsetY = 0f
        var removeId: String? = null
        for (card in cards) {
            val cardY = listY + offsetY + scroll.getValue()
            val deleteSize = 18f
            val actionSize = 18f
            val actionGap = 5f
            val deleteX = listX + listWidth - 10f - deleteSize
            val deleteY = cardY + (cardHeight - deleteSize) / 2f
            val actionX = deleteX - actionGap - actionSize
            val actionY = deleteY

            if (MouseUtils.isInside(mouseX, mouseY, deleteX, deleteY, deleteSize, deleteSize)) {
                removeId = card.entry.id
                break
            }

            if (MouseUtils.isInside(mouseX, mouseY, actionX, actionY, actionSize, actionSize)) {
                if (card.editing) {
                    card.entry.name = card.nameBox.getText().trim()
                    card.entry.textOrCommand = card.textBox.getText().trim()
                    card.nameBox.setFocused(false)
                    card.textBox.setFocused(false)
                    card.editing = false
                    manager.save()
                } else {
                    setEditingById(card.entry.id, true)
                }
                return
            }

            if (card.editing) {
                card.nameBox.mouseClicked(mouseX, mouseY, mouseButton)
                card.textBox.mouseClicked(mouseX, mouseY, mouseButton)
                card.keybindComp.mouseClicked(mouseX, mouseY, mouseButton)
            }

            offsetY += cardHeight + cardGap
        }

        if (removeId != null) {
            manager.removeEntry(removeId)
            rebuildCards()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            val anyBinding = cards.any { it.editing && it.keybindComp.isBinding() }
            if (anyBinding) {
                for (card in cards) {
                    if (card.editing) {
                        card.keybindComp.keyTyped(typedChar, keyCode)
                    }
                }
                return
            }

            val anyEditing = cards.any { it.editing }
            if (anyEditing) {
                cancelAllEditing()
                return
            }

            introAnimation.setDirection(Direction.BACKWARDS)
            return
        }

        for (card in cards) {
            if (card.editing) {
                card.nameBox.keyTyped(typedChar, keyCode)
                card.textBox.keyTyped(typedChar, keyCode)
                card.keybindComp.keyTyped(typedChar, keyCode)
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun closeGui() {
        mc.displayGuiScreen(parent)
        if (parent == null) {
            mc.setIngameFocus()
        }
    }

    private fun cancelAllEditing() {
        for (card in cards) {
            if (card.editing) {
                card.nameBox.setText(card.entry.name)
                card.textBox.setText(card.entry.textOrCommand)
                card.nameBox.setFocused(false)
                card.textBox.setFocused(false)
                card.editing = false
            }
        }
    }

    private fun rebuildCards() {
        val map = cards.associateBy { it.entry.id }
        cards.clear()

        for (entry in manager.getEntries()) {
            val old = map[entry.id]
            val card = if (old != null) {
                old
            } else {
                AutoTextCard(
                    entry = entry,
                    keybindComp = CompAutoTextKeybind(
                        72f,
                        { entry.keyCode },
                        {
                            entry.keyCode = it
                            manager.save()
                        }
                    )
                )
            }
            card.nameBox.setText(entry.name)
            card.textBox.setText(entry.textOrCommand)
            cards.add(card)
        }
    }

    private fun setEditingById(id: String, editing: Boolean) {
        for (card in cards) {
            val isTarget = card.entry.id == id
            card.editing = isTarget && editing
            if (card.editing) {
                card.nameBox.setText(card.entry.name)
                card.textBox.setText(card.entry.textOrCommand)
            } else {
                card.nameBox.setFocused(false)
                card.textBox.setFocused(false)
            }
        }
    }
}
