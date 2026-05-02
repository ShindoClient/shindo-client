package me.miki.shindo.gui.mainmenu.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.v1.Animation
import me.miki.shindo.ui.animation.v1.Direction
import me.miki.shindo.ui.animation.v1.easing.EaseInOutCirc
import me.miki.shindo.ui.animation.v1.screen.ScreenAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color

class ShopScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAnimation = ScreenAnimation()
    private val goldFeatures: ArrayList<TranslateText> = ArrayList()
    private val info: TranslateText
    private lateinit var introAnimation: Animation

    init {
        goldFeatures.add(TranslateText.SPECIAL_BADGE)
        goldFeatures.add(TranslateText.SPECIAL_CAPE)

        info = TranslateText.PURCHASE
    }

    override fun initScene() {
        introAnimation = EaseInOutCirc(250, 1.0)
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager

        screenAnimation.wrap(
            Runnable { drawNanoVG(mouseX, mouseY, sr, instance, nvg) },
            0F,
            0F,
            sr.scaledWidth.toFloat(),
            sr.scaledHeight.toFloat(),
            2 - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f),
            false
        )
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(MainScene::class.java))
        }
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager?) {
        val acWidth = 220
        val acHeight = 190
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)

        var offsetY = 0

        val panelColor = getPanelColor()
        val controlColor = getControlColor()

        nvg!!.drawRoundedRect(
            acX.toFloat(),
            acY.toFloat(),
            acWidth.toFloat(),
            acHeight.toFloat(),
            8f,
            getBackgroundColor()
        )
        nvg.drawCenteredText(
            TranslateText.PRICING_PLANS.getText(),
            acX + (acWidth / 2f),
            acY + 12f,
            Color.WHITE,
            14f,
            Fonts.MEDIUM
        )
        nvg.drawCenteredText(
            TranslateText.PRICING_PLANS_DESCRIPTION.getText(),
            acX + (acWidth / 2f),
            acY + 30f,
            Color.WHITE,
            9f,
            Fonts.REGULAR
        )
        nvg.drawRoundedRect(acX + 20f, acY + 50f, 82f, 128f, 6f, panelColor)
        nvg.drawRoundedRect(acX + acWidth - 82f - 20f, acY + 50f, 82f, 128f, 6f, panelColor)

        nvg.drawCenteredText(
            TranslateText.PREMIUM.getText(),
            acX + 20f + (82 / 2f),
            acY + 58f,
            Color.WHITE,
            12f,
            Fonts.MEDIUM
        )

        nvg.drawCenteredText(
            "20$ / " + TranslateText.LIFETIME.getText(),
            acX + 20f + (82 / 2f),
            acY + 71f,
            Color.WHITE,
            8f,
            Fonts.REGULAR
        )
        nvg.drawRect(acX + 20f, acY + 80f, 82f, 1f, Color.WHITE)

        nvg.drawRoundedRect(acX + 25f, acY + 153f, 72f, 20f, 6f, controlColor)
        nvg.drawCenteredText(info.getText(), acX + 25f + (72 / 2f), acY + 159f, Color.WHITE, 10f, Fonts.MEDIUM)

        for (t in goldFeatures) {
            nvg.drawText(LegacyIcon.CHECK_CIRCLE, acX + 25f, acY + 87f + offsetY, Color.WHITE, 9f, Fonts.LEGACYICON)
            nvg.drawText(t.getText(), acX + 36f, acY + 88f + offsetY, Color.WHITE, 8f, Fonts.REGULAR)

            offsetY += 12
        }

        nvg.drawCenteredText(
            TranslateText.SOON.getText(),
            acX + acWidth - 82f - 20f + (82 / 2f),
            acY + 58f,
            Color.WHITE,
            12f,
            Fonts.MEDIUM
        )
        nvg.drawCenteredText(
            "?? / " + TranslateText.MONTH.getText(),
            acX + acWidth - 82f - 20f + (82 / 2f),
            acY + 71f,
            Color.WHITE,
            8f,
            Fonts.REGULAR
        )
        nvg.drawRect(acX + acWidth - 82f - 20f, acY + 80f, 82f, 1f, Color.WHITE)

        nvg.drawRoundedRect(acX + acWidth - 82f - 15f, acY + 153f, 72f, 20f, 6f, controlColor)
        nvg.drawCenteredText(
            TranslateText.SOON.getText(),
            acX + acWidth - 82f - 15f + (72 / 2f),
            acY + 159f,
            Color.WHITE,
            10f,
            Fonts.MEDIUM
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)

        val acWidth = 220
        val acHeight = 190
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)

        if (!MouseUtils.isInside(mouseX, mouseY, acX.toFloat(), acY.toFloat(), acWidth.toFloat(), acHeight.toFloat())
            && !MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 84f, 6f, 22f, 22f)
        ) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }
}
