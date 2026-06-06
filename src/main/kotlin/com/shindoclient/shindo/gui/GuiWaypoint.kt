package com.shindoclient.shindo.gui

import com.shindoclient.extensions.ui.animation.setAnimation
import com.shindoclient.extensions.ui.animation.wrap
import com.shindoclient.extensions.ui.nanovg.drawRoundedRect
import com.shindoclient.extensions.ui.nanovg.drawShadow
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.color.ColorManager
import com.shindoclient.shindo.management.color.palette.ColorPalette
import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.waypoint.Waypoint
import com.shindoclient.shindo.management.waypoint.WaypointManager
import com.shindoclient.shindo.ui.animation.v2.Animation
import com.shindoclient.shindo.ui.animation.v2.Direction
import com.shindoclient.shindo.ui.animation.v2.easing.EaseBackIn
import com.shindoclient.shindo.ui.animation.v2.screen.ScreenAnimation
import com.shindoclient.shindo.ui.components.v2.inputs.CompTextBox
import com.shindoclient.shindo.utils.ColorUtils
import com.shindoclient.shindo.utils.mouse.MouseUtils
import com.shindoclient.shindo.utils.mouse.Scroll
import com.shindoclient.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiWaypoint : GuiScreen() {
    private val scroll = Scroll()
    private val screenAnimation = ScreenAnimation()
    private val textBox = CompTextBox()
    private val colors: ArrayList<Color> = ArrayList()
    private lateinit var introAnimation: Animation
    private var x = 0
    private var y = 0
    private var menuWidth = 0
    private var menuHeight = 0
    private var removeWaypoint: Waypoint? = null
    private var currentColor: Color = Color.RED

    init {
        colors.add(Color.RED)
        colors.add(Color.GREEN)
        colors.add(Color.BLUE)
        colors.add(Color.ORANGE)
        colors.add(Color.YELLOW)
        colors.add(Color.MAGENTA)
        colors.add(Color.PINK)
        colors.add(Color.GRAY)
        colors.add(Color.DARK_GRAY)
    }

    override fun initGui() {
        val sr = ScaledResolution(mc)

        val addX = 160
        val addY = 80

        x = (sr.scaledWidth / 2) - addX
        y = (sr.scaledHeight / 2) - addY
        menuWidth = addX * 2
        menuHeight = addY * 2

        introAnimation = EaseBackIn(320, 1.0, 2.0f)
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        BlurUtils.drawBlurScreen(20F)

        screenAnimation.wrap(
            Runnable { drawNanoVG(mouseX, mouseY, partialTicks) },
            x,
            y,
            menuWidth,
            menuHeight,
            2 - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f),
            false,
        )
    }

    private fun drawNanoVG(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val waypointManager: WaypointManager = instance.getWaypointManager()
        val colorManager: ColorManager = instance.getColorManager()
        val palette: ColorPalette = colorManager.getPalette()

        var offsetX: Int
        var offsetY = 0
        var index = 0

        scroll.onScroll()
        scroll.onAnimation()

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(null)
        }

        nvg.drawShadow(x, y, menuWidth, menuHeight, 8f, 7)
        nvg.drawRoundedRect(
            x,
            y,
            menuWidth,
            menuHeight,
            8f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210),
        )
        nvg.drawRoundedRect(
            x + 1f,
            y + 1f,
            menuWidth - 2f,
            menuHeight - 2f,
            7f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230),
        )

        nvg.drawText("Waypoint", x + 8f, y + 8f, palette.getFontColor(ColorType.DARK), 13f, Fonts.MEDIUM)
        nvg.drawRoundedRect(x + 6, y + 25f, menuWidth - 12, 1.75f, 3f, palette.getBackgroundColor(ColorType.NORMAL))

        nvg.save()
        nvg.scissor(x.toFloat(), y + 25f, 190f, menuHeight - 25f)
        nvg.translate(0f, scroll.getValue())

        for (waypoint in waypointManager.getWaypoints()) {
            if (waypoint.getWorld() == waypointManager.getWorld()) {
                waypoint.getTrashAnimation().setAnimation(
                    if (MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            x + 162f,
                            y + 44f + offsetY + scroll.getValue(),
                            11f,
                            11f,
                        )
                    ) {
                        1.0f
                    } else {
                        0.0f
                    },
                    16,
                )

                nvg.drawShadow(x, y, width, height, 6f, 7)
                nvg.drawRoundedRect(
                    x + 10f,
                    y + 35f + offsetY,
                    170f,
                    28f,
                    6f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220),
                )
                nvg.drawOutlineRoundedRect(
                    x + 10f,
                    y + 35f + offsetY,
                    170f,
                    28f,
                    6f,
                    1f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
                )
                nvg.drawRoundedRect(x + 16f, y + 40f + offsetY, 18f, 18f, 4f, waypoint.getColor())
                nvg.drawText(
                    waypoint.getName(),
                    x + 40f,
                    y + 45.5f + offsetY,
                    palette.getFontColor(ColorType.DARK),
                    9.5f,
                    Fonts.REGULAR,
                )

                nvg.drawText(
                    Lucide.TRASH,
                    x + 162f,
                    y + 44f + offsetY,
                    Color(
                        255,
                        255 - (waypoint.getTrashAnimation().getValue() * 255).toInt(),
                        255 - (waypoint.getTrashAnimation().getValue() * 255).toInt(),
                    ),
                    11f,
                    Fonts.LUCIDE,
                )

                offsetY += 38
                index++
            }
        }

        nvg.restore()

        scroll.maxScroll = if (index < 3) 0f else (index - 3) * 66f

        nvg.drawShadow(x + menuWidth - 130f, y + 35f, 120f, menuHeight - 45f, 6f, 7)
        nvg.drawRoundedRect(
            x + menuWidth - 130f,
            y + 35f,
            120f,
            menuHeight - 45f,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220),
        )
        nvg.drawOutlineRoundedRect(
            x + menuWidth - 130f,
            y + 35f,
            120f,
            menuHeight - 45f,
            6f,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
        )

        nvg.drawCenteredText(
            "Create a waypoint",
            x + menuWidth - 130f + (120 / 2f),
            y + 43f,
            palette.getFontColor(ColorType.DARK),
            10.5f,
            Fonts.MEDIUM,
        )

        textBox.setDefaultText("Name")
        textBox.setPosition(x + menuWidth - 120f, y + 59f, 100f, 18f)
        textBox.draw(mouseX, mouseY, partialTicks)

        offsetX = 0
        offsetY = 0
        index = 0

        for (color in colors) {
            nvg.drawRoundedRect(x + menuWidth - 120f + offsetX, y + 84f + offsetY, 13f, 13f, 2f, color)

            if (currentColor == color) {
                nvg.drawText(
                    Lucide.CHECK,
                    x + menuWidth - 118f + offsetX,
                    y + 86.5f + offsetY,
                    Color.WHITE,
                    9f,
                    Fonts.LUCIDE,
                )
            }

            offsetX += 17
            index++

            if (index % 6 == 0) {
                offsetY += 17
                offsetX = 0
            }
        }

        nvg.drawRoundedRect(
            x + menuWidth - 85f,
            y + menuHeight - 34f,
            65f,
            18f,
            6f,
            palette.getBackgroundColor(ColorType.NORMAL),
        )
        nvg.drawCenteredText(
            "Save",
            x + menuWidth - 85f + (65 / 2f),
            y + menuHeight - 29f,
            palette.getFontColor(ColorType.DARK),
            9f,
            Fonts.REGULAR,
        )

        if (removeWaypoint != null) {
            waypointManager.getWaypoints().remove(removeWaypoint!!)
            removeWaypoint = null
            waypointManager.save()
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        val instance = Shindo.getInstance()
        val waypointManager: WaypointManager = instance.getWaypointManager()

        var offsetX: Int
        var offsetY = 0
        var index = 0

        for (waypoint in waypointManager.getWaypoints()) {
            if (waypoint.getWorld() == waypointManager.getWorld()) {
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        x + 160f,
                        y + 41f + offsetY + scroll.getValue().toInt(),
                        16f,
                        16f,
                    ) &&
                    mouseButton == 0
                ) {
                    removeWaypoint = waypoint
                }

                offsetY += 38
                index++
            }
        }

        offsetX = 0
        offsetY = 0
        index = 0

        for (color in colors) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    x + menuWidth - 120f + offsetX,
                    y + 84f + offsetY,
                    13f,
                    13f,
                ) &&
                mouseButton == 0
            ) {
                currentColor = color
            }

            offsetX += 17
            index++

            if (index % 6 == 0) {
                offsetY += 17
                offsetX = 0
            }
        }

        if (MouseUtils.isInside(
                mouseX,
                mouseY,
                x + menuWidth - 85f,
                y + menuHeight - 34f,
                65f,
                18f,
            ) &&
            mouseButton == 0 &&
            textBox.getText().isNotEmpty()
        ) {
            waypointManager.addWaypoint(
                textBox.getText(),
                mc.thePlayer.posX,
                mc.thePlayer.posY,
                mc.thePlayer.posZ,
                currentColor,
            )
            textBox.setText("")
            waypointManager.save()
        }

        textBox.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }

        textBox.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame(): Boolean = false
}
