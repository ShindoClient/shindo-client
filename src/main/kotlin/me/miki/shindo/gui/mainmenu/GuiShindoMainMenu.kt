package me.miki.shindo.gui.mainmenu

import me.miki.shindo.Shindo
import me.miki.shindo.gui.IShindoScreen
import me.miki.shindo.gui.mainmenu.impl.BackgroundScene
import me.miki.shindo.gui.mainmenu.impl.MainScene
import me.miki.shindo.gui.mainmenu.impl.ShopScene
import me.miki.shindo.gui.mainmenu.impl.SkinScene
import me.miki.shindo.gui.mainmenu.impl.UpdateScene
import me.miki.shindo.gui.mainmenu.impl.welcome.AccentColorSelectScene
import me.miki.shindo.gui.mainmenu.impl.welcome.CheckingDataScene
import me.miki.shindo.gui.mainmenu.impl.welcome.LanguageSelectScene
import me.miki.shindo.gui.mainmenu.impl.welcome.LastMessageScene
import me.miki.shindo.gui.mainmenu.impl.welcome.ThemeSelectScene
import me.miki.shindo.gui.mainmenu.impl.welcome.WelcomeMessageScene
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.event.impl.EventRenderNotification
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.profile.mainmenu.impl.Background
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground
import me.miki.shindo.management.profile.mainmenu.impl.ShaderBackground
import me.miki.shindo.management.shader.ShaderBackgroundRenderer
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.Sound
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import java.awt.Color
import java.util.ArrayList

class GuiShindoMainMenu : GuiScreen(), IShindoScreen {

    private val scenes: ArrayList<MainMenuScene> = ArrayList()

    private val skinFocusAnimation = SimpleAnimation()
    private val shopFocusAnimation = SimpleAnimation()
    private val backgroundSelectFocusAnimation = SimpleAnimation()
    private val closeFocusAnimation = SimpleAnimation()

    private val backgroundAnimations = Array(2) { SimpleAnimation() }

    var currentScene: MainMenuScene? = null
        private set

    private var soundPlayed = false

    private var fadeIconAnimation: Animation? = null
    private var fadeBackgroundAnimation: Animation? = null

    init {
        val instance = Shindo.getInstance()
        val firstLogin = instance.shindoAPI.isFirstLogin()
        ensureDefaultColorScheme(instance, firstLogin)

        scenes.add(MainScene(this))
        scenes.add(BackgroundScene(this))
        scenes.add(ShopScene(this))
        scenes.add(SkinScene(this))
        scenes.add(UpdateScene(this))
        scenes.add(WelcomeMessageScene(this))
        scenes.add(LanguageSelectScene(this))
        scenes.add(ThemeSelectScene(this))
        scenes.add(AccentColorSelectScene(this))
        scenes.add(CheckingDataScene(this))
        scenes.add(LastMessageScene(this))

        currentScene = if (firstLogin) {
            getSceneByClass(WelcomeMessageScene::class.java)
        } else {
            if (instance.updateNeeded) {
                getSceneByClass(UpdateScene::class.java)
            } else {
                getSceneByClass(MainScene::class.java)
            }
        }
    }

    override fun initGui() {
        currentScene?.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        val isFirstLogin = instance.shindoAPI.isFirstLogin()

        backgroundAnimations[0].setAnimation(Mouse.getX().toFloat(), 16.0)
        backgroundAnimations[1].setAnimation(Mouse.getY().toFloat(), 16.0)

        nvg!!.setupAndDraw(Runnable {
            drawNanoVG(sr, instance, nvg)

            if (!isFirstLogin) {
                drawButtons(mouseX, mouseY, sr, nvg)
            }
        })

        currentScene?.drawScreen(mouseX, mouseY, partialTicks)

        if (fadeBackgroundAnimation == null || (fadeBackgroundAnimation != null && !fadeBackgroundAnimation!!.isDone(Direction.FORWARDS))) {
            nvg.setupAndDraw(Runnable { drawSplashScreen(sr, nvg) })
            if (!soundPlayed) {
                Sound.play("shindo/audio/start.wav", true)
                soundPlayed = true
            }
        }

        nvg.setupAndDraw(Runnable { EventRenderNotification().call() })

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager) {
        val copyright = "Copyright Mojang AB. Do not distribute!"
        val currentBackground = instance.profileManager.backgroundManager.currentBackground

        when (currentBackground) {
            is DefaultBackground -> {
                nvg.drawImage(
                    currentBackground.image,
                    -21f + backgroundAnimations[0].value / 90,
                    backgroundAnimations[1].value * -1 / 90,
                    sr.scaledWidth + 21f,
                    sr.scaledHeight + 20f
                )
            }
            is CustomBackground -> {
                nvg.drawImage(
                    currentBackground.image,
                    -21f + backgroundAnimations[0].value / 90,
                    backgroundAnimations[1].value * -1 / 90,
                    sr.scaledWidth + 21f,
                    sr.scaledHeight + 20f
                )
            }
            is ShaderBackground -> {
                ShaderBackgroundRenderer.renderShaderBackground(
                    nvg,
                    currentBackground.shaderFile,
                    -21f + backgroundAnimations[0].value / 90,
                    backgroundAnimations[1].value * -1 / 90,
                    sr.scaledWidth + 21f,
                    sr.scaledHeight + 20f
                )
            }
        }

        nvg.drawText(copyright, sr.scaledWidth - (nvg.getTextWidth(copyright, 9f, Fonts.REGULAR)) - 4, sr.scaledHeight - 12f, Color.WHITE, 9f, Fonts.REGULAR)
        nvg.drawText("Shindo Client v" + instance.version, 4f, sr.scaledHeight - 12f, Color.WHITE, 9f, Fonts.REGULAR)
    }

    private fun drawButtons(mouseX: Int, mouseY: Int, sr: ScaledResolution, nvg: NanoVGManager) {
        val controlColor = getControlFillColor()

        closeFocusAnimation.setAnimation(
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 28f, 6f, 22f, 22f)) 1.0f else 0.0f,
            16.0
        )

        nvg.drawRoundedRect(sr.scaledWidth - 28f, 6f, 22f, 22f, 4f, controlColor)
        nvg.drawCenteredText(
            LegacyIcon.X,
            sr.scaledWidth - 19f,
            8f,
            Color(255, 255 - (closeFocusAnimation.value * 200).toInt(), 255 - (closeFocusAnimation.value * 200).toInt()),
            18f,
            Fonts.LEGACYICON
        )

        backgroundSelectFocusAnimation.setAnimation(
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 56f, 6f, 22f, 22f)) 1.0f else 0.0f,
            16
        )

        nvg.drawRoundedRect(sr.scaledWidth - 56f, 6f, 22f, 22f, 4f, controlColor)
        nvg.drawCenteredText(
            LegacyIcon.IMAGE,
            sr.scaledWidth - 52f + 6.5f - 1.5f,
            9.5f - 1.5f,
            Color(255 - (backgroundSelectFocusAnimation.value * 200).toInt(), 255, 255 - (backgroundSelectFocusAnimation.value * 200).toInt()),
            18f,
            Fonts.LEGACYICON
        )

        shopFocusAnimation.setAnimation(
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 84f, 6f, 22f, 22f)) 1.0f else 0.0f,
            16
        )

        nvg.drawRoundedRect(sr.scaledWidth - 84f, 6f, 22f, 22f, 4f, controlColor)
        nvg.drawCenteredText(
            LegacyIcon.SHOPPING,
            sr.scaledWidth - 78f + 4.5f,
            9.5f,
            Color(255 - (shopFocusAnimation.value * 200).toInt(), 255, 255),
            15f,
            Fonts.LEGACYICON
        )

        skinFocusAnimation.setAnimation(
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 112f, 6f, 22f, 22f)) 1.0f else 0.0f,
            16
        )

        nvg.drawRoundedRect(sr.scaledWidth - 112f, 6f, 22f, 22f, 4f, controlColor)
        nvg.drawCenteredText(
            LegacyIcon.SKIN,
            sr.scaledWidth - 104f + 3.5f,
            9.5f,
            Color(255 - (skinFocusAnimation.value * 200).toInt(), 255, 255),
            15f,
            Fonts.LEGACYICON
        )
    }

    private fun drawSplashScreen(sr: ScaledResolution, nvg: NanoVGManager) {
        if (fadeIconAnimation == null) {
            fadeIconAnimation = DecelerateAnimation(100, 1.0)
            fadeIconAnimation!!.setDirection(Direction.FORWARDS)
            fadeIconAnimation!!.reset()
        }

        if (fadeIconAnimation != null) {
            if (fadeIconAnimation!!.isDone(Direction.FORWARDS) && fadeBackgroundAnimation == null) {
                fadeBackgroundAnimation = DecelerateAnimation(500, 1.0)
                fadeBackgroundAnimation!!.setDirection(Direction.FORWARDS)
                fadeBackgroundAnimation!!.reset()
            }

            nvg.drawRect(0f, 0f, sr.scaledWidth.toFloat(), sr.scaledHeight.toFloat(), Color(0, 0, 0, if (fadeBackgroundAnimation != null) (255 - (fadeBackgroundAnimation!!.getValue() * 255)).toInt() else 255))
            nvg.drawCenteredText(
                LegacyIcon.SHINDO,
                sr.scaledWidth / 2f,
                (sr.scaledHeight / 2f) - (nvg.getTextHeight(LegacyIcon.SHINDO, 130f, Fonts.LEGACYICON) / 2) - 1,
                Color(255, 255, 255, (255 - (fadeIconAnimation!!.getValue() * 255)).toInt()),
                130f,
                Fonts.LEGACYICON
            )
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)

        val isFirstLogin = Shindo.getInstance().shindoAPI.isFirstLogin()

        if (mouseButton == 0 && !isFirstLogin) {
            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 28f, 6f, 22f, 22f)) {
                mc.shutdown()
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 56f, 6f, 22f, 22f) && currentScene != getSceneByClass(BackgroundScene::class.java)) {
                setCurrentScene(getSceneByClass(BackgroundScene::class.java))
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 84f, 6f, 22f, 22f)) {
                setCurrentScene(getSceneByClass(ShopScene::class.java))
            }

            if (MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 112f, 6f, 22f, 22f)) {
                setCurrentScene(getSceneByClass(SkinScene::class.java))
            }
        }

        currentScene?.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        currentScene?.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        currentScene?.keyTyped(typedChar, keyCode)
    }

    override fun handleInput() {
        super.handleInput()
    }

    override fun onGuiClosed() {
        currentScene?.onGuiClosed()
    }

    fun setCurrentScene(currentScene: MainMenuScene?) {
        this.currentScene?.onSceneClosed()
        this.currentScene = currentScene
        this.currentScene?.initScene()
    }

    fun isDoneBackgroundAnimation(): Boolean {
        return fadeBackgroundAnimation != null && fadeBackgroundAnimation!!.isDone(Direction.FORWARDS)
    }

    fun getSceneByClass(clazz: Class<out MainMenuScene>): MainMenuScene? {
        for (scene in scenes) {
            if (scene.javaClass == clazz) {
                return scene
            }
        }
        return null
    }

    private fun ensureDefaultColorScheme(instance: Shindo?, forceDefaults: Boolean) {
        if (instance == null) {
            return
        }
        val colorManager: ColorManager? = instance.colorManager
        if (colorManager == null) {
            return
        }
        if (forceDefaults || colorManager.currentColor == null) {
            colorManager.currentColor = colorManager.getColorByName("Default")
        }
        if (forceDefaults || colorManager.theme == null) {
            colorManager.theme = Theme.DARK
        }
    }

    private fun getControlFillColor(): Color {
        val instance = Shindo.getInstance()
        val palette: ColorPalette? = if (instance != null && instance.colorManager != null) instance.colorManager.palette else null
        val base = palette?.getBackgroundColor(ColorType.NORMAL) ?: getBackgroundColor()
        return ColorUtils.applyAlpha(base, Math.min(255, base.alpha + 5))
    }

    fun getBackgroundColor(): Color {
        val instance = Shindo.getInstance()
        val palette: ColorPalette? = if (instance != null && instance.colorManager != null) instance.colorManager.palette else null
        val base = palette?.getBackgroundColor(ColorType.DARK) ?: Color(30, 30, 30)
        return ColorUtils.applyAlpha(base, 235)
    }
}
