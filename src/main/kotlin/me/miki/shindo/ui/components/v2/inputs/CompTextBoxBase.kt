package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.IOUtils
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class CompTextBoxBase : Comp {

    private var text: String

    private var enabled: Boolean
    private var focused: Boolean
    private var cursorPosition: Int = 0
    private var selectionEnd: Int = 0
    private var maxStringLength: Int

    fun getText(): String = text
    fun isEnabled(): Boolean = enabled
    fun isFocused(): Boolean = focused
    fun getCursorPosition(): Int = cursorPosition
    fun getSelectionEnd(): Int = selectionEnd
    fun getMaxStringLength(): Int = maxStringLength

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
        if (!enabled) {
            this.focused = false
        }
    }

    fun setFocused(focused: Boolean) {
        if (!enabled) {
            this.focused = false
            return
        }
        this.focused = focused
    }

    fun setSelectionEnd(selectionEnd: Int) {
        this.selectionEnd = selectionEnd
    }

    fun setMaxStringLength(maxStringLength: Int) {
        this.maxStringLength = maxStringLength
    }

    constructor(x: Float, y: Float, width: Float, height: Float) : super(x, y) {
        setWidth(width)
        setHeight(height)
        this.enabled = true
        this.focused = false
        this.text = ""
        this.maxStringLength = 256
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!enabled) {
            this.focused = false
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val flag = MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())

        this.setFocused(flag)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!enabled) {
            super.keyTyped(typedChar, keyCode)
            return
        }

        if (focused) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_C) {
                IOUtils.copyStringToClipboard(this.getSelectedText())
            } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_V) {
                writeText(IOUtils.getStringFromClipboard().toString())
            } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_X) {
                IOUtils.copyStringToClipboard(this.getSelectedText())
                this.writeText("")
            } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_A) {
                this.cursorPosition = this.text.length
                this.setSelectionPos(0)
            } else {
                when (keyCode) {
                    Keyboard.KEY_BACK -> {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            this.deleteWords(-1)
                        } else {
                            this.deleteFromCursor(-1)
                        }
                        return
                    }

                    Keyboard.KEY_HOME -> {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                            this.setSelectionPos(0)
                        } else {
                            this.setCursorPosition(0)
                        }
                        return
                    }

                    Keyboard.KEY_LEFT -> {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                this.setSelectionPos(this.getNthWordFromPos(-1, this.selectionEnd))
                            } else {
                                this.setSelectionPos(this.selectionEnd - 1)
                            }
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            this.setCursorPosition(this.getNthWordFromCursor(-1))
                        } else {
                            this.moveCursorBy(-1)
                        }
                        return
                    }

                    Keyboard.KEY_RIGHT -> {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                                this.setSelectionPos(this.getNthWordFromPos(1, this.selectionEnd))
                            } else {
                                this.setSelectionPos(this.selectionEnd + 1)
                            }
                        } else if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            this.setCursorPosition(this.getNthWordFromCursor(1))
                        } else {
                            this.moveCursorBy(1)
                        }
                        return
                    }

                    Keyboard.KEY_END -> {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                            this.setSelectionPos(this.text.length)
                        } else {
                            this.setCursorPosition(this.text.length)
                        }
                        return
                    }

                    Keyboard.KEY_DELETE -> {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                            this.deleteWords(1)
                        } else {
                            this.deleteFromCursor(1)
                        }
                        return
                    }

                    else -> if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        this.writeText(typedChar.toString())
                    }
                }
            }
        }

        super.keyTyped(typedChar, keyCode)
    }

    private fun writeText(text: String) {
        var result = ""
        val filtered = ChatAllowedCharacters.filterAllowedCharacters(text)
        val minIdx = min(this.cursorPosition, this.selectionEnd)
        val maxIdx = max(this.cursorPosition, this.selectionEnd)
        val len = this.maxStringLength - this.text.length - (minIdx - maxIdx)

        val allowed = if (len < filtered.length) len else filtered.length

        if (this.text.isNotEmpty()) {
            result += this.text.substring(0, minIdx)
        }

        result += filtered.substring(0, allowed)

        if (this.text.isNotEmpty() && maxIdx < this.text.length) {
            result += this.text.substring(maxIdx)
        }

        this.text = result
        this.moveCursorBy(minIdx - this.selectionEnd + allowed)
    }

    private fun deleteWords(num: Int) {
        if (this.text.isNotEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("")
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition)
            }
        }
    }

    private fun getSelectedText(): String {
        val minIdx = min(this.cursorPosition, this.selectionEnd)
        val maxIdx = max(this.cursorPosition, this.selectionEnd)
        return this.text.substring(minIdx, maxIdx)
    }

    private fun deleteFromCursor(num: Int) {
        if (this.text.isEmpty()) return

        if (this.selectionEnd != this.cursorPosition) {
            this.writeText("")
        } else {
            val negative = num < 0
            val i = if (negative) this.cursorPosition + num else this.cursorPosition
            val j = if (negative) this.cursorPosition else this.cursorPosition + num
            var result = ""

            if (i > 0) {
                result = this.text.substring(0, i)
            }

            if (j < this.text.length) {
                result += this.text.substring(j)
            }

            this.text = result

            if (negative) {
                this.moveCursorBy(num)
            }
        }
    }

    private fun getNthWordFromCursor(num: Int): Int = getNthWordFromPos(num, this.cursorPosition)

    private fun getNthWordFromPos(num: Int, pos: Int): Int {
        var i = pos
        val negative = num < 0
        val steps = abs(num)

        repeat(steps) {
            if (!negative) {
                val len = this.text.length
                i = this.text.indexOf(' ', i)

                if (i == -1) {
                    i = len
                } else {
                    while (i < len && this.text[i] == ' ') {
                        ++i
                    }
                }
            } else {
                while (i > 0 && this.text[i - 1] == ' ') {
                    --i
                }

                while (i > 0 && this.text[i - 1] != ' ') {
                    --i
                }
            }
        }

        return i
    }

    private fun moveCursorBy(offset: Int) {
        this.setCursorPosition(this.selectionEnd + offset)
    }

    private fun setSelectionPos(selectionPos: Int) {
        var selection = selectionPos
        val len = this.text.length

        if (selection > len) selection = len
        if (selection < 0) selection = 0

        this.selectionEnd = selection
    }

    open fun setPosition(x: Float, y: Float, width: Float, height: Float) {
        this.setX(x)
        this.setY(y)
        this.setWidth(width)
        this.setHeight(height)
    }

    fun setText(text: String) {
        this.text = text
        this.setCursorPosition(this.text.length)
    }

    private fun setCursorPosition(position: Int) {
        this.cursorPosition = position
        val len = this.text.length

        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, len)
        this.setSelectionPos(this.cursorPosition)
    }
}
